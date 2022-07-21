package com.diablominer.opengl.examples.learning;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class Camera {

    public static float firstFov = 45.0f, firstNear = 0.1f, firstFar = 100.0f;
    public static float movementFactor = 4.0f;
    public static Vector3d worldSpaceRight = new Vector3d(1.0, 0.0, 0.0);
    public static Vector3d worldSpaceUp = new Vector3d(0.0, 1.0, 0.0);

    public Vector3f position, direction, right, up;
    public float yaw, pitch, fov, near, far;

    public Camera(Vector3f position, Vector3f direction) {
        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        Vector3f tempUp = new Vector3f(0.0f, 1.0f, 0.0f);
        right = new Vector3f(tempUp).cross(direction).normalize();
        this.up = new Vector3f(direction).cross(right);
        right = new Vector3f(direction).cross(up);

        yaw = (float) Math.toDegrees(Math.signum(direction.z) * new Vector3d(direction).angle(worldSpaceRight));
        pitch = (float) Math.toDegrees(new Vector3d(up).angle(worldSpaceUp));
        fov = firstFov;
        near = firstNear;
        far = firstFar;
    }

    public Camera(Vector3f position, Vector3f direction, float near, float far) {
        this.position = new Vector3f(position);
        this.direction = new Vector3f(direction);
        Vector3f tempUp = new Vector3f(0.0f, 1.0f, 0.0f);
        right = new Vector3f(tempUp).cross(direction).normalize();
        up = new Vector3f(direction).cross(right);
        right = new Vector3f(direction).cross(up);

        yaw = (float) Math.toDegrees(Math.signum(direction.z) * new Vector3d(direction).angle(worldSpaceRight));
        pitch = (float) Math.toDegrees(new Vector3d(up).angle(worldSpaceUp));
        fov = firstFov;
        this.near = near;
        this.far = far;
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
        right = new Vector3f(direction).cross(up).normalize();
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
        position.add(new Vector3f(direction).mul(movementFactor * factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveBackwards(float factor) {
        position.sub(new Vector3f(direction).mul(movementFactor * factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveRight(float factor) {
        position.add(new Vector3f(right).mul(movementFactor * factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public void moveLeft(float factor) {
        position.sub(new Vector3f(right).mul(movementFactor * factor));

        Learning6.engineInstance.getEventManager().addEvent(new CameraPositionUpdate(this));
    }

    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f().identity();
        view.lookAt(position, getFront(), up);
        return view;
    }



}
