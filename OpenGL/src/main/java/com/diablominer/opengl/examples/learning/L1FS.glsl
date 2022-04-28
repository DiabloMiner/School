#version 330 core
out vec4 fragColor;

in vec3 outColor;

void main() {
    fragColor = vec4(outColor.x, outColor.y, outColor.z, 1.0f);
}