#version 330 core
out vec4 fragmentColor;

struct Material {
    sampler2D texture_diffuse1;
    sampler2D texture_specular1;
    sampler2D texture_normal1;
    sampler2D texture_displacement1;
    float shininess;
};

struct DirectionaLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    sampler2D shadowMap;
};

struct PointLight {
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    samplerCube shadowMap;
    float farPlane;
};

struct SpotLight {
    vec3 position;
    vec3 direction;

    float constant;
    float linear;
    float quadratic;

    float cutOff;
    float outerCutOff;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    sampler2D shadowMap;
};

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
    for(int x = -1; x <= 1; ++x)
    {
        for(int y = -1; y <= 1; ++y)
        {
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

float omnidirectionalShadowCalculation(vec3 fragPos, vec3 lightPos, vec3 normal, samplerCube shadowMap, float farPlane) {
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
        float closestDepth = texture(shadowMap, fragToLight + sampleOffsetDirections[i] * diskRadius).r;
        closestDepth *= farPlane;
        if(currentDepth - bias > closestDepth)
            shadow += 1.0f;
    }
    shadow /= float(samples);

    return shadow;
}

vec3 calcDirLight(DirectionaLight dirLight, vec3 normal, vec3 viewDir, vec2 texCoords) {
    vec3 lightDir = normalize(-dirLight.direction);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0f), material.shininess);

    vec3 ambient = dirLight.ambient * texture(material.texture_diffuse1, texCoords).xyz;
    vec3 diffuse = dirLight.diffuse * diff;
    vec3 specular = dirLight.specular * spec;

    float shadow = directionalShadowCalculation(dirLightFragPosLightSpace, dirLight.direction, normal, dirLight.shadowMap);
    shadow *= floor(texture(material.texture_diffuse1, texCoords).w);
    vec3 lighting = (ambient + (1.0f - shadow) * (diffuse * texture(material.texture_diffuse1, texCoords).xyz + specular * texture(material.texture_specular1, texCoords).xyz));

    return (lighting);
}

vec3 calcPointLight(PointLight pointLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec2 texCoords) {
    vec3 lightDir = normalize(pointLight.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0f), material.shininess);

    float distance = length(pointLight.position - fragPos);
    float attenuation = 1.0 / (pointLight.constant + pointLight.linear * distance + pointLight.quadratic * distance * distance);
    attenuation = 1.0f;

    vec3 ambient = pointLight.ambient * texture(material.texture_diffuse1, texCoords).xyz;
    vec3 diffuse = pointLight.diffuse * diff;
    vec3 specular = pointLight.specular * spec;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;

    float shadow = omnidirectionalShadowCalculation(fragPos, pointLight.position, normal, pointLight.shadowMap, pointLight.farPlane);
    shadow *= floor(texture(material.texture_diffuse1, texCoords).w);
    vec3 lighting = (ambient + (1.0f - shadow) * (diffuse * texture(material.texture_diffuse1, texCoords).xyz + specular * texture(material.texture_specular1, texCoords).xyz));

    return (lighting);
}

vec3 calcSpotLight(SpotLight spotLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec2 texCoords) {
    vec3 lightDir = normalize(spotLight.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0f), material.shininess);

    float theta = dot(lightDir, normalize(-spotLight.direction));
    float epsilon = spotLight.cutOff - spotLight.outerCutOff;
    float intensity = clamp((theta - spotLight.outerCutOff) / epsilon, 0.0f, 1.0f);

    float distance = length(spotLight.position - fragPos);
    float attenuation = 1.0f / (spotLight.constant + spotLight.linear * distance + spotLight.quadratic * (distance * distance));

    vec3 ambient = spotLight.ambient * texture(material.texture_diffuse1, texCoords).xyz;
    vec3 diffuse = spotLight.diffuse * diff;
    vec3 specular = spotLight.specular * spec;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;
    diffuse *= intensity;
    specular *= intensity;

    float shadow = directionalShadowCalculation(spotLightFragPosLightSpace, spotLight.direction, normal, spotLight.shadowMap);
    shadow *= floor(texture(material.texture_diffuse1, texCoords).w);
    shadow *= intensity;
    vec3 lighting = (ambient + (1.0f - shadow) * (diffuse * texture(material.texture_diffuse1, texCoords).xyz + specular * texture(material.texture_specular1, texCoords).xyz));

    return (lighting);
}

void main() {
    vec3 tangentViewDir = normalize(tangentViewPos - tangentFragPos);
    vec2 parallaxMappedTexCoords = parallaxMapping(texCoord, tangentViewDir);
    if (parallaxMappedTexCoords.x > 1.0 || parallaxMappedTexCoords.y > 1.0 || parallaxMappedTexCoords.x < 0.0 || parallaxMappedTexCoords.y < 0.0)
        discard;

    vec3 rgbNormal = texture(material.texture_normal1, parallaxMappedTexCoords).rgb * 2.0f - 1.0f;
    vec3 norm = normalize(TBN * rgbNormal);
    vec3 viewDir = normalize(viewPos - fragPos);

    vec3 result = calcDirLight(dirLight, norm, viewDir, parallaxMappedTexCoords);
    result += calcPointLight(pointLight, norm, fragPos, viewDir, parallaxMappedTexCoords);
    result += calcSpotLight(spotLight, norm, fragPos, viewDir, parallaxMappedTexCoords);

    // TODO: Investigate issue: Edges of cube are blue

    fragmentColor = vec4(result, texture(material.texture_diffuse1, parallaxMappedTexCoords).w);
}