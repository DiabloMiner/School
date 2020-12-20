package com.diablominer.opengl.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Renderable {

    private Vector3f position;

    public Renderable(Vector3f position) {
        this.position = position;
    }

    public void setPositionAsModelMatrix(ShaderProgram shaderProgram) {
        shaderProgram.setUniformMat4F("model", new Matrix4f().identity().translate(position));
    }

    public abstract void draw(ShaderProgram shaderProgram);

    public abstract void cleanUp();

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }
}
