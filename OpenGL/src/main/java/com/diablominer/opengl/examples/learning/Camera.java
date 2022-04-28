package com.diablominer.opengl.examples.learning;

import org.joml.Math;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Camera implements KeyPressObserver {

    public Vector3f position, direction, right, up;
    public float yaw, pitch, fov;

    public Camera(Vector3f position, Vector3f direction) {
        yaw = -90.0f;
        pitch = 0.0f;
        fov = 45.0f;

        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        Vector3f tempUp = new Vector3f(0.0f, 1.0f, 0.0f);
        right = new Vector3f(tempUp).cross(direction).normalize();
        this.up = new Vector3f(direction).cross(right);

        Learning6.getEventManager().addEventObserver(EventTypes.KeyPressEvent, this);
    }

    public void update(Mouse mouse) {
        yaw += mouse.deltaX;
        pitch += mouse.deltaY;
        pitch = Math.clamp(-89.0f, 89.0f, pitch);

        changeDirection();

        Learning6.getEventManager().addEvent(new CameraDirectionUpdate(this));
    }

    private void changeDirection() {
        Vector3f direction = new Vector3f();
        direction.x = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        direction.y = Math.sin(Math.toRadians(pitch));
        direction.z = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        direction.normalize(this.direction);
        right = new Vector3f(direction).cross(up);
    }

    public void updateZoom(float deltaValue) {
        fov -= deltaValue;
        fov = Math.clamp(1.0f, 45.0f, fov);

        Learning6.getEventManager().addEvent(new CameraZoomUpdate(this));
    }

    public Vector3f getFront() {
        return new Vector3f(position).add(direction);
    }

    public void moveForwards(float factor) {
        position.add(new Vector3f(direction).mul(factor));

        Learning6.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveBackwards(float factor) {
        position.sub(new Vector3f(direction).mul(factor));

        Learning6.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveRight(float factor) {
        position.add(new Vector3f(right).mul(factor));

        Learning6.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveLeft(float factor) {
        position.sub(new Vector3f(right).mul(factor));

        Learning6.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    @Override
    public void update(Event event) {
        update((KeyPressEvent) event);
    }

    @Override
    public void update(KeyPressEvent event) {
        if (event.pressedKey == GLFW.GLFW_KEY_W) {
            moveForwards(event.factor);
        } else if (event.pressedKey == GLFW.GLFW_KEY_S) {
            moveBackwards(event.factor);
        } else if (event.pressedKey == GLFW.GLFW_KEY_A) {
            moveLeft(event.factor);
        } else if (event.pressedKey == GLFW.GLFW_KEY_D) {
            moveRight(event.factor);
        }
    }
}
