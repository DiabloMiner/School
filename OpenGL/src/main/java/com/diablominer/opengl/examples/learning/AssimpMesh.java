package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AssimpMesh extends Mesh {

    public static final int vertexIndex = 0;
    public static final int normalIndex = 1;
    public static final int texCoordIndex = 2;
    public static final int tangentIndex = 3;
    public static final int bitangentIndex = 4;

    public static final int vertexSize = 3;
    public static final int normalSize = 3;
    public static final int texCoordSize = 2;
    public static final int tangentSize = 3;
    public static final int bitangentSize = 3;


    protected int[] indices;
    protected List<ModelTexture2D> texture2DS;

    public AssimpMesh() {
        super();
        floatData = new ArrayList<>(Arrays.asList(new float[0], new float[0], new float[0], new float[0], new float[0]));
        this.indices = new int[0];
        this.texture2DS = new ArrayList<>();
        this.vertexAttributeSizes = new ArrayList<>();
    }

    public AssimpMesh(float[] vertices, float[] normals, float[] texCoords, float[] tangents, float[] biTangents, int[] indices, List<ModelTexture2D> texture2DS) {
        super();
        floatData = new ArrayList<>(Arrays.asList(vertices, normals, texCoords, tangents, biTangents));
        this.indices = indices;
        this.texture2DS = texture2DS;
        this.vertexAttributeSizes = new ArrayList<>();
        setUpMesh();
    }

    void setUpMesh() {
        vertexAttributeSizes.addAll(Arrays.asList(vertexSize, normalSize, texCoordSize, tangentSize, bitangentSize));
        vao = new MeshVAO(floatData, vertexAttributeSizes, indices, GL33.GL_STATIC_DRAW);
    }

    public void draw(ShaderProgram shaderProgram) {
        int colorCounter = 1;
        int normalCounter = 1;
        int displacementCounter = 1;
        int roughnessCounter = 1;
        int metallicCounter = 1;
        int aoCounter = 1;
        int reflectionCounter = 1;
        for (ModelTexture2D currentTexture2D : texture2DS) {
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
                shaderProgram.setUniform1I("material." + name + number, currentTexture2D.getIndex());
            }
        }

        // Bind the shaderProgram
        shaderProgram.bind();

        // Let the VAO draw its contents
        vao.draw();

        // Unbind the shaderProgram
        ShaderProgram.unbind();

        // Unbind all twoDimensionalTextures used for this mesh
        for (ModelTexture2D currentTexture2D : texture2DS) {
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
                shaderProgram.setUniform1I("material." + name + number, currentTexture2D.getIndex());
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
        return floatData.get(vertexIndex);
    }

    public float[] getNormals() {
        return floatData.get(normalIndex);
    }

    public float[] getTexCoords() {
        return floatData.get(texCoordIndex);
    }

    public float[] getTangents() {
        return floatData.get(tangentIndex);
    }

    public float[] getBiTangents() {
        return floatData.get(bitangentIndex);
    }

    public int[] getIndices() {
        return indices;
    }

    public VAO getVao() {
        return vao;
    }

    public void setVao(VAO vao) {
        this.vao = vao;
    }

}
