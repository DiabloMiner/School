#version 330 core
out vec4 fragmentColor;

in vec3 normal;
in vec3 fragPos;

uniform vec3 viewPos;
uniform samplerCube cubeMap;

vec3 refraction(vec3 fragmentPosition, vec3 cameraPosition, vec3 normal) {
    float ratio = 1.00 / 1.52;
    vec3 I = normalize(fragmentPosition - cameraPosition);
    vec3 R = refract(I, normalize(normal), ratio);
    return texture(cubeMap, R).rgb;
}

void main() {
    fragmentColor = vec4(refraction(fragPos, viewPos, normal), 1.0f);
}