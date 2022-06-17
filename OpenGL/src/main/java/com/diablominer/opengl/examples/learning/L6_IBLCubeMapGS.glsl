#version 400

layout (triangles, invocations = 6) in;
layout (triangle_strip, max_vertices = 3) out;

uniform mat4[6] viewMatrices;
uniform mat4 projection;

in VS_OUT {
    vec3 localPos;
} gsIn[];

out vec3 localPos;
out int gl_Layer;

void main() {
    for (int i = 0; i < 3; i++) {
        gl_Position = projection * viewMatrices[gl_InvocationID] * vec4(gsIn[i].localPos, 1.0f);

        localPos = gsIn[i].localPos;

        gl_Layer = gl_InvocationID;
        EmitVertex();
    }
    EndPrimitive();
}
