#version 330 core
layout (location = 0) in vec3 inPos;
layout (location = 2) in vec2 inTexCoords;

uniform mat4 model;

out VS_OUT {
    vec2 texCoords;
} vsOut;

void main() {
    vsOut.texCoords = inTexCoords;
    gl_Position = model * vec4(inPos, 1.0f);
}
