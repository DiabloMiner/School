package com.diablominer.opengl.render.renderables;

import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.render.textures.TwoDimensionalTexture;
import com.diablominer.opengl.utils.BufferUtil;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.List;

public class Mesh {

    public float[] vertices;
    public float[] normals;
    public float[] texCoords;
    public float[] tangents;
    public float[] biTangents;
    public int[] indices;
    public List<TwoDimensionalTexture> twoDimensionalTextures;

    private int VAO, VBO, NBO, TBO, EBO, tangentBufferObject, biTangentBufferObject;
    private IntBuffer indicesBuffer;

    public Mesh(float[] vertices, float[] normals, float[] texCoords, float[] tangents, float[] biTangents,int[] indices, List<TwoDimensionalTexture> twoDimensionalTextures) {
        this.vertices = vertices;
        this.normals = normals;
        this.texCoords = texCoords;
        this.tangents = tangents;
        this.biTangents = biTangents;
        this.indices = indices;
        this.twoDimensionalTextures = twoDimensionalTextures;
        setUpMesh();
    }

    private void setUpMesh() {
        // Buffers are created
        indicesBuffer = BufferUtil.createBuffer(indices);
        FloatBuffer verticesBuffer = BufferUtil.createBuffer(vertices);
        FloatBuffer normalBuffer = BufferUtil.createBuffer(normals);
        FloatBuffer textureCoordinateBuffer = BufferUtil.createBuffer(texCoords);
        FloatBuffer tangentBuffer = BufferUtil.createBuffer(tangents);
        FloatBuffer biTangentBuffer = BufferUtil.createBuffer(biTangents);

        // Vertex array and buffers are generated
        VAO = GL33.glGenVertexArrays();
        VBO = GL33.glGenBuffers();
        NBO = GL33.glGenBuffers();
        TBO = GL33.glGenBuffers();
        EBO = GL33.glGenBuffers();
        tangentBufferObject = GL33.glGenBuffers();
        biTangentBufferObject = GL33.glGenBuffers();

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

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, tangentBufferObject);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, tangentBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(3, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, biTangentBufferObject);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, biTangentBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(4, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);

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
        int normalCounter = 1;
        int displacementCounter = 1;
        int roughnessCounter = 1;
        int metallicCounter = 1;
        int aoCounter = 1;
        int reflectionCounter = 1;
        for (TwoDimensionalTexture currentTwoDimensionalTexture : twoDimensionalTextures) {
            int number = 0;
            String name = currentTwoDimensionalTexture.type;
            switch (name) {
                case "texture_diffuse":
                    number = diffuseCounter++;
                    break;
                case "texture_normal":
                    number = normalCounter++;
                    break;
                case "texture_displacement":
                    number = displacementCounter++;
                    break;
                case "texture_roughness":
                    number = roughnessCounter++;
                    break;
                case "texture_metallic":
                    number = metallicCounter++;
                    break;
                case "texture_ao":
                    number = aoCounter++;
                    break;
                case "texture_reflection":
                    number = reflectionCounter++;
                    break;
            }
            if (number != 0) {
                currentTwoDimensionalTexture.bind();
                shaderProgram.setUniform1I("material." + name + number, currentTwoDimensionalTexture.index);
            }
        }

        // Bind the shaderProgram
        shaderProgram.bind();

        // Bind vertex array object and enable vertex attribute pointers
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glEnableVertexAttribArray(2);
        GL33.glEnableVertexAttribArray(3);
        GL33.glEnableVertexAttribArray(4);

        // Draw the elements
        GL33.glDrawElements(GL33.GL_TRIANGLES, indicesBuffer);

        // Unbind the vertex array object and disable vertex attribute pointers
        GL33.glDisableVertexAttribArray(4);
        GL33.glDisableVertexAttribArray(3);
        GL33.glDisableVertexAttribArray(2);
        GL33.glDisableVertexAttribArray(1);
        GL33.glDisableVertexAttribArray(0);
        GL33.glBindVertexArray(0);

        // Unbind the shaderProgram
        shaderProgram.unbind();

        // Unbind all twoDimensionalTextures used for this mesh
        for (TwoDimensionalTexture currentTwoDimensionalTexture : twoDimensionalTextures) {
            int number = 0;
            String name = currentTwoDimensionalTexture.type;
            switch (name) {
                case "texture_diffuse":
                    number = diffuseCounter;
                    break;
                case "texture_normal":
                    number = normalCounter;
                    break;
                case "texture_displacement":
                    number = displacementCounter;
                    break;
                case "texture_roughness":
                    number = roughnessCounter;
                    break;
                case "texture_metallic":
                    number = metallicCounter;
                    break;
                case "texture_ao":
                    number = aoCounter;
                    break;
                case "texture_reflection":
                    number = reflectionCounter;
                    break;
            }
            if (number != 0) {
                currentTwoDimensionalTexture.unbind();
                shaderProgram.setUniform1I("material." + name + number, currentTwoDimensionalTexture.index);
            }
        }
    }

    public void destroy() {
        // Destroy the indices buffer
        BufferUtil.destroyBuffer(indicesBuffer);

        // Delete all buffer objects
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(VBO);
        GL33.glDeleteBuffers(NBO);
        GL33.glDeleteBuffers(TBO);
        GL33.glDeleteBuffers(tangentBufferObject);
        GL33.glDeleteBuffers(biTangentBufferObject);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(EBO);

        // Delete the VAO
        GL33.glBindVertexArray(0);
        GL33.glDeleteVertexArrays(VAO);
    }

}
