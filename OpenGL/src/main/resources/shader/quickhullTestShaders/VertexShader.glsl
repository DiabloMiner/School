#version 330 core
layout (location = 0) in vec3 aPos;

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
};

uniform mat4 model;

out float color;

void main() {
    color = gl_VertexID;
    if(aPos == vec3(0.0f, 0.0f, 0.0f)) {
        color = 0.0f;
    }
    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
