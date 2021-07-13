#version 330 core
out vec4 fragmentColor;

in float color;

void main() {
    fragmentColor = vec4(0.0f, 0.01f * color, 0.0f, 1.0f);
}
