package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class PingPongQuadMesh extends Mesh implements PingPongIterationObserver {

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

    public PingPongQuadMesh(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex) {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), Arrays.asList(verticalTex, horizontalTex, inputTex));
        verticalTexIndex = texture2DS.indexOf(verticalTex);
        horizontalTexIndex = texture2DS.indexOf(horizontalTex);
        inputTexIndex = texture2DS.indexOf(inputTex);
        setUpMesh();

        Learning6.engineInstance.getEventManager().addEventObserver(EventTypes.PingPongIterationEvent, this);
    }

    void setUpMesh() {
        vao = new SimpleVAO(floatData, vertexAttributeSizes, GL33.GL_STATIC_DRAW);
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        if (firstIteration) {
            texture2DS.get(inputTexIndex).bind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(inputTexIndex).getIndex());
        } else {
            texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).bind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).getIndex());
        }
        shaderProgram.setUniform1I("horizontal", horizontal ? 1 : 0);

        shaderProgram.bind();
        vao.draw();
        ShaderProgram.unbind();

        if (firstIteration) {
            texture2DS.get(inputTexIndex).unbind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(inputTexIndex).getIndex());
        } else {
            texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).unbind();
            shaderProgram.setUniform1I("blurringTex", texture2DS.get(horizontal ? verticalTexIndex : horizontalTexIndex).getIndex());
        }
    }

    @Override
    public void update(Event event) {}

    @Override
    public void update(PingPongIterationEvent event) {
        this.firstIteration = event.firstIteration;
        this.horizontal = event.horizontal;
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
