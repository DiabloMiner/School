#version 330 core

struct Material {
    sampler2D texture_diffuse1;
    sampler2D texture_specular1;
    float shininess;
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

in vec4 fragPos;
in vec2 texCoords;

uniform PointLight pointLight;
uniform Material material;
uniform float farPlane;

void main() {
    if (texture(material.texture_diffuse1, texCoords).w < 1.0f) {
        discard;
    } else {
        float lightDistance = length(fragPos.xyz - pointLight.position);
        lightDistance = lightDistance / farPlane;
        gl_FragDepth = lightDistance;
    }
}
