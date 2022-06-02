#version 330 core
layout (location = 0) in vec3 inPos;

uniform mat4 model;
uniform mat4 lightSpaceMat[];

void main() {
    gl_Position = lightSpaceMat[0] * model * vec4(inPos, 1.0f);
}
