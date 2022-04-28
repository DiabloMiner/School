#version 330 core
out vec4 fragColor;

uniform vec3 lightColor;

void main() {
    vec3 color = vec3(1.0f) - exp(-lightColor * 0.01);
    fragColor = vec4(color, 1.0f);
}
