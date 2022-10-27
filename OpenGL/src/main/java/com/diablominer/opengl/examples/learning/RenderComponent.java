package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class RenderComponent implements Component, Renderable {

    protected Matrix4f modelMatrix;
    protected boolean hasShadow;

    public RenderComponent() {
        this.hasShadow = false;
        this.modelMatrix = new Matrix4f().identity();
    }

    public RenderComponent(Vector3f position) {
        this.hasShadow = false;
        this.modelMatrix = new Matrix4f().identity().translate(position);
    }

    public RenderComponent(Matrix4f model) {
        this.hasShadow = false;
        this.modelMatrix = new Matrix4f().identity().set(model);
    }

    public RenderComponent(boolean hasShadow) {
        this.hasShadow = hasShadow;
        this.modelMatrix = new Matrix4f().identity();
    }

    public RenderComponent(Vector3f position, boolean hasShadow) {
        this.hasShadow = hasShadow;
        this.modelMatrix = new Matrix4f().identity().translate(position);
    }

    public RenderComponent(Matrix4f model, boolean hasShadow) {
        this.hasShadow = hasShadow;
        this.modelMatrix = new Matrix4f().identity().set(model);
    }

    public void setModelMatrixUniform(ShaderProgram shaderProgram) {
        shaderProgram.setUniformMat4F("model", modelMatrix);
    }

    public void setModelMatrixUniform(ShaderProgram shaderProgram, Matrix4f modelMatrix) {
        shaderProgram.setUniformMat4F("model", modelMatrix);
    }

    public Vector3f getPosition() {
        Vector3f result = new Vector3f();
        modelMatrix.getTranslation(result);
        return result;
    }

    public void updateModelMatrix(Matrix4f matrix) {
        modelMatrix.set(matrix);
    }

    public void updateModelMatrix(Matrix4d matrix) {
        modelMatrix.set(matrix);
    }

    public void setShadowStatus(boolean hasShadow) {
        this.hasShadow = hasShadow;
    }

}
