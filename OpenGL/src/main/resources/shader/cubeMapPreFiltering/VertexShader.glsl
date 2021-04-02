#version 330
layout (location = 0) in vec3 aPos;

out VS_OUT {
    vec3 localPos;
} vsOut;

void main() {
    vsOut.localPos = aPos;
    gl_Position = vec4(vsOut.localPos, 1.0);
}
