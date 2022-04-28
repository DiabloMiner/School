#version 330 core
out vec4 fragColor;

in vec3 outColor;
in vec2 outTexCoords;

uniform sampler2D texture1;

void main() {
    fragColor = texture(texture1, outTexCoords) * vec4(1.0f, 1.0f, 1.0f, 1.0f);
}