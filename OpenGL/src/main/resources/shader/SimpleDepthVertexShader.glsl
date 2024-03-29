#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aTexCoords;

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
};
uniform mat4 model;
uniform mat4 lightSpaceMatrix;

out vec2 texCoords;

void main() {
    gl_Position = lightSpaceMatrix * model * vec4(aPos, 1.0f);
    texCoords = aTexCoords;
}
