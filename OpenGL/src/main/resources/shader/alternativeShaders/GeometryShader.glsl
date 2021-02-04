#version 400 core
layout (triangles, invocations = 6) in;
layout (triangle_strip, max_vertices = 3) out;

layout (std140) uniform environmentMappingMatrices {
    mat4[6] environmentMappingViewMatrices;
    mat4 environmentMappingProjectionMatrix;
};

out vec3 fragPos;
out vec2 texCoord;
out vec4 fragPosLightSpace;
out vec3 vPos;

out vec3 dirLightDirection;
out vec3 pointLightPosition;
out vec3 spotLightPosition;
out vec3 spotLightDirection;

out int gl_Layer;

in VS_OUT {
    vec3 fragPos;
    vec2 texCoord;
    vec4 fragPosLightSpace;
    vec3 vPos;

    vec3 dirLightDirection;
    vec3 pointLightPosition;
    vec3 spotLightPosition;
    vec3 spotLightDirection;
} gsIn[];

void main() {
    for (int i = 0; i < 3; i++) {
        gl_Position = environmentMappingProjectionMatrix * (environmentMappingViewMatrices[gl_InvocationID]) * vec4(gsIn[i].fragPos, 1.0f);

        fragPos = gsIn[i].fragPos;
        vPos = gsIn[i].vPos;
        texCoord = gsIn[i].texCoord;
        fragPosLightSpace = gsIn[i].fragPosLightSpace;

        dirLightDirection = gsIn[i].dirLightDirection;
        pointLightPosition = gsIn[i].pointLightPosition;
        spotLightPosition = gsIn[i].spotLightPosition;
        spotLightDirection = gsIn[i].spotLightDirection;

        gl_Layer = gl_InvocationID;
        EmitVertex();
    }
    EndPrimitive();
}
