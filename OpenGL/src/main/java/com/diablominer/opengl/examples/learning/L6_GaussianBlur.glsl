#version 330 core
out vec4 fragmentColor;

in vec2 texCoords;

uniform sampler2D texture0;

uniform bool fbIndex;
uniform float weight[5] = float[5] (0.227027, 0.1945946, 0.1216216, 0.054054, 0.016216);
float linearWeights[3] = float[3] (weight[0], weight[1] + weight[2], weight[3] +  weight[4]);

float calculateLinearOffset(float offset1, float offset2, float weight1, float weight2) {
    return (offset1 * weight1 + offset2 * weight2) / (weight1 +  weight2);
}

void main() {
    float offsets[3];
    offsets[0] = 0.0f;

    vec2 texOffset = 1.0f / textureSize(texture0, 0);
    vec3 result = texture(texture0, texCoords).rgb * linearWeights[0];

    if (fbIndex) {
        offsets[1] = calculateLinearOffset(texOffset.x, (2 * texOffset.x), weight[1], weight[2]);
        offsets[2] = calculateLinearOffset((3 * texOffset.x), (4 * texOffset.x), weight[3], weight[4]);

        for(int i = 1; i < 3; ++i) {
            result += texture(texture0, texCoords + vec2(offsets[i], 0.0)).rgb * linearWeights[i];
            result += texture(texture0, texCoords - vec2(offsets[i], 0.0)).rgb * linearWeights[i];
        }
    } else {
        offsets[1] = calculateLinearOffset(texOffset.y, (2 * texOffset.y), weight[1], weight[2]);
        offsets[2] = calculateLinearOffset((3 * texOffset.y), (4 * texOffset.y), weight[3], weight[4]);

        for(int i = 1; i < 3; ++i) {
            result += texture(texture0, texCoords + vec2(0.0, offsets[i])).rgb * linearWeights[i];
            result += texture(texture0, texCoords - vec2(0.0, offsets[i])).rgb * linearWeights[i];
        }
    }
    fragmentColor = vec4(result, 1.0f);
}

