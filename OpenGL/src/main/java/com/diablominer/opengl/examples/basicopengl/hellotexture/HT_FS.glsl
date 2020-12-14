#version 330 core
out vec4 fragmentColor;

in vec3 color;
in vec2 texCoord;

uniform sampler2D inputtedTexture1;

void main() {
    fragmentColor = texture(inputtedTexture1, texCoord) * vec4(color, 1.0f);
}
