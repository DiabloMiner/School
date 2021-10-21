#version 330 core


struct Material {
    sampler2D texture_diffuse1;
    sampler2D texture_specular1;
    float shininess;
};

uniform Material material;

in vec2 texCoords;

void main() {
    if (texture(material.texture_diffuse1, texCoords).w < 1.0f) {
        discard;
    }
}
