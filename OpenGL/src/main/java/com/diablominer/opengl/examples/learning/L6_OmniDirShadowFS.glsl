#version 330 core

struct PointLight {
    vec3 position;

    vec3 color;

    samplerCube shadowMap;
    float far;
};

in vec4 fragPos;

uniform PointLight pointLight0;

void main() {
    float lightDistance = length(fragPos.xyz - pointLight0.position);
    lightDistance = lightDistance / pointLight0.far;
    gl_FragDepth = lightDistance;
}
