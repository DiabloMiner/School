#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;
layout (location = 3) in vec3 aTangent;
layout (location = 4) in vec3 aBitangent;

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
    mat4 lightSpaceMatrix;
};

uniform mat4 model;

out VS_OUT {
    vec3 fragPos;
    vec2 texCoord;
    vec4 fragPosLightSpace;
    mat3 TBN;
} vsOut;

void main() {
    vsOut.fragPos = vec3(model * vec4(aPos, 1.0f));
    vsOut.fragPosLightSpace = lightSpaceMatrix * vec4(vsOut.fragPos, 1.0f);
    vsOut.texCoord = aTexCoords;

    vec3 T = normalize(vec3(model * vec4(aTangent,   0.0)));
    vec3 B = normalize(vec3(model * vec4(aBitangent, 0.0)));
    vec3 N = normalize(vec3(model * vec4(aNormal,    0.0)));
    vsOut.TBN = mat3(T, B, N);

    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
