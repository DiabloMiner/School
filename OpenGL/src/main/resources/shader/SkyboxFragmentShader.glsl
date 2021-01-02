#version 330 core
out vec4 fragColor;

in vec3 texCoords;

uniform samplerCube cubeMap;

void main()
{
    fragColor = texture(cubeMap, texCoords);
}
