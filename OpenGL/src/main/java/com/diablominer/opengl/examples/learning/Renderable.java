package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class Renderable {

    public static List<Renderable> allRenderables = new ArrayList<>();

    private Matrix4f modelMatrix;

    public Renderable() {
        modelMatrix = new Matrix4f().identity();
        allRenderables.add(this);
    }

    public Renderable(Vector3f position) {
        modelMatrix = new Matrix4f().identity().translate(position);
        allRenderables.add(this);
    }

    public Renderable(Matrix4f model) {
        modelMatrix = new Matrix4f().identity().set(model);
        allRenderables.add(this);
    }

    public void setModelMatrixUniform(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        shaderProgram.setUniformMat4F("model", modelMatrix);
    }

    public void setModelMatrixUniform(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, Matrix4f modelMatrix) {
        shaderProgram.setUniformMat4F("model", modelMatrix);
    }

    public abstract void draw(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram);

    public abstract void destroy();

    public Vector3f getPosition() {
        Vector3f result = new Vector3f();
        modelMatrix.getTranslation(result);
        return result;
    }

    public void setPosition(Vector3f position) {
        modelMatrix.translate(position);
    }

    public void setModelMatrix(Matrix4f matrix) {
        modelMatrix = matrix;
    }

    public static void destroyAll() {
        for (Renderable renderable : allRenderables) {
            renderable.destroy();
        }
    }
}