package com.diablominer.opengl.render.renderables;

import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.render.textures.Texture;
import com.diablominer.opengl.utils.BufferUtil;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Mesh {

    public float[] vertices;
    public float[] normals;
    public float[] texCoords;
    public int[] indices;
    public List<Texture> textures;

    private int VAO, VBO, NBO, TBO, EBO;
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
        FloatBuffer normalBuffer = BufferUtil.createBuffer(normals);
        FloatBuffer textureCoordinateBuffer = BufferUtil.createBuffer(texCoords);

        // Vertex array and buffers are generated
        VAO = GL33.glGenVertexArrays();
        VBO = GL33.glGenBuffers();
        NBO = GL33.glGenBuffers();
        TBO = GL33.glGenBuffers();
        EBO = GL33.glGenBuffers();

        // The vertex array object is bound
        GL33.glBindVertexArray(VAO);

        // The element buffer object is bound and data is provided to it
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL33.GL_STATIC_DRAW);

        // The vertex buffer object is bound, all vertex data is provided to it and the data is pointed to the vertex shader
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verticesBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, NBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, normalBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, TBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, textureCoordinateBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 2 * Float.BYTES, 0);

        // The vertex array object, the array buffer and the element array buffer is bound to zero, resetting it to its normal state
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        // The buffers are destroyed
        BufferUtil.destroyBuffer(verticesBuffer);
        BufferUtil.destroyBuffer(normalBuffer);
        BufferUtil.destroyBuffer(textureCoordinateBuffer);
    }

    public void draw(ShaderProgram shaderProgram) {
        int diffuseCounter = 1;
        int specularCounter = 1;
        for (Texture currentTexture : textures) {
            int number = 0;
            String name = currentTexture.type;
            if (name.equals("texture_diffuse")) {
                number = diffuseCounter++;
            } else if (name.equals("texture_specular")) {
                number = specularCounter++;
            }
            if (number != 0) {
                currentTexture.bind();
                shaderProgram.setUniform1I("material." + name + number, Texture.getIndexForTexture(currentTexture));
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

        // Unbind the shaderProgram and all textures
        shaderProgram.unbind();
    }

    public void destroy() {
        // Destroy the indices buffer
        BufferUtil.destroyBuffer(indicesBuffer);

        // Delete all buffer objects
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(VBO);
        GL33.glDeleteBuffers(NBO);
        GL33.glDeleteBuffers(TBO);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(EBO);

        // Delete the VAO
        GL33.glBindVertexArray(0);
        GL33.glDeleteVertexArrays(VAO);
    }

}
