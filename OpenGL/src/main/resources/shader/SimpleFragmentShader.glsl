#version 330 core
out vec4 fragmentColor;

in vec2 texCoords;

uniform sampler2D screenTexture;
uniform float exposure;

const float offset = 1.0f / 300.0f;

vec3 toneMapping(vec3 hdrColor) {
    vec3 result = vec3(1.0f) - exp(-hdrColor * exposure);
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
        sampleTex[i] = toneMapping(vec3(texture(screenTexture, texCoords.xy + offsets[i])));
    }
    vec3 col = vec3(0.0);
    for(int i = 0; i < 9; i++)
        col += sampleTex[i] * kernel[i];

    fragmentColor = vec4(toneMapping(texture(screenTexture, texCoords).xyz), 1.0f);
}