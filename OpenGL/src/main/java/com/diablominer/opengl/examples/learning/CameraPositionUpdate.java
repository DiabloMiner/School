package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;

public class CameraPositionUpdate implements Event {

    public Vector3f position;

    public CameraPositionUpdate(Camera camera) {
        this.position = camera.position;
    }
}
