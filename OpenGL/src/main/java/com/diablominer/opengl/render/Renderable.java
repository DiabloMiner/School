package com.diablominer.opengl.render;

import org.joml.Matrix4f;

public abstract class Renderable {

    private Matrix4f position;

    public Renderable(EngineUnit engineUnit, Matrix4f matrix) {
        engineUnit.addNewRenderable(this);
        position = matrix;
    }

    public abstract void draw(ShaderProgram shaderProgram);

    public abstract void cleanUp();

    public Matrix4f getPosition() {
        return position;
    }

    public void setPosition(Matrix4f matrix) {
        position = matrix;
    }

    public void setModelMatrix(ShaderProgram shaderProgram) {
        shaderProgram.setUniformMat4F("model", position);
    }

}
