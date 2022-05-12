#version 330 core
layout (location = 0) out vec4 fragmentColor;
layout (location = 1) out vec4 brightColor;

struct Material {
    sampler2D texture_color1;
    sampler2D texture_metallic1;
    sampler2D texture_roughness1;
    sampler2D texture_ao1;
};

struct DirectionaLight {
    vec4 direction;

    vec4 color;
};

struct PointLight {
    vec4 position;

    vec4 color;
};

struct SpotLight {
    vec4 position;
    vec4 direction;

    vec4 color;
};

layout (std140) uniform Lights {
    DirectionaLight dirLight;
    PointLight pointLight;
    SpotLight spotLight;
};

const float pi = 3.14159265359;

in vec3 fragPos;
in vec3 outNormal;
in vec2 outTexCoords;
in vec3 viewPos;

uniform Material material;

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

vec3 calcDirLight(DirectionaLight dirLight, vec3 normal, vec3 viewDir, vec2 texCoords, float roughness, float metallic, vec3 F0) {
    vec3 lightDir = normalize(-dirLight.direction.xyz);
    vec3 halfwayDir = normalize(viewDir + lightDir);
    vec3 radiance = dirLight.color.xyz;

    float NDF = distributionGGX(normal, halfwayDir, roughness);
    float G = geometrySmith(normal, viewDir, lightDir, roughness);
    vec3 F = fresnelSchlick(clamp(dot(halfwayDir, viewDir), 0.0f, 1.0f), F0);

    vec3 kS = F;
    vec3 kD = vec3(1.0f) - kS;
    kD *= (1.0f - metallic);

    vec3 nominator = NDF * G * F;
    float denominatior = 4.0f * max(dot(normal, viewDir), 0.0f) * max(dot(normal, lightDir), 0.0f);
    vec3 specular = nominator / max(denominatior, 0.001f);

    float NdotL = max(dot(normal, lightDir), 0.0f);
    vec3 albedo = texture(material.texture_color1, texCoords).rgb;
    vec3 Lo = ((kD * (albedo / pi)) + specular) * radiance * NdotL;
    return Lo;
}

vec3 calcPointLight(PointLight pointLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec2 texCoords, float roughness, float metallic, vec3 F0) {
    vec3 lightDir = normalize(pointLight.position.xyz - fragPos);
    vec3 halfwayDir = normalize(viewDir + lightDir);
    float distance = length(pointLight.position.xyz - fragPos);
    float attenuation = 1.0f / (distance * distance);
    vec3 radiance = pointLight.color.xyz * attenuation;

    float NDF = distributionGGX(normal, halfwayDir, roughness);
    float G = geometrySmith(normal, viewDir, lightDir, roughness);
    vec3 F = fresnelSchlick(clamp(dot(halfwayDir, viewDir), 0.0f, 1.0f), F0);

    vec3 kS = F;
    vec3 kD = vec3(1.0f) - kS;
    kD *= (1.0f - metallic);

    vec3 nominator = NDF * G * F;
    float denominatior = 4.0f * max(dot(normal, viewDir), 0.0f) * max(dot(normal, lightDir), 0.0f);
    vec3 specular = nominator / max(denominatior, 0.001f);

    float NdotL = max(dot(normal, lightDir), 0.0f);
    vec3 albedo = texture(material.texture_color1, texCoords).rgb;
    vec3 Lo = ((kD * (albedo / pi)) + specular) * radiance * NdotL;
    return Lo;
}

vec3 calcSpotLight(SpotLight spotLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec2 texCoords, float roughness, float metallic, vec3 F0) {
    vec3 lightDir = normalize(spotLight.position.xyz - fragPos);
    vec3 halfwayDir = normalize(viewDir + lightDir);

    float theta = dot(lightDir, normalize(-spotLight.direction.xyz));
    float intensity = clamp(dot(normalize(fragPos - spotLight.position.xyz), spotLight.direction.xyz), 0.0f, 1.0f);
    float distance = length(spotLight.position.xyz - fragPos);
    float attenuation = 1.0f / (distance * distance);
    vec3 radiance = spotLight.color.xyz * attenuation * intensity;

    float NDF = distributionGGX(normal, halfwayDir, roughness);
    float G = geometrySmith(normal, viewDir, lightDir, roughness);
    vec3 F = fresnelSchlick(clamp(dot(halfwayDir, viewDir), 0.0f, 1.0f), F0);

    vec3 kS = F;
    vec3 kD = vec3(1.0f) - kS;
    kD *= (1.0f - metallic);

    vec3 nominator = NDF * G * F;
    float denominatior = 4.0f * max(dot(normal, viewDir), 0.0f) * max(dot(normal, lightDir), 0.0f);
    vec3 specular = nominator / max(denominatior, 0.001f);

    float NdotL = max(dot(normal, lightDir), 0.0f);
    vec3 albedo = texture(material.texture_color1, texCoords).rgb;
    vec3 Lo = ((kD * (albedo / pi)) + specular) * radiance * NdotL;
    return Lo;
}

void main() {
    // Normal rendering
    vec3 norm = normalize(outNormal);
    vec3 viewDir = normalize(viewPos - fragPos);

    float metallic = texture(material.texture_metallic1, outTexCoords).r;
    float roughness = texture(material.texture_roughness1, outTexCoords).r;
    float ao = texture(material.texture_ao1, outTexCoords).r;

    vec3 F0 = vec3(0.04f);
    F0 = mix(F0, texture(material.texture_color1, outTexCoords).rgb, metallic);

    vec3 Lo = vec3(0.0f);
    Lo += calcDirLight(dirLight, norm, viewDir, outTexCoords, roughness, metallic, F0);
    Lo += calcPointLight(pointLight, norm, fragPos, viewDir, outTexCoords, roughness, metallic, F0);
    Lo += calcSpotLight(spotLight, norm, fragPos, viewDir, outTexCoords, roughness, metallic, F0);

    fragmentColor = vec4(Lo, texture(material.texture_color1, outTexCoords).w);

    // Brightness color is determined
    float brightness = dot(fragmentColor.rgb, vec3(0.2126f, 0.7152f, 0.0722f));
    if (brightness > 1.5f) {
        brightColor = vec4(fragmentColor.rgb, 1.0f);
    } else {
        brightColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
}