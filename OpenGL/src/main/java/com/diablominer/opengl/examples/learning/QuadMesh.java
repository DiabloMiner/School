package com.diablominer.opengl.examples.learning;

import java.util.*;

public class QuadMesh extends Mesh {

    public static final int vertexSize = 2;
    public static final int texCoordSize = 2;
    public static final float[] vertices = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f, -1.0f,
             1.0f,  1.0f,
    };

    public static final float[] texCoords = {
             0.0f, 1.0f,
             0.0f, 0.0f,
             1.0f, 0.0f,
             0.0f, 1.0f,
             1.0f, 0.0f,
             1.0f, 1.0f
    };

    protected List<Map.Entry<String, Texture2D>> textureUniforms;

    public QuadMesh() {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), new ArrayList<>());
        textureUniforms = new ArrayList<>();
        setUpMesh();
    }

    public QuadMesh(float[] vertices, float[] texCoords, int vertexSize, int texCoordSize) {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), new ArrayList<>());
        textureUniforms = new ArrayList<>();
        setUpMesh();
    }

    public QuadMesh(Collection<Texture2D> textures) {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), new ArrayList<>(textures));
        textureUniforms = new ArrayList<>();
        setUpMesh();
    }

    void setUpMesh() {
        vao = new SimpleVAO(floatData, vertexAttributeSizes, Buffer.Usage.STATIC_DRAW);
        bindTextures();
    }

    private void bindTextures() {
        for (int i = 0; i < texture2DS.size(); i++) {
            texture2DS.get(i).bind();
            textureUniforms.add(new AbstractMap.SimpleEntry<>("texture" + i, texture2DS.get(i)));
        }
    }

    private void setTextureUniforms(ShaderProgram shaderProgram) {
        for (Map.Entry<String, Texture2D> entry : textureUniforms) {
            if (!entry.getValue().isBound()) {
                entry.getValue().bind();
            }
            shaderProgram.setUniform1IBindless(entry.getKey(), entry.getValue().getIndex());
        }
    }

    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        shaderProgram.bind();
        if (flags.getKey().intoColor) {
            setTextureUniforms(shaderProgram);
        }
        shaderProgram.validate();

        vao.draw();

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

}
