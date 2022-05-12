package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class PingPongQuadMesh extends Mesh {

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

    public PingPongQuadMesh(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex) {
        setUpMesh(new ArrayList<>(Arrays.asList(verticalTex, horizontalTex, inputTex)));
        verticalTexIndex = texture2DS.indexOf(verticalTex);
        horizontalTexIndex = texture2DS.indexOf(horizontalTex);
        inputTexIndex = texture2DS.indexOf(inputTex);
    }

    private void setUpMesh(Collection<Texture2D> textures) {
        vertexAttributeSizes.addAll(Arrays.asList(2, 2));
        texture2DS.addAll(textures);
        vao = new SimpleVAO(new ArrayList<>(Arrays.asList(vertices, texCoords)), vertexAttributeSizes, GL33.GL_STATIC_DRAW);
    }

    public void draw(ShaderProgram shaderProgram, boolean firstIteration, boolean horizontal) {
        if (firstIteration) {
            texture2DS.get(inputTexIndex).bind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(inputTexIndex).index);
        } else {
            texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).bind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).index);
        }
        shaderProgram.setUniform1I("horizontal", horizontal ? 1 : 0);

        shaderProgram.bind();
        vao.draw();
        ShaderProgram.unbind();

        if (firstIteration) {
            texture2DS.get(inputTexIndex).unbind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(inputTexIndex).index);
        } else {
            texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).unbind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).index);
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

}
