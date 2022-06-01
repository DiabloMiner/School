#version 330 core
layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inTexCoords;

out vec3 outNormal;
out vec2 outTexCoords;
out vec3 fragPos;
out vec4 dirLight0FragPos;
out vec4 spotLight0FragPos;

layout (std140) uniform Matrices {
    vec4 inViewPos;
    mat4 view;
    mat4 projection;
};

uniform mat4 model;
uniform mat4 dirLight0Matrix;
uniform mat4 spotLightLight0Matrix;

void main() {
    outTexCoords = inTexCoords;
    outNormal = mat3(transpose(inverse(model))) * inNormal;
    fragPos = vec3(model * vec4(inPos, 1.0f));
    dirLight0FragPos = dirLight0Matrix * vec4(fragPos, 1.0f);
    spotLight0FragPos = spotLightLight0Matrix * vec4(fragPos, 1.0f);
    gl_Position = projection * view * model * vec4(inPos, 1.0f);
}
