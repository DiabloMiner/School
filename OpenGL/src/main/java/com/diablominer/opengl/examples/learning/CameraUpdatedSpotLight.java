package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;

public class CameraUpdatedSpotLight extends SpotLight implements CameraPositionObserver, CameraDirectionObserver {

    private final Camera observedCamera;

    public CameraUpdatedSpotLight(Vector3f position, Vector3f direction, Vector3f color, int shadowSize, Camera camera) {
        super(position, direction, color, shadowSize);
        observedCamera = camera;
        Learning6.engineInstance.getEventManager().addEventObserver(EventTypes.CameraPositionUpdate, this);
        Learning6.engineInstance.getEventManager().addEventObserver(EventTypes.CameraDirectionUpdate, this);
    }

    @Override
    public void update(Event event) {}

    @Override
    public void update(CameraPositionUpdate event) {
        if (event.camera.equals(observedCamera)) {
            this.position = new Vector3f(event.position);
        }
    }

    @Override
    public void update(CameraDirectionUpdate event) {
        if (event.camera.equals(observedCamera)) {
            this.direction = new Vector3f(event.direction);
        }
    }
}
