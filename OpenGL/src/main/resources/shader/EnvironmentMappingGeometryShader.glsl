#version 400 core

layout (triangles, invocations = 6) in;
layout (triangle_strip, max_vertices = 3) out;

out GS_OUT {
    vec3 normal;
    vec3 fragPos;
    vec2 texCoord;
} gsOut;

out int gl_Layer;

in VS_OUT {
    vec3 normal;
    vec3 fragPos;
    vec2 texCoord;
} gsIn[];

uniform mat4[6] environmentMappingViewMatrices;
uniform mat4 environmentMappingProjectionMatrix;

void main() {
    for (int i = 0; i < 3; i++) {
        gl_Position = environmentMappingProjectionMatrix * environmentMappingViewMatrices[gl_InvocationID] * vec4(gsIn[i].fragPos, 1.0f);
        gsOut.normal = gsIn[i].normal;
        gsOut.fragPos = gsIn[i].fragPos;
        gsOut.texCoord = gsIn[i].texCoord;
        gl_Layer = gl_InvocationID;
        EmitVertex();
    }

    EndPrimitive();
}
