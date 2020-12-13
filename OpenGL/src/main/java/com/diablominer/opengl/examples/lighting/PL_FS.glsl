#version 330 core
out vec4 fragmentColor;

in vec3 normal;
in vec3 fragPos;

uniform vec3 lightColor;
uniform vec3 lightPos;
uniform vec3 objectColor;
uniform vec3 viewPos;

void main() {
    float ambientStrength = 0.3f;
    vec3 ambient = ambientStrength * lightColor;

    float diffuseStrength = 1.0f;
    vec3 norm = normalize(normal);
    vec3 lightDir = normalize(lightPos - fragPos);
    float diff = max(dot(norm, lightDir), 0.0f);
    vec3 diffuse = diffuseStrength * diff * lightColor;

    float specularStrength = 0.7f;
    float shininess = 32.0f;
    vec3 viewDir = normalize(viewPos - fragPos);
    vec3 reflection = reflect(-lightDir, norm);
    float spec = pow(max(dot(reflection, viewDir), 0.0f), shininess);
    vec3 specular = specularStrength * spec * lightColor;

    vec3 result = (ambient +  diffuse + specular) * objectColor;
    fragmentColor = vec4(result, 1.0f);
}
