#version 330 core
layout (location = 0) in vec3 inPos;

layout (std140) uniform Matrices {
    vec4 inViewPos;
    mat4 view;
    mat4 projection;
};

out vec3 texCoords;

void main() {
    texCoords = inPos;
    vec4 pos = projection * mat4(mat3(view)) * vec4(inPos, 1.0f);
    gl_Position = pos.xyww;
}
