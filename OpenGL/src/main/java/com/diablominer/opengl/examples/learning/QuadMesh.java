package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class QuadMesh extends Mesh {

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
        super();
        setUpMesh();
    }

    public QuadMesh(Collection<Texture2D> textures) {
        super();
        setUpMesh(textures);
    }

    private void setUpMesh() {
        vertexAttributeSizes.addAll(Arrays.asList(2, 2));
        vao = new SimpleVAO(new ArrayList<>(Arrays.asList(vertices, texCoords)), vertexAttributeSizes, GL33.GL_STATIC_DRAW);
    }

    private void setUpMesh(Collection<Texture2D> textures) {
        vertexAttributeSizes.addAll(Arrays.asList(2, 2));
        texture2DS.addAll(textures);
        vao = new SimpleVAO(new ArrayList<>(Arrays.asList(vertices, texCoords)), vertexAttributeSizes, GL33.GL_STATIC_DRAW);
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
