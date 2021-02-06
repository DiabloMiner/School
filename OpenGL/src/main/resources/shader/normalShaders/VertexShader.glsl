#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;
layout (location = 2) in vec2 aTexCoords;
layout (location = 3) in vec3 aTangent;
layout (location = 4) in vec3 aBitangent;

struct DirectionaLight {
    vec3 direction;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    sampler2D shadowMap;
};

struct PointLight {
    vec3 position;

    float constant;
    float linear;
    float quadratic;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    samplerCube shadowMap;
    float farPlane;
};

struct SpotLight {
    vec3 position;
    vec3 direction;

    float constant;
    float linear;
    float quadratic;

    float cutOff;
    float outerCutOff;

    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    samplerCube shadowMap;
    float farPlane;
};

layout (std140) uniform Matrices {
    mat4 view;
    mat4 projection;
    mat4 lightSpaceMatrix;
};

out vec2 texCoord;
out vec4 fragPosLightSpace;
out vec3 fragPos;
out vec3 realFragPos;
out vec3 vPos;

out vec3 dirLightDirection;
out vec3 pointLightPosition;
out vec3 spotLightPosition;
out vec3 spotLightDirection;

uniform mat4 model;
uniform vec3 viewPos;
uniform DirectionaLight dirLight;
uniform PointLight pointLight;
uniform SpotLight spotLight;

void main() {
    fragPos = vec3(model * vec4(aPos, 1.0f));
    fragPosLightSpace = lightSpaceMatrix * vec4(fragPos, 1.0f);
    texCoord = aTexCoords;

    vec3 T = normalize(vec3(model * vec4(aTangent,   0.0)));
    vec3 B = normalize(vec3(model * vec4(aBitangent, 0.0)));
    vec3 N = normalize(vec3(model * vec4(aNormal,    0.0)));
    mat3 TBN = mat3(T, B, N);

    fragPos = TBN * fragPos;
    vPos = TBN * viewPos;

    dirLightDirection = TBN * dirLight.direction;
    pointLightPosition = TBN * pointLight.position;
    spotLightPosition = TBN * spotLight.position;
    spotLightDirection = TBN * spotLight.direction;

    gl_Position = projection * view * model * vec4(aPos, 1.0f);
}
