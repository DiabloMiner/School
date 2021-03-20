package com.diablominer.opengl.examples.advancedlighting;

import com.diablominer.opengl.examples.modelloading.Texture;
import com.diablominer.opengl.examples.modelloading.Vertex;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Mesh {

    public List<Vertex> vertices;
    public List<Integer> indices;
    public List<Texture> textures;
    public int VAO, EBO;
    public IntBuffer indicesBuffer;

    public Mesh(List<Vertex> vertices, List<Integer> indices, List<Texture> textures) {
        this.vertices = vertices;
        this.indices = indices;
        this.textures = textures;

        setUpMesh();
    }

    private void setUpMesh() {
        FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.size() * 3);
        for (Vertex vertex : vertices) {
            FloatBuffer buffer = MemoryUtil.memAllocFloat(3);
            vertex.position.get(buffer);
            verticesBuffer.put(buffer);
        }
        verticesBuffer.flip();
        FloatBuffer normalBuffer = MemoryUtil.memAllocFloat(vertices.size() * 3);
        for (Vertex vertex : vertices) {
            FloatBuffer buffer = MemoryUtil.memAllocFloat(3);
            vertex.normal.get(buffer);
            normalBuffer.put(buffer);
        }
        normalBuffer.flip();
        FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(vertices.size() * 2);
        for (Vertex vertex : vertices) {
            FloatBuffer buffer = MemoryUtil.memAllocFloat(2);
            vertex.texCoords.get(buffer);
            texCoordsBuffer.put(buffer);
        }
        texCoordsBuffer.flip();
        indicesBuffer = MemoryUtil.memAllocInt(indices.size());
        indicesBuffer.put(indices.stream().mapToInt(i -> i).toArray());
        indicesBuffer.flip();

        VAO = GL33.glGenVertexArrays();
        int VBO = GL33.glGenBuffers(), NBO = GL33.glGenBuffers(), TBO = GL33.glGenBuffers();
        EBO = GL33.glGenBuffers();

        GL33.glBindVertexArray(VAO);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verticesBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, NBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, normalBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(1);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, TBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, texCoordsBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(2);

        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL33.GL_STATIC_DRAW);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        MemoryUtil.memFree(verticesBuffer);
        MemoryUtil.memFree(normalBuffer);
        MemoryUtil.memFree(texCoordsBuffer);
        GL33.glDeleteBuffers(VBO);
        GL33.glDeleteBuffers(NBO);
        GL33.glDeleteBuffers(TBO);
    }

    public void draw(int shaderProgram) {
        int diffuseCounter = 0;
        int specularCounter = 0;
        for (int i = 0; i < textures.size(); i++) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + i);
            int number = 0;
            String name = textures.get(i).type;
            if (name.equals("texture_diffuse")) {
                number = diffuseCounter++;
            } else if (name.equals("texture_specular")) {
                number = specularCounter++;
            }
            GL33.glUseProgram(shaderProgram);
            GL33.glUniform1i(GL33.glGetUniformLocation(shaderProgram, "material." + name + number), i);
            GL33.glUseProgram(0);

            GL33.glBindTexture(GL33.GL_TEXTURE_2D, textures.get(i).id);
        }
        GL33.glActiveTexture(GL33.GL_TEXTURE0);

        GL33.glUseProgram(shaderProgram);
        GL33.glBindVertexArray(VAO);

        GL33.glDrawElements(GL33.GL_TRIANGLES, indicesBuffer);

        GL33.glBindVertexArray(0);
        GL33.glUseProgram(0);
    }

    public void destroy() {
        MemoryUtil.memFree(indicesBuffer);

        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(EBO);

        GL33.glBindVertexArray(0);
        GL33.glDeleteVertexArrays(VAO);
    }

}
