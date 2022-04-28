package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;

public class CameraDirectionUpdate implements Event {

    public Vector3f direction, right, up;
    public float yaw, pitch;

    public CameraDirectionUpdate(Camera camera) {
        this.direction = camera.direction;
        this.right = camera.right;
        this.up = camera.up;
        this.yaw = camera.yaw;
        this.pitch = camera.pitch;
    }
}
