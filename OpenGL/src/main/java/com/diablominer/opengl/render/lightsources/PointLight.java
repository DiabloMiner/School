package com.diablominer.opengl.render.lightsources;

import org.joml.Vector3f;

public class PointLight {

    private Vector3f position, color;
    private float farPlane;

    public PointLight(Vector3f position, Vector3f color, float farPlane) {
        this.position = position;
        this.color = color;
        this.farPlane = farPlane;
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

    public void setFarPlane(float farPlane) {
        this.farPlane = farPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }
}
