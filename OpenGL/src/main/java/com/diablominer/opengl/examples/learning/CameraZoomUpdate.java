package com.diablominer.opengl.examples.learning;

public class CameraZoomUpdate implements Event {

    public float fov;

    public CameraZoomUpdate(Camera camera) {
        this.fov = camera.fov;
    }

}
