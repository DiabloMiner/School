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

uniform vec3 viewPos;
uniform mat4 model;
uniform mat4 dirLightLightSpaceMatrix;
uniform mat4 spotLightLightSpaceMatrix;

out VS_OUT {
    vec3 fragPos;
    vec2 texCoord;
    vec4 dirLightFragPosLightSpace;
    vec4 spotLightFragPosLightSpace;
    vec3 tangentFragPos;
    vec3 tangentViewPos;
    mat3 TBN;
} vsOut;

void main() {
    vsOut.fragPos = vec3(model * vec4(aPos, 1.0f));
    vsOut.dirLightFragPosLightSpace = dirLightLightSpaceMatrix * vec4(vsOut.fragPos, 1.0f);
    vsOut.spotLightFragPosLightSpace = spotLightLightSpaceMatrix * vec4(vsOut.fragPos, 1.0f);
    vsOut.texCoord = aTexCoords;

    vec3 T = normalize(vec3(model * vec4(aTangent, 0.0f)));
    vec3 B = normalize(vec3(model * vec4(aBitangent, 0.0f)));
    vec3 N = normalize(vec3(model * vec4(aNormal, 0.0f)));
    vsOut.TBN = mat3(T, B, N);

    mat3 transposedTBN = transpose(vsOut.TBN);
    vsOut.tangentFragPos = transposedTBN * vec3(model * vec4(aPos, 1.0f));
    vsOut.tangentViewPos = transposedTBN * viewPos;

    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
