package com.diablominer.opengl.examples.learning;

import java.util.*;

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
    protected int colorCounter, normalCounter, displacementCounter, roughnessCounter, metallicCounter, aoCounter, reflectionCounter;
    protected List<Map.Entry<String, ModelTexture2D>> textureUniforms;

    public AssimpMesh() {
        super();
        this.floatData = new ArrayList<>(Arrays.asList(new float[0], new float[0], new float[0], new float[0], new float[0]));
        this.indices = new int[0];
        this.texture2DS = new ArrayList<>();
        this.vertexAttributeSizes = new ArrayList<>();
        this.textureUniforms = new ArrayList<>();
    }

    public AssimpMesh(float[] vertices, float[] normals, float[] texCoords, float[] tangents, float[] biTangents, int[] indices, List<ModelTexture2D> texture2DS) {
        super();
        this.floatData = new ArrayList<>(Arrays.asList(vertices, normals, texCoords, tangents, biTangents));
        this.indices = indices;
        this.texture2DS.addAll(texture2DS);
        this.vertexAttributeSizes = new ArrayList<>();
        this.textureUniforms = new ArrayList<>();
        this.setUpMesh();
    }

    void setUpMesh() {
        vertexAttributeSizes.addAll(Arrays.asList(vertexSize, normalSize, texCoordSize, tangentSize, bitangentSize));
        vao = new MeshVAO(floatData, vertexAttributeSizes, indices, Buffer.Usage.STATIC_DRAW);
        bindTextures();
    }

    private void bindTextures() {
        colorCounter = 1;
        normalCounter = 1;
        displacementCounter = 1;
        roughnessCounter = 1;
        metallicCounter = 1;
        aoCounter = 1;
        reflectionCounter = 1;
        for (Texture2D currentTexture : texture2DS) {
            ModelTexture2D currentTexture2D = (ModelTexture2D) currentTexture;
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
                textureUniforms.add(new AbstractMap.SimpleEntry<>("material." + name + number, currentTexture2D));
            }
        }
    }

    private void setTextureUniforms(ShaderProgram shaderProgram) {
        for (Map.Entry<String, ModelTexture2D> entry : textureUniforms) {
            if (!entry.getValue().isBound()) {
                entry.getValue().bind();
            }
            shaderProgram.setUniform1IBindless(entry.getKey(), entry.getValue().getIndex());
        }
    }

    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        // Bind the shaderProgram
        shaderProgram.bind();

        // Set all texture uniforms for this mesh if the correct flag is set
        if (flags.getKey().intoColor) {
            setTextureUniforms(shaderProgram);
        }

        // Validate the shaderProgram
        shaderProgram.validate();

        // Let the VAO draw its contents
        vao.draw();

        // Unbind the shaderProgram
        ShaderProgram.unbind();
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
