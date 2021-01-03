#version 330 core
layout (location = 0) in vec2 aPos;
layout (location = 1) in vec3 aColor;

out VS_OUT {
    vec3 color;
} vsOut;

void main() {
    vsOut.color = aColor;
    gl_Position = vec4(aPos.x, aPos.y, 0.0f, 1.0f);
}
