package com.diablominer.opengl.render.lightsources;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class SpotLight {

    private Vector3f position, color;
    private Matrix4f lightSpaceMatrix;

    public SpotLight(Vector3f position, Vector3f color) {
        this.position = position;
        this.color = color;
        lightSpaceMatrix = new Matrix4f().identity();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public Matrix4f getLightSpaceMatrix() {
        return lightSpaceMatrix;
    }

    public void setLightSpaceMatrix(Matrix4f lightSpaceMatrix) {
        this.lightSpaceMatrix = lightSpaceMatrix;
    }
}
