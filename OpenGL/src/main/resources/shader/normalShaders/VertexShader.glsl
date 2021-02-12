#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;
layout (location = 3) in vec3 aTangent;
layout (location = 4) in vec3 aBitangent;

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
};

uniform mat4 model;
uniform mat4 dirLightLightSpaceMatrix;
uniform mat4 spotLightLightSpaceMatrix;

out vec3 normal;
out vec3 fragPos;
out vec2 texCoord;
out vec4 dirLightFragPosLightSpace;
out vec4 spotLightFragPosLightSpace;
out mat3 TBN;

void main() {
    fragPos = vec3(model * vec4(aPos, 1.0f));
    normal = mat3(transpose(inverse(model))) * aNormal;
    dirLightFragPosLightSpace = dirLightLightSpaceMatrix * vec4(fragPos, 1.0f);
    spotLightFragPosLightSpace = spotLightLightSpaceMatrix * vec4(fragPos, 1.0f);
    texCoord = aTexCoords;

    vec3 T = normalize(vec3(model * vec4(aTangent, 0.0f)));
    vec3 N = normalize(vec3(model * vec4(aNormal, 0.0f)));
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(N, T);
    TBN = mat3(T, B, N);

    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
