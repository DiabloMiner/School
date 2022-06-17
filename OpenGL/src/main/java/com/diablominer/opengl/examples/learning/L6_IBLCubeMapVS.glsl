#version 330
layout (location = 0) in vec3 inPos;

out vec3 localPos;

out VS_OUT {
    vec3 localPos;
} vsOut;

void main() {
    vsOut.localPos = inPos;
    gl_Position = vec4(localPos, 1.0);
}
