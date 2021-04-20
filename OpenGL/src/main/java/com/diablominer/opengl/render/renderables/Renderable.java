package com.diablominer.opengl.render.renderables;

import com.diablominer.opengl.render.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class Renderable {

    private Matrix4f modelMatrix;

    public Renderable(Vector3f position) {
        modelMatrix = new Matrix4f().identity().translate(position);
    }

    public void setPositionAsModelMatrix(ShaderProgram shaderProgram) {
        shaderProgram.setUniformMat4F("model", modelMatrix);
    }

    public abstract void draw(ShaderProgram shaderProgram);

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
