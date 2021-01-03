#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoord;

out VS_OUT {
    vec2 texCoords;
    vec3 normal;
    vec3 fragPos;
} vsOut;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    vsOut.fragPos = vec3(model * vec4(aPos, 1.0f));
    vsOut.normal = mat3(transpose(inverse(model))) * aNormal;
    vsOut.texCoords = aTexCoord;
}
