package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

public abstract class Renderable {

    protected Matrix4f modelMatrix;

    public Renderable() {
        modelMatrix = new Matrix4f().identity();
    }

    public Renderable(Vector3f position) {
        modelMatrix = new Matrix4f().identity().translate(position);
    }

    public Renderable(Matrix4f model) {
        modelMatrix = new Matrix4f().identity().set(model);
    }

    public void setModelMatrixUniform(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        shaderProgram.setUniformMat4F("model", modelMatrix);
    }

    public void setModelMatrixUniform(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, Matrix4f modelMatrix) {
        shaderProgram.setUniformMat4F("model", modelMatrix);
    }

    public abstract void draw(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags);

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

}
