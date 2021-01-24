#version 330 core
layout (location = 0) in vec3 aPos;

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
    mat4 lightSpaceMatrix;
};

out VS_OUT {
    vec3 texCoords;
    vec4 pos;
} vsOut;

void main() {
    vsOut.texCoords = aPos;
    vsOut.pos = vec4(aPos, 1.0f);
    vec4 pos = projection * mat4(mat3(view)) * vec4(aPos, 1.0);
    gl_Position = pos.xyww;
}