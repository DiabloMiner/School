package com.diablominer.opengl.examples.learning;

import java.util.*;

public class PingPongQuadMesh extends Mesh {

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

    public int inputTexIndex, horizontalTexIndex, verticalTexIndex;
    private boolean firstIteration, horizontal;

    public PingPongQuadMesh(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex, boolean horizontalAtStart) {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), Arrays.asList(verticalTex, horizontalTex, inputTex));
        verticalTexIndex = texture2DS.indexOf(verticalTex);
        horizontalTexIndex = texture2DS.indexOf(horizontalTex);
        inputTexIndex = texture2DS.indexOf(inputTex);
        setUpMesh();

        this.horizontal = horizontalAtStart;
        this.firstIteration = true;
    }

    void setUpMesh() {
        vao = new SimpleVAO(floatData, vertexAttributeSizes, Buffer.Usage.STATIC_DRAW);
        bindTextures();
    }

    private void bindTextures() {
        for (Texture2D texture : texture2DS) {
            texture.bind();
        }
    }

    private void setTextureUniforms(ShaderProgram shaderProgram) {
        if (firstIteration) {
            if (!texture2DS.get(inputTexIndex).isBound()) {
                texture2DS.get(inputTexIndex).bind();
            }
            shaderProgram.setUniform1IBindless("blurringTex", texture2DS.get(inputTexIndex).getIndex());
            firstIteration = false;
        } else {
            int index = horizontal ? verticalTexIndex : horizontalTexIndex;
            if (!texture2DS.get(index).isBound()) {
                texture2DS.get(index).bind();
            }
            shaderProgram.setUniform1IBindless("blurringTex", texture2DS.get(index).getIndex());
        }
    }

    @Override
    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        shaderProgram.bind();
        setTextureUniforms(shaderProgram);
        shaderProgram.setUniform1IBindless("horizontal", horizontal ? 1 : 0);
        shaderProgram.validate();

        vao.draw();

        ShaderProgram.unbind();

        horizontal = !horizontal;
    }

    public void setFirstIteration(boolean firstIteration) {
        this.firstIteration = firstIteration;
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
