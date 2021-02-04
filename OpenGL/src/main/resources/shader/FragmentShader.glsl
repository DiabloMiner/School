#version 330 core
out vec4 fragmentColor;

struct Material {
    sampler2D texture_diffuse1;
    sampler2D texture_specular1;
    sampler2D texture_normal1;
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

    samplerCube shadowMap;
    float farPlane;
};

in vec3 fragPos;
in vec2 texCoord;
in vec4 fragPosLightSpace;
in vec3 vPos;

in vec3 dirLightDirection;
in vec3 pointLightPosition;
in vec3 spotLightPosition;
in vec3 spotLightDirection;

uniform Material material;
uniform DirectionaLight dirLight;
uniform PointLight pointLight;
uniform SpotLight spotLight;

float directionalShadowCalculation(vec4 fragPosLightSpace, DirectionaLight dirLight, vec3 normal, vec3 dirLightDirection) {
    vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
    projCoords = projCoords * 0.5 + 0.5;

    float closestDepth = texture(dirLight.shadowMap, projCoords.xy).r;
    float currentDepth = projCoords.z;
    float bias = max(0.04f * (1.0f - dot(normal, dirLightDirection)), 0.005f);

    float shadow = 0.0f;
    vec2 texelSize = 1.0 / textureSize(dirLight.shadowMap, 0);
    for(int x = -1; x <= 1; ++x)
    {
        for(int y = -1; y <= 1; ++y)
        {
            float pcfDepth = texture(dirLight.shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
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
    float viewDistance = length(vPos - fragPos);
    float diskRadius = (1.0f + (viewDistance / farPlane)) / 25.0f;

    for(int i = 0; i < samples; ++i)
    {
        float closestDepth = texture(shadowMap, fragToLight + sampleOffsetDirections[i] * diskRadius).r;
        closestDepth *= farPlane;
        if(currentDepth - bias > closestDepth)
            shadow += 1.0f;
    }
    shadow /= float(samples);

    return shadow;
}

vec3 calcDirLight(DirectionaLight dirLight, vec3 normal, vec3 viewDir, vec3 dirLightDirection) {
    vec3 lightDir = normalize(dirLightDirection);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0f), material.shininess);

    vec3 ambient = dirLight.ambient;
    vec3 diffuse = dirLight.diffuse * diff;
    vec3 specular = dirLight.specular * spec;

    float shadow = directionalShadowCalculation(fragPosLightSpace, dirLight, normal, dirLightDirection);
    shadow *= floor(texture(material.texture_diffuse1, texCoord).w);
    vec3 lighting = (ambient + (1.0f - shadow) * (diffuse + specular)) * texture(material.texture_specular1, texCoord).xyz;

    return (lighting);
}

vec3 calcPointLight(PointLight pointLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 lightPos) {
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0f), material.shininess);

    float distance = length(lightPos - fragPos);
    float attenuation = 1.0 / (pointLight.constant + pointLight.linear * distance + pointLight.quadratic * distance * distance);
    attenuation = 1.0f;

    vec3 ambient = pointLight.ambient * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 diffuse = pointLight.diffuse * diff;
    vec3 specular = pointLight.specular * spec;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;

    float shadow = omnidirectionalShadowCalculation(fragPos, lightPos, normal, pointLight.shadowMap, pointLight.farPlane);
    vec3 lighting = (ambient + (1.0f - shadow) * (diffuse * texture(material.texture_diffuse1, texCoord).xyz + specular * texture(material.texture_specular1, texCoord).xyz));

    return (lighting);
}

vec3 calcSpotLight(SpotLight spotLight, vec3 normal, vec3 fragPos, vec3 viewDir, vec3 lightPos, vec3 spotLightDirection) {
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0f), material.shininess);

    float theta = dot(lightDir, normalize(-spotLightDirection));
    float epsilon = spotLight.cutOff - spotLight.outerCutOff;
    float intensity = clamp((theta - spotLight.outerCutOff) / epsilon, 0.0f, 1.0f);

    float distance = length(lightPos - fragPos);
    float attenuation = 1.0f / (spotLight.constant + spotLight.linear * distance + spotLight.quadratic * (distance * distance));

    vec3 ambient = spotLight.ambient * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 diffuse = spotLight.diffuse * diff;
    vec3 specular = spotLight.specular * spec;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;
    diffuse *= intensity;
    specular *= intensity;

    float shadow = omnidirectionalShadowCalculation(fragPos, spotLight.position, normal, spotLight.shadowMap, spotLight.farPlane);
    vec3 lighting = (ambient + (1.0f - shadow) * (diffuse * texture(material.texture_diffuse1, texCoord).xyz + specular * texture(material.texture_specular1, texCoord).xyz));

    return (lighting);
}

void main() {
    vec3 normal = texture(material.texture_normal1, texCoord).rgb;
    normal = normal * 2.0f - 1.0f;
    vec3 norm = normalize(normal);
    vec3 viewDir = normalize(vPos - fragPos);

    vec3 result = calcDirLight(dirLight, norm, viewDir, dirLightDirection);
    result += calcPointLight(pointLight, norm, fragPos, viewDir, pointLightPosition);
    result += calcSpotLight(spotLight, norm, fragPos, viewDir, spotLightPosition, spotLightDirection);

    fragmentColor = vec4(result, texture(material.texture_diffuse1, texCoord).w);
}