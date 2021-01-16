#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
};

uniform mat4 model;

out vec3 normal;
out vec3 fragPos;
out vec2 texCoord;

void main() {
    fragPos = vec3(model * vec4(aPos, 1.0f));
    normal = mat3(transpose(inverse(model))) * aNormal;
    texCoord = aTexCoords;

    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
