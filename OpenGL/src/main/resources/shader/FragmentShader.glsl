#version 330 core
layout (location = 0) out vec4 fragmentColor;
layout (location = 1) out vec4 brightColor;

struct Material {
    sampler2D texture_color1;
    sampler2D texture_normal1;
    sampler2D texture_displacement1;
    sampler2D texture_metallic1;
    sampler2D texture_roughness1;
    sampler2D texture_ao1;
};

struct DirectionaLight {
    vec3 direction;

    vec3 color;

    sampler2D shadowMap;
};

struct PointLight {
    vec3 position;

    vec3 color;

    float farPlane;
};

struct SpotLight {
    vec3 position;
    vec3 direction;

    vec3 color;

    sampler2D shadowMap;
};

const float pi = 3.14159265359;

in vec3 fragPos;
in vec2 texCoord;
in vec4 dirLightFragPosLightSpace;
in vec4 spotLightFragPosLightSpace;
in vec3 tangentViewPos;
in vec3 tangentFragPos;
in mat3 TBN;

uniform vec3 viewPos;
uniform Material material;
uniform DirectionaLight dirLight;
uniform PointLight pointLight;
uniform SpotLight spotLight;
uniform sampler2D brdfLUT;

float distributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness * roughness;
    float a2 = a * a;
    float NdotH = max(dot(N, H), 0.0f);
    float NdotH2 = NdotH * NdotH;

    float nom  = a2;
    float denom = (NdotH2 * (a2 - 1.0f) + 1.0f);
    denom = pi * denom * denom;

    return nom / max(denom, 0.00001f);
}

float geometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0f);
    float k = (r * r) / 8.0f;

    float num = NdotV;
    float denom = NdotV * (1.0f - k) + k;

    return num / denom;
}

float geometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2  = geometrySchlickGGX(NdotV, roughness);
    float ggx1  = geometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return max(F0 + (1.0f - F0) * pow(max(1.0f - cosTheta, 0.0f), 5.0f), 0.0f);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(max(1.0 - cosTheta, 0.0), 5.0);
}

vec2 parallaxMapping(vec2 texCoords, vec3 viewDir) {
    const float heightScale = 0.1f;
    const float minLayers = 8.0f;
    const float maxLayers = 32.0f;

    float numLayers = mix(maxLayers, minLayers, max(dot(vec3(0.0f, 0.0f, 1.0f), viewDir), 0.0f));
    float layerDepth = 1.0f / numLayers;
    float currentLayerDepth = 0.0f;
    viewDir.y = -viewDir.y;
    vec2 P = viewDir.xy / viewDir.z * heightScale;
    vec2 deltaTexCoords = P / numLayers;

    vec2 currentTexCoords = texCoords;
    float currentDepthMapValue = texture(material.texture_displacement1, currentTexCoords).r;

    while(currentLayerDepth < currentDepthMapValue) {
        currentTexCoords -= deltaTexCoords;
        currentDepthMapValue = texture(material.texture_displacement1, currentTexCoords).r;
        currentLayerDepth += layerDepth;
    }

    vec2 prevTexCoords = currentTexCoords + deltaTexCoords;

    float afterDepth  = currentDepthMapValue - currentLayerDepth;
    float beforeDepth = texture(material.texture_displacement1, prevTexCoords).r - currentLayerDepth + layerDepth;

    float weight = afterDepth / (afterDepth - beforeDepth);
    vec2 finalTexCoords = prevTexCoords * weight + currentTexCoords * (1.0 - weight);

    return finalTexCoords;
}

float directionalShadowCalculation(vec4 fragPosLightSpace, vec3 direction, vec3 normal, sampler2D shadowMap) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    float closestDepth = texture(shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;
    float bias = max(0.04f * (1.0f - dot(normal, direction)), 0.005f);

    float shadow = 0.0f;
    vec2 texelSize = 1.0 / textureSize(shadowMap, 0);
    for(int x = -1; x <= 1; ++x) {
        for(int y = -1; y <= 1; ++y) {
            float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }
    shadow /= 9.0f;

    if (projCoords.z > 1.0f) {
        shadow = 0.0f;
    }

    return shadow;
}

float omnidirectionalShadowCalculation(vec3 fragPos, vec3 lightPos, vec3 normal, float farPlane) {
    vec3 sampleOffsetDirections[20] = vec3[] (
        vec3( 1,  1,  1), vec3( 1, -1,  1), vec3(-1, -1,  1), vec3(-1,  1,  1),
        vec3( 1,  1, -1), vec3( 1, -1, -1), vec3(-1, -1, -1), vec3(-1,  1, -1),
        vec3( 1,  1,  0), vec3( 1, -1,  0), vec3(-1, -1,  0), vec3(-1,  1,  0),
        vec3( 1,  0,  1), vec3(-1,  0,  1), vec3( 1,  0, -1), vec3(-1,  0, -1),
        vec3( 0,  1,  1), vec3( 0, -1,  1), vec3( 0, -1, -1), vec3( 0,  1, -1)
    );

    vec3 fragToLight = fragPos - lightPos;
    float currentDepth = length(fragToLight);

    float bias = 0.15f;
    float shadow = 0.0f;
    int samples = 20;
    float viewDistance = length(viewPos - fragPos);
    float diskRadius = (1.0f + (viewDistance / farPlane)) / 25.0f;

    for(int i = 0; i < samples; ++i) {
        float closestDepth = 0.0f;
        closestDepth *= farPlane;
        if(currentDepth - bias > closestDepth)
            shadow += 1.0f;
    }
    shadow /= float(samples);

    return shadow;
}

vec3 calcDirLight(DirectionaLight dirLight, vec3 normal, vec3 viewDir, vec2 texCoords, float roughness, float metallic, vec3 F0) {
    vec3 lightDir = normalize(-dirLight.direction);
    vec3 halfwayDir = normalize(viewDir + lightDir);
    vec3 radiance = dirLight.color;

    float NDF = distributionGGX(normal, halfwayDir, roughness);
    float G = geometrySmith(normal, viewDir, lightDir, roughness);
    vec3 F = fresnelSchlick(clamp(dot(halfwayDir, viewDir), 0.0f, 1.0f), F0);

    vec3 kS = F;
    vec3 kD = vec3(1.0f) - kS;
    kD *= (1.0f - metallic);

    vec3 nominator = NDF * G * F;
    float denominatior = 4.0f * max(dot(normal, viewDir), 0.0f) * max(dot(normal, lightDir), 0.0f);
    vec3 specular = nominator / max(denominatior, 0.001f);

    float shadow = directionalShadowCalculation(dirLightFragPosLightSpace, dirLight.direction, normal, dirLight.shadowMap);
    shadow *= floor(texture(material.texture_color1, texCoords).w);

    float NdotL = max(dot(normal, lightDir), 0.0f);
    vec3 albedo = texture(material.texture_color1, texCoords).rgb;
    albedo *= (1.0f - shadow);
    specular *= (1.0f - shadow);
    vec3 Lo = ((kD * (albedo / pi)) + specular) * radiance * NdotL;
    return Lo;
}

vec3 calcPointLight(PointLight pointLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec2 texCoords, float roughness, float metallic, vec3 F0) {
    vec3 lightDir = normalize(pointLight.position - fragPos);
    vec3 halfwayDir = normalize(viewDir + lightDir);
    float distance = length(pointLight.position - fragPos);
    float attenuation = 1.0f / (distance * distance);
    vec3 radiance = pointLight.color * attenuation;

    float NDF = distributionGGX(normal, halfwayDir, roughness);
    float G = geometrySmith(normal, viewDir, lightDir, roughness);
    vec3 F = fresnelSchlick(clamp(dot(halfwayDir, viewDir), 0.0f, 1.0f), F0);

    vec3 kS = F;
    vec3 kD = vec3(1.0f) - kS;
    kD *= (1.0f - metallic);

    vec3 nominator = NDF * G * F;
    float denominatior = 4.0f * max(dot(normal, viewDir), 0.0f) * max(dot(normal, lightDir), 0.0f);
    vec3 specular = nominator / max(denominatior, 0.001f);

    float shadow = omnidirectionalShadowCalculation(fragPos, pointLight.position, normal, pointLight.farPlane);
    shadow *= floor(texture(material.texture_color1, texCoords).w);

    float NdotL = max(dot(normal, lightDir), 0.0f);
    vec3 albedo = texture(material.texture_color1, texCoords).rgb;
    albedo *= (1.0f - shadow);
    specular *= (1.0f - shadow);
    vec3 Lo = ((kD * (albedo / pi)) + specular) * radiance * NdotL;
    return Lo;
}

vec3 calcSpotLight(SpotLight spotLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec2 texCoords, float roughness, float metallic, vec3 F0) {
    vec3 lightDir = normalize(spotLight.position - fragPos);
    vec3 halfwayDir = normalize(viewDir + lightDir);

    float theta = dot(lightDir, normalize(-spotLight.direction));
    float intensity = clamp(dot(normalize(fragPos - spotLight.position), spotLight.direction), 0.0f, 1.0f);
    float distance = length(spotLight.position - fragPos);
    float attenuation = 1.0f / (distance * distance);
    vec3 radiance = spotLight.color * attenuation * intensity;

    float NDF = distributionGGX(normal, halfwayDir, roughness);
    float G = geometrySmith(normal, viewDir, lightDir, roughness);
    vec3 F = fresnelSchlick(clamp(dot(halfwayDir, viewDir), 0.0f, 1.0f), F0);

    vec3 kS = F;
    vec3 kD = vec3(1.0f) - kS;
    kD *= (1.0f - metallic);

    vec3 nominator = NDF * G * F;
    float denominatior = 4.0f * max(dot(normal, viewDir), 0.0f) * max(dot(normal, lightDir), 0.0f);
    vec3 specular = nominator / max(denominatior, 0.001f);

    float shadow = directionalShadowCalculation(spotLightFragPosLightSpace, spotLight.direction, normal, spotLight.shadowMap);
    shadow *= floor(texture(material.texture_color1, texCoords).w);
    shadow *= intensity;

    float NdotL = max(dot(normal, lightDir), 0.0f);
    vec3 albedo = texture(material.texture_color1, texCoords).rgb;
    albedo *= (1.0f - shadow);
    specular *= (1.0f - shadow);
    vec3 Lo = ((kD * (albedo / pi)) + specular) * radiance * NdotL;
    return Lo;
}

void main() {
    // Normal rendering
    vec3 tangentViewDir = normalize(tangentViewPos - tangentFragPos);
    vec2 parallaxMappedTexCoords = parallaxMapping(texCoord, tangentViewDir);
    if (parallaxMappedTexCoords.x > 1.0 || parallaxMappedTexCoords.y > 1.0 || parallaxMappedTexCoords.x < 0.0 || parallaxMappedTexCoords.y < 0.0)
        discard;

    vec3 rgbNormal = texture(material.texture_normal1, parallaxMappedTexCoords).rgb * 2.0f - 1.0f;
    vec3 norm = normalize(TBN * rgbNormal);
    vec3 viewDir = normalize(viewPos - fragPos);

    float metallic = texture(material.texture_metallic1, parallaxMappedTexCoords).r;
    float roughness = texture(material.texture_roughness1, parallaxMappedTexCoords).r;
    float ao = texture(material.texture_ao1, parallaxMappedTexCoords).r;

    vec3 F0 = vec3(0.04f);
    F0 = mix(F0, texture(material.texture_color1, parallaxMappedTexCoords).rgb, metallic);

    vec3 Lo = vec3(0.0f);
    Lo += calcDirLight(dirLight, norm, viewDir, parallaxMappedTexCoords, roughness, metallic, F0);
    Lo += calcPointLight(pointLight, norm, fragPos, viewDir, parallaxMappedTexCoords, roughness, metallic, F0);
    Lo += calcSpotLight(spotLight, norm, fragPos, viewDir, parallaxMappedTexCoords, roughness, metallic, F0);

    vec3 kS = fresnelSchlickRoughness(max(dot(norm, viewDir), 0.0), F0, roughness);
    vec3 kD = 1.0f - kS;
    vec3 irradiance = vec3(0.5f);
    vec3 diffuse = irradiance * texture(material.texture_color1, parallaxMappedTexCoords).rgb;

    vec3 reflectionVector = reflect(-viewDir, norm);
    const float MAX_REFLECTION_LOD = 4.0f;
    vec3 prefilteredColor = vec3(1.0f);
    vec3 F = fresnelSchlickRoughness(max(dot(norm, viewDir), 0.0), F0, roughness);
    vec2 envBRDF = texture(brdfLUT, vec2(max(dot(norm, viewDir), 0.0), roughness)).rg;
    vec3 specular = prefilteredColor * (F * envBRDF.x + envBRDF.y);

    vec3 ambient = (kD * diffuse + specular) * ao;
    Lo += ambient;

    fragmentColor = vec4(Lo, texture(material.texture_color1, parallaxMappedTexCoords).w);

    // Brightness color is determined
    float brightness = dot(fragmentColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    if (brightness > 1.5f) {
        brightColor = vec4(fragmentColor.rgb, 1.0f);
    } else {
        brightColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }

    // TODO: Investigate issue: Edges of cube are blue
}