package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;

public class CameraUpdatedSpotLight extends SpotLight implements CameraPositionObserver, CameraDirectionObserver {

    public CameraUpdatedSpotLight(Vector3f position, Vector3f direction, Vector3f color) {
        super(position, direction, color);
        Learning6.getEventManager().addEventObserver(EventTypes.CameraPositionUpdate, this);
        Learning6.getEventManager().addEventObserver(EventTypes.CameraDirectionUpdate, this);
    }

    @Override
    public void update(Event event) {
        if (event.getClass().equals(CameraPositionUpdate.class)) {
            update((CameraPositionUpdate) event);
        } else {
            update((CameraDirectionUpdate) event);
        }
    }

    @Override
    public void update(CameraPositionUpdate event) {
        this.position = new Vector3f(event.position);
    }

    @Override
    public void update(CameraDirectionUpdate event) {
        this.direction = new Vector3f(event.direction);
    }
}
