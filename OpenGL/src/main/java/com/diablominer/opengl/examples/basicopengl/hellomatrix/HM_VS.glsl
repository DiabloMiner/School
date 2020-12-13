#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTexCoord;

out vec2 texCoord;

uniform mat4 matrix;

void main()
{
    gl_Position = matrix * vec4(aPos, 1.0);
    texCoord = aTexCoord;
}