package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mesh {

    public static final int vertexSize = 3;
    public static final int normalSize = 3;
    public static final int texCoordSize = 2;
    public static final int tangentSize = 3;
    public static final int bitangentSize = 3;

    protected float[] vertices;
    protected float[] normals;
    protected float[] texCoords;
    protected float[] tangents;
    protected float[] biTangents;
    protected int[] indices;
    protected List<Texture2D> texture2DS;
    protected List<Integer> vertexAttributeSizes;
    protected VAO vao;

    public Mesh() {
        this.vertices = new float[0];
        this.normals = new float[0];
        this.texCoords = new float[0];
        this.tangents = new float[0];
        this.biTangents = new float[0];
        this.indices = new int[0];
        this.texture2DS = new ArrayList<>();
        this.vertexAttributeSizes = new ArrayList<>();
    }

    public Mesh(float[] vertices, float[] normals, float[] texCoords, float[] tangents, float[] biTangents, int[] indices, List<Texture2D> texture2DS) {
        this.vertices = vertices;
        this.normals = normals;
        this.texCoords = texCoords;
        this.tangents = tangents;
        this.biTangents = biTangents;
        this.indices = indices;
        this.texture2DS = texture2DS;
        this.vertexAttributeSizes = new ArrayList<>();
        setUpMesh();
    }

    private void setUpMesh() {
        ArrayList<float[]> vertexAttributeArrays = new ArrayList<>(Arrays.asList(vertices, normals, texCoords, tangents, biTangents));
        vertexAttributeSizes.addAll(Arrays.asList(vertexSize, normalSize, texCoordSize, tangentSize, bitangentSize));
        vao = new MeshVAO(vertexAttributeArrays, vertexAttributeSizes, indices, GL33.GL_STATIC_DRAW);
    }

    public void draw(ShaderProgram shaderProgram) {
        int colorCounter = 1;
        int normalCounter = 1;
        int displacementCounter = 1;
        int roughnessCounter = 1;
        int metallicCounter = 1;
        int aoCounter = 1;
        int reflectionCounter = 1;
        for (Texture2D currentTexture2D : texture2DS) {
            int number = 0;
            String name = currentTexture2D.type;
            switch (name) {
                case "texture_color":
                    number = colorCounter++;
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
                currentTexture2D.bind();
                shaderProgram.setUniform1I("material." + name + number, currentTexture2D.index);
            }
        }

        // Bind the shaderProgram
        shaderProgram.bind();

        // Let the VAO draw its contents
        vao.draw();

        // Unbind the shaderProgram
        ShaderProgram.unbind();

        // Unbind all twoDimensionalTextures used for this mesh
        for (Texture2D currentTexture2D : texture2DS) {
            int number = 0;
            String name = currentTexture2D.type;
            switch (name) {
                case "texture_color":
                    number = colorCounter;
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
                currentTexture2D.unbind();
                shaderProgram.setUniform1I("material." + name + number, currentTexture2D.index);
            }
        }
    }

    public void destroy() {
        // Unbind all VAOs and Buffer Objects
        VAO.unbind();
        VertexBufferObject.unbind();
        ElementBufferObject.unbind();

        // Destroy VAO
        vao.destroy();
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getTexCoords() {
        return texCoords;
    }

    public float[] getTangents() {
        return tangents;
    }

    public float[] getBiTangents() {
        return biTangents;
    }

    public int[] getIndices() {
        return indices;
    }
}
