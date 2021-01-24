#version 400 core

layout (triangles, invocations = 6) in;
layout (triangle_strip, max_vertices = 3) out;

layout (std140) uniform environmentMappingMatrices {
    mat4[6] environmentMappingViewMatrices;
    mat4 environmentMappingProjectionMatrix;
};

out vec3 normal;
out vec3 fragPos;
out vec2 texCoord;
out vec4 fragPosLightSpace;
out int gl_Layer;

in VS_OUT {
    vec3 normal;
    vec3 fragPos;
    vec2 texCoord;
    vec4 fragPosLightSpace;
} gsIn[];

void main() {
    for (int i = 0; i < 3; i++) {
        gl_Position = environmentMappingProjectionMatrix * (environmentMappingViewMatrices[gl_InvocationID]) * vec4(gsIn[i].fragPos, 1.0f);
        fragPos = gsIn[i].fragPos;
        normal = gsIn[i].normal;
        texCoord = gsIn[i].texCoord;
        fragPosLightSpace = gsIn[i].fragPosLightSpace;
        gl_Layer = gl_InvocationID;
        EmitVertex();
    }
    EndPrimitive();
}
