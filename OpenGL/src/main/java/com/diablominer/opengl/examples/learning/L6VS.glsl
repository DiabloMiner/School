#version 330 core
layout (location = 0) in vec3 inPos;
layout (location = 1) in vec3 inNormal;
layout (location = 2) in vec2 inTexCoords;
layout (location = 3) in vec3 inTangent;
layout (location = 4) in vec3 inBitangent;

out vec3 outNormal;
out vec2 outTexCoords;
out vec3 fragPos;
out vec4 dirLight0FragPos;
out vec4 spotLight0FragPos;
out mat3 TBN;

layout (std140) uniform Matrices {
    vec4 inViewPos;
    mat4 view;
    mat4 projection;
};

uniform mat4 model;
uniform mat4 dirLight0Matrix;
uniform mat4 spotLightLight0Matrix;

void main() {
    vec3 T = normalize(vec3(model * vec4(inTangent, 0.0f)));
    vec3 N = normalize(vec3(model * vec4(inNormal, 0.0f)));
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(N, T);
    TBN = mat3(T, B, N);

    outTexCoords = inTexCoords;
    outNormal = mat3(transpose(inverse(model))) * inNormal;
    fragPos = vec3(model * vec4(inPos, 1.0f));
    dirLight0FragPos = dirLight0Matrix * vec4(fragPos, 1.0f);
    spotLight0FragPos = spotLightLight0Matrix * vec4(fragPos, 1.0f);
    gl_Position = projection * view * model * vec4(inPos, 1.0f);
}
