#version 400 core
layout (triangles, invocations = 6) in;
layout (triangle_strip, max_vertices = 3) out;

uniform mat4 shadowMatrices[6];

out vec4 fragPos;
out vec2 texCoords;

in VS_OUT {
    vec2 texCoords;
} gsIn[];

void main() {
    for (int i = 0; i < 3; ++i) {
        fragPos = gl_in[i].gl_Position;
        texCoords = gsIn[i].texCoords;
        gl_Position = shadowMatrices[gl_InvocationID] * fragPos;
        gl_Layer = gl_InvocationID;
        EmitVertex();
    }
    EndPrimitive;
}
