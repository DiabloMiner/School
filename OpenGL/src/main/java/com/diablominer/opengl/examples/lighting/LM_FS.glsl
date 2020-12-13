#version 330 core
out vec4 fragmentColor;

struct Material {
    sampler2D diffuse;
    sampler2D specular;
    float shininess;
};

struct Light {
    vec3 position;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

in vec3 normal;
in vec3 fragPos;
in vec2 texCoord;

uniform Material material;
uniform Light light;
uniform vec3 lightColor;
uniform vec3 lightPos;
uniform vec3 viewPos;

void main() {
    vec3 ambient = light.ambient * vec3(texture(material.diffuse, texCoord));

    vec3 norm = normalize(normal);
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = light.diffuse * diff * vec3(texture(material.diffuse, texCoord));

    float specularStrength = 0.7f;
    float shininess = 32.0f;
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflection = reflect(-lightDir, norm);
    float spec = pow(max(dot(reflection, viewDir), 0.0f), shininess);
    vec3 specular = light.specular * spec * vec3(texture(material.specular, texCoord));

    vec3 result = ambient +  diffuse + specular;
    fragmentColor = vec4(result, 1.0f);
}
