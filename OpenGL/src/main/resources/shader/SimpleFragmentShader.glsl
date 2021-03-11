#version 330 core
out vec4 fragmentColor;

in vec2 texCoords;

uniform sampler2D screenTexture;
uniform sampler2D bloomBlur;

const float offset = 1.0f / 300.0f;

vec3 toneMapping(vec3 hdrColor) {
    vec3 result = hdrColor / (hdrColor + vec3(1.0));
    return result;
}

void main() {
    vec2 offsets[9] = vec2[](
        vec2(-offset,  offset),
        vec2( 0.0f,    offset),
        vec2( offset,  offset),
        vec2(-offset,  0.0f),
        vec2( 0.0f,    0.0f),
        vec2( offset,  0.0f),
        vec2(-offset, -offset),
        vec2( 0.0f,   -offset),
        vec2( offset, -offset)
    );

    float kernel[9] = float[](
       5.3,  0, -2,
         0,  0,  0,
        -1,  0, -3
    );

    vec3 sampleTex[9];
    for(int i = 0; i < 9; i++) {
        sampleTex[i] = texture(screenTexture, texCoords.xy + offsets[i]).xyz;
        sampleTex[i] += texture(bloomBlur, texCoords.xy + offsets[i]).xyz;
        sampleTex[i] = toneMapping(sampleTex[i]);
    }
    vec3 col = vec3(0.0);
    for(int i = 0; i < 9; i++)
        col += sampleTex[i] * kernel[i];

    vec3 normalColor = texture(screenTexture, texCoords).xyz;
    normalColor += texture(bloomBlur, texCoords).xyz;
    normalColor = toneMapping(normalColor);
    fragmentColor = vec4(normalColor, 1.0f);
}