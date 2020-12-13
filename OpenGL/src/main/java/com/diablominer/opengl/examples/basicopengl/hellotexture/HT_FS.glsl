#version 330 core
out vec4 fragmentColor;

in vec3 color;
in vec2 texCoord;

uniform sampler2D inputtedTexture1;
uniform sampler2D inputtedTexture2;

void main() {
    fragmentColor = mix(texture(inputtedTexture1, texCoord), texture(inputtedTexture2, texCoord), 0.3f);
}
