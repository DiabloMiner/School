#version 330 core
out vec4 fragmentColor;

in vec2 texCoords;

uniform sampler2D image;

uniform bool horizontal;
uniform float weight[5] = float[5] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

void main() {
    vec2 texOffset = 1.0f / textureSize(image, 0);
    vec3 result = texture(image, texCoords).rgb * weight[0];
    if (horizontal) {
        for(int i = 1; i < 5; ++i) {
            result += texture(image, texCoords + vec2(texOffset.x * i, 0.0)).rgb * weight[i];
            result += texture(image, texCoords - vec2(texOffset.x * i, 0.0)).rgb * weight[i];
        }
    } else {
        for(int i = 1; i < 5; ++i) {
            result += texture(image, texCoords + vec2(0.0, texOffset.y * i)).rgb * weight[i];
            result += texture(image, texCoords - vec2(0.0, texOffset.y * i)).rgb * weight[i];
        }
    }
    fragmentColor = vec4(result, 1.0f);
}
