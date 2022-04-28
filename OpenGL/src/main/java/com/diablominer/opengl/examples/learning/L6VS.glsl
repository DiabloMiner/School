#version 330 core
layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inTexCoords;

out vec3 outNormal;
out vec2 outTexCoords;
out vec3 fragPos;

out vec3 viewPos;

layout (std140) uniform Matrices {
    vec4 inViewPos;
    mat4 view;
    mat4 projection;
};

uniform mat4 model;

void main() {
    viewPos = inViewPos.xyz;
    outTexCoords = inTexCoords;
    outNormal = mat3(transpose(inverse(model))) * inNormal;
    fragPos = vec3(model * vec4(inPos, 1.0f));
    gl_Position = projection * view * model * vec4(inPos, 1.0f);
}
