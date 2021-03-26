package com.diablominer.opengl.render.lightsources;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DirectionalLight {

    private Vector3f direction, color;
    private Matrix4f lightSpaceMatrix;

    public DirectionalLight(Vector3f dir, Vector3f color) {
        this.direction = dir;
        this.color = color;
        lightSpaceMatrix = new Matrix4f().identity();
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
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
