package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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

    public QuadMesh() {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), new ArrayList<>());
        setUpMesh();
    }

    public QuadMesh(float[] vertices, float[] texCoords, int vertexSize, int texCoordSize) {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), new ArrayList<>());
        setUpMesh();
    }

    public QuadMesh(Collection<Texture2D> textures) {
        super(Arrays.asList(vertices, texCoords), Arrays.asList(vertexSize, texCoordSize), new ArrayList<>(textures));
        setUpMesh();
    }

    void setUpMesh() {
        vao = new SimpleVAO(floatData, vertexAttributeSizes, GL33.GL_STATIC_DRAW);
    }

    public void draw(ShaderProgram shaderProgram) {
        for (int i = 0; i < texture2DS.size(); i++) {
            texture2DS.get(i).bind();
            shaderProgram.setUniform1I("texture" + i, texture2DS.get(i).getIndex());
        }

        shaderProgram.bind();
        vao.draw();
        ShaderProgram.unbind();

        for (int i = 0; i < texture2DS.size(); i++) {
            texture2DS.get(i).unbind();
            shaderProgram.setUniform1I("texture" + i, texture2DS.get(i).getIndex());
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
