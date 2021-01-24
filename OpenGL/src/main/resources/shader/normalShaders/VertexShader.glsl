#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
    mat4 lightSpaceMatrix;
};

uniform mat4 model;

out vec3 normal;
out vec3 fragPos;
out vec2 texCoord;
out vec4 fragPosLightSpace;

void main() {
    fragPos = vec3(model * vec4(aPos, 1.0f));
    normal = mat3(transpose(inverse(model))) * aNormal;
    fragPosLightSpace = lightSpaceMatrix * vec4(fragPos, 1.0f);
    texCoord = aTexCoords;

    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
