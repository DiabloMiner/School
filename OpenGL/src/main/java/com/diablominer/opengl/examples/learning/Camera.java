package com.diablominer.opengl.examples.learning;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Camera implements KeyPressObserver {

    public static float firstFov = 45.0f, firstNear = 0.1f, firstFar = 100.0f;

    public Vector3f position, direction, right, up;
    public float yaw, pitch, fov, near, far;

    public Camera(Vector3f position, Vector3f direction) {
        yaw = -90.0f;
        pitch = 0.0f;
        fov = firstFov;
        near = firstNear;
        far = firstFar;

        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        Vector3f tempUp = new Vector3f(0.0f, 1.0f, 0.0f);
        right = new Vector3f(tempUp).cross(direction).normalize();
        this.up = new Vector3f(direction).cross(right);

        Learning6.engineInstance.getEventManager().addEventObserver(EventTypes.KeyPressEvent, this);
    }

    public Camera(Vector3f position, Vector3f direction, float near, float far) {
        yaw = -90.0f;
        pitch = 0.0f;
        fov = firstFov;
        this.near = near;
        this.far = far;

        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        Vector3f tempUp = new Vector3f(0.0f, 1.0f, 0.0f);
        right = new Vector3f(tempUp).cross(direction).normalize();
        this.up = new Vector3f(direction).cross(right);

        Learning6.engineInstance.getEventManager().addEventObserver(EventTypes.KeyPressEvent, this);
    }

    public void update(Mouse mouse) {
        yaw += mouse.deltaX;
        pitch += mouse.deltaY;
        pitch = Math.clamp(-89.0f, 89.0f, pitch);

        changeDirection();

        Learning6.engineInstance.getEventManager().addEvent(new CameraDirectionUpdate(this));
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

        Learning6.engineInstance.getEventManager().addEvent(new CameraZoomUpdate(this));
    }

    public Vector3f getFront() {
        return new Vector3f(position).add(direction);
    }

    public void moveForwards(float factor) {
        position.add(new Vector3f(direction).mul(factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveBackwards(float factor) {
        position.sub(new Vector3f(direction).mul(factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveRight(float factor) {
        position.add(new Vector3f(right).mul(factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveLeft(float factor) {
        position.sub(new Vector3f(right).mul(factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    @Override
    public void update(Event event) {}

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

    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f().identity();
        view.lookAt(position, getFront(), up);
        return view;
    }



}
