package com.diablominer.opengl.examples.learning;

public enum EventTypes {

    Event(com.diablominer.opengl.examples.learning.Event.class),
    CameraDirectionUpdate(CameraDirectionUpdate.class),
    CameraPositionUpdate(CameraPositionUpdate.class),
    CameraZoomUpdate(CameraZoomUpdate.class),
    KeyPressEvent(KeyPressEvent.class),
    WindowResizeEvent(com.diablominer.opengl.examples.learning.WindowResizeEvent.class);

    public final Class<?> classValue;

    EventTypes(Class<?> classValue) {
        this.classValue = classValue;
    }

}
