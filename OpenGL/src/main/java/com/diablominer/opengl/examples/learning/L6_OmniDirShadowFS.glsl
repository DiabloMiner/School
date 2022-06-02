#version 330 core

struct PointLight {
    vec3 position;

    vec3 color;

    samplerCube shadowMap;
};

in vec4 fragPos;
in vec2 outTexCoords;

uniform PointLight pointLight0;
uniform float farPlane;

void main() {
    float lightDistance = length(fragPos.xyz - pointLight.position);
    lightDistance = lightDistance / farPlane;
    gl_FragDepth = lightDistance;
}
