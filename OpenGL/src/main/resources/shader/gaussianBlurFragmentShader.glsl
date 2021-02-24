#version 330 core
out vec4 fragmentColor;

in vec2 texCoords;

uniform sampler2D image;

uniform bool horizontal;
uniform float weight[5] = float[5] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);

float calculateLinearWeight(float weight1, float weight2) {
    return (weight1 + weight2);
}

float calculateLinearOffset(float offset1, float offset2, float weight1, float weight2) {
    return (offset1 * weight1 + offset2 * weight2) / (calculateLinearWeight(weight1, weight2));
}

void main() {
    float offsets[3];
    offsets[0] = 0.0f;

    float weights[3];
    weights[0] = weight[0];
    weights[1] = calculateLinearWeight(weight[1], weight[2]);
    weights[2] = calculateLinearWeight(weight[3], weight[4]);

    vec2 texOffset = 1.0f / textureSize(image, 0);
    vec3 result = texture(image, texCoords).rgb * weights[0];

    if (horizontal) {
        offsets[1] = calculateLinearOffset(texOffset.x, (2 * texOffset.x), weight[1], weight[2]);
        offsets[2] = calculateLinearOffset((3 * texOffset.x), (4 * texOffset.x), weight[3], weight[4]);

        for(int i = 1; i < 3; ++i) {
            result += texture(image, texCoords + vec2(offsets[i], 0.0)).rgb * weights[i];
            result += texture(image, texCoords - vec2(offsets[i], 0.0)).rgb * weights[i];
        }
    } else {
        offsets[1] = calculateLinearOffset(texOffset.y, (2 * texOffset.x), weight[1], weight[2]);
        offsets[2] = calculateLinearOffset((3 * texOffset.y), (4 * texOffset.x), weight[3], weight[4]);

        for(int i = 1; i < 3; ++i) {
            result += texture(image, texCoords + vec2(0.0, offsets[i])).rgb * weights[i];
            result += texture(image, texCoords - vec2(0.0, offsets[i])).rgb * weights[i];
        }
    }
    fragmentColor = vec4(result, 1.0f);
}
