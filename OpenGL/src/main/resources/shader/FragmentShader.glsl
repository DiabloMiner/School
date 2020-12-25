#version 330 core
out vec4 fragmentColor;

struct Material {
    sampler2D texture_diffuse1;
    sampler2D texture_specular1;
    float shininess;
};

struct DirectionaLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

struct PointLight {
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
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
};

in vec3 normal;
in vec3 fragPos;
in vec2 texCoord;

uniform Material material;
uniform DirectionaLight dirLight;
uniform PointLight pointLight;
uniform SpotLight spotLight;
uniform vec3 viewPos;

vec3 calcDirLight(DirectionaLight dirLight, vec3 normal, vec3 viewDir) {
    vec3 lightDir = normalize(-dirLight.direction);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0f), material.shininess);

    vec3 ambient = dirLight.ambient * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 diffuse = dirLight.diffuse * diff * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 specular = dirLight.specular * spec * texture(material.texture_specular1, texCoord).xyz;

    return (ambient + diffuse + specular);
}

vec3 calcPointLight(PointLight pointLight, vec3 normal, vec3 fragPos, vec3 viewDir) {
    vec3 lightDir = normalize(pointLight.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0f), material.shininess);

    float distance = length(pointLight.position - fragPos);
    float attenuation = 1.0 / (pointLight.constant + pointLight.linear * distance + pointLight.quadratic * distance * distance);
    attenuation = 1.0f;

    vec3 ambient = pointLight.ambient * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 diffuse = pointLight.diffuse * diff * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 specular = pointLight.specular * spec * texture(material.texture_specular1, texCoord).xyz;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;

    return (ambient + diffuse + specular);
}

vec3 calcSpotLight(SpotLight spotLight, vec3 normal, vec3 fragPos, vec3 viewDir) {
    vec3 lightDir = normalize(spotLight.position - fragPos);
    float diff = max(dot(normal, lightDir), 0.0f);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0f), material.shininess);

    float theta = dot(lightDir, normalize(-spotLight.direction));
    float epsilon = spotLight.cutOff - spotLight.outerCutOff;
    float intensity = clamp((theta - spotLight.outerCutOff) / epsilon, 0.0, 1.0);
    float distance = length(spotLight.position - fragPos);
    float attenuation = 1.0 / (spotLight.constant + spotLight.linear * distance + spotLight.quadratic * (distance * distance));

    vec3 ambient = spotLight.ambient * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 diffuse = spotLight.diffuse * diff * texture(material.texture_diffuse1, texCoord).xyz;
    vec3 specular = spotLight.specular * spec * texture(material.texture_specular1, texCoord).xyz;
    ambient *= attenuation;
    diffuse *= attenuation;
    specular *= attenuation;
    diffuse *= intensity;
    specular *= intensity;

    return (ambient + diffuse + specular);
}

void main() {
    vec3 norm = normalize(normal);
    vec3 viewDir = normalize(viewPos - fragPos);

    vec3 result = calcDirLight(dirLight, norm, viewDir);
    result += calcPointLight(pointLight, norm, fragPos, viewDir);
    result += calcSpotLight(spotLight, norm, fragPos, viewDir);

    fragmentColor = vec4(result, texture(material.texture_diffuse1, texCoord).w);
}