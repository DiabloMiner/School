#version 400 core

layout (triangles, invocations = 6) in;
layout (triangle_strip, max_vertices = 3) out;

layout (std140) uniform environmentMappingMatrices {
    mat4[6] environmentMappingViewMatrices;
    mat4 environmentMappingProjectionMatrix;
};

out vec3 texCoords;
out int gl_Layer;

in VS_OUT {
    vec3 texCoords;
    vec4 pos;
} gsIn[];

void main() {
    for (int i = 0; i < 3; i++) {
        vec4 position = environmentMappingProjectionMatrix * mat4(mat3(environmentMappingViewMatrices[gl_InvocationID])) * gsIn[i].pos;
        gl_Position = position.xyww;
        texCoords = gsIn[i].texCoords;
        gl_Layer = gl_InvocationID;
        EmitVertex();
    }
    EndPrimitive();
}
