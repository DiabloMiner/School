package com.diablominer.opengl.render;

import com.diablominer.opengl.utils.BufferUtil;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;

public class Mesh {

    public float[] vertices;
    public float[] normals;
    public float[] texCoords;
    public int[] indices;
    public List<Texture> textures;

    private int VAO, VBO, EBO;
    private IntBuffer indicesBuffer;

    public Mesh(float[] vertices, float[] normals, float[] texCoords, int[] indices, List<Texture> textures) {
        this.vertices = vertices;
        this.normals = normals;
        this.texCoords = texCoords;
        this.indices = indices;
        this.textures = textures;

        setUpMesh();
    }

    private void setUpMesh() {
        // Buffers are created
        indicesBuffer = BufferUtil.createBuffer(indices);
        FloatBuffer verticesBuffer = BufferUtil.createBuffer(vertices);

        // Generate Vertex Array and Buffers
        VAO = GL33.glGenVertexArrays();
        VBO = GL33.glGenBuffers();
        EBO = GL33.glGenBuffers();

        // Bind Vertex Array and bind Vertex Buffer Object to GL_ARRAY_BUFFER
        GL33.glBindVertexArray(VAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);

        // Data is provided to GL_ARRAY_BUFFER and subsequently also to the Vertex Buffer Object
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verticesBuffer, GL33.GL_STATIC_DRAW);

        // Element Buffer Object is bound and data is provided for it
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, BufferUtil.createBuffer(indices), GL33.GL_STATIC_DRAW);

        // Stride is the product of how many components a vertex attribute has multiplied with the byte size of the number object that is used (e.g.:float)
        // These products are calculated for every vertex attribute and the sum of it is the stride
        int stride = (3 * Float.BYTES) + (3 * Float.BYTES) + (2 * Float.BYTES);
        // Vertex positions are set
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, stride, 0);
        // Vertex normals are set
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, stride, (3 * Float.BYTES));
        // Vertex texture coordinates are set
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, stride, (3 * Float.BYTES) + (3 * Float.BYTES));

        // GL_ARRAY_BUFFER, GL_ELEMENT_ARRAY_BUFFER and the vertex array object are bound to zero, resetting them to their normal state
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        // The vertices buffer is destroyed
        BufferUtil.destroyBuffer(verticesBuffer);
    }

    public void draw(ShaderProgram shaderProgram) {
        int diffuseCounter = 1;
        int specularCounter = 1;
        for (Texture currentTexture : textures) {
            String number = "";
            String name = currentTexture.type;
            if (name.equals("texture_diffuse")) {
                number = String.valueOf(diffuseCounter++);
            } else if (name.equals("texture_specular")) {
                number = String.valueOf(specularCounter++);
            }
            currentTexture.bind();
            if (!number.equals("") && Texture.getIndexForTexture(currentTexture) >= 0) {
                shaderProgram.setUniform1F("material." + name + number, Texture.getIndexForTexture(currentTexture));
            }
        }

        // Bind the shaderProgram
        shaderProgram.bind();

        // Bind vertex array object and enable vertex attribute pointers
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glEnableVertexAttribArray(2);

        // Draw the elements
        GL33.glDrawElements(GL33.GL_TRIANGLES, indicesBuffer);

        // Unbind the vertex array object and disable vertex attribute pointers
        GL33.glBindVertexArray(0);
        GL33.glDisableVertexAttribArray(2);
        GL33.glDisableVertexAttribArray(1);
        GL33.glDisableVertexAttribArray(0);
    }

    public void destroy() {
        // Destroy the indices buffer
        BufferUtil.destroyBuffer(indicesBuffer);

        // Delete the VBO
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(VBO);

        // Delete EBO
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(EBO);

        // Delete the VAO
        GL33.glBindVertexArray(0);
        GL33.glDeleteVertexArrays(VAO);
    }

}
