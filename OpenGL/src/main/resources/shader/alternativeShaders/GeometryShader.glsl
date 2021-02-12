#version 400 core
layout (triangles, invocations = 6) in;
layout (triangle_strip, max_vertices = 3) out;

layout (std140) uniform environmentMappingMatrices {
    mat4[6] environmentMappingViewMatrices;
    mat4 environmentMappingProjectionMatrix;
};

out vec3 fragPos;
out vec2 texCoord;
out vec4 dirLightFragPosLightSpace;
out vec4 spotLightFragPosLightSpace;
out mat3 TBN;

out int gl_Layer;

in VS_OUT {
    vec3 fragPos;
    vec2 texCoord;
    vec4 dirLightFragPosLightSpace;
    vec4 spotLightFragPosLightSpace;
    mat3 TBN;
} gsIn[];

void main() {
    for (int i = 0; i < 3; i++) {
        gl_Position = environmentMappingProjectionMatrix * (environmentMappingViewMatrices[gl_InvocationID]) * vec4(gsIn[i].fragPos, 1.0f);

        fragPos = gsIn[i].fragPos;
        TBN = gsIn[i].TBN;
        texCoord = gsIn[i].texCoord;
        dirLightFragPosLightSpace = gsIn[i].dirLightFragPosLightSpace;
        spotLightFragPosLightSpace = gsIn[i].spotLightFragPosLightSpace;

        gl_Layer = gl_InvocationID;
        EmitVertex();
    }
    EndPrimitive();
}
