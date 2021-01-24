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

out VS_OUT {
    vec3 normal;
    vec3 fragPos;
    vec2 texCoord;
    vec4 fragPosLightSpace;
} vsOut;

void main() {
    vsOut.fragPos = vec3(model * vec4(aPos, 1.0f));
    vsOut.normal = mat3(transpose(inverse(model))) * aNormal;
    vsOut.fragPosLightSpace = lightSpaceMatrix * vec4(vsOut.fragPos, 1.0f);
    vsOut.texCoord = aTexCoords;

    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
