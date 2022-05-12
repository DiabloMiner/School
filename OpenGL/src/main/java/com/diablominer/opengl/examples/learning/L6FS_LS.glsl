#version 330 core
layout (location = 0) out vec4 fragmentColor;
layout (location = 1) out vec4 brightColor;

uniform vec3 lightColor;

void main() {
    vec3 color = vec3(1.0f) - exp(-lightColor * 0.01);
    fragmentColor = vec4(color, 1.0f);

    float brightness = dot(lightColor.rgb, vec3(0.2126f, 0.7152f, 0.0722f));
    if (brightness > 1.5f) {
        brightColor = vec4(fragmentColor.rgb, 1.0f);
    } else {
        brightColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
}
