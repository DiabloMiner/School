#version 330 core
out vec4 fragColor;

in vec2 outTexCoords;

uniform sampler2D texture0;
uniform sampler2D texture1;

vec3 toneMapping(vec3 hdrColor) {
    vec3 result = hdrColor / (hdrColor + vec3(1.0f));
    return result;
}

void main() {
    vec3 normalColor = texture(texture0, outTexCoords).xyz;
    vec3 bloomColor = texture(texture1, outTexCoords).xyz;
    normalColor += bloomColor;
    normalColor = toneMapping(normalColor);
    fragColor = vec4(normalColor, 1.0f);
}
