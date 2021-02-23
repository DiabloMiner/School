#version 330 core
layout (location = 0) out vec4 fragmentColor;
layout (location = 1) out vec4 brightColor;

uniform vec3 color;

void main() {
    fragmentColor = vec4(color, 1.0f);

    float brightness = dot(fragmentColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    if (brightness > 1.0f) {
        brightColor = vec4(fragmentColor.rgb, 1.0f);
    } else {
        brightColor = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
}
