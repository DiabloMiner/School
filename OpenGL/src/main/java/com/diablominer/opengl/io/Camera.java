package com.diablominer.opengl.io;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Math;
import org.joml.Vector3f;

public class Camera {

    private static final Vector3f zeroDegreeVector = new Vector3f(1.0f, 0.0f, 0.0f);
    public float fov;
    public float aspect;
    private float yaw;
    private float pitch;
    public Vector3f position;
    public Vector3f front;
    public Vector3f up;

    public Camera(float fov, Vector3f pos, Vector3f front, Vector3f up, float aspect) {
        this.fov = fov;
        this.aspect = aspect;
        this.position = pos;
        this.front = front.normalize();
        this.up = up;
        yaw = (float) -Math.toDegrees(zeroDegreeVector.angle(this.front));
        pitch = 0.0f;
        changeDirection();
    }

    public void update(Mouse mouse) {
        yaw += mouse.xOffset;
        pitch += mouse.yOffset;

        pitch = Math.clamp(-89.0f, 89.0f, pitch);
        changeDirection();
    }

    private void changeDirection() {
        Vector3f direction = new Vector3f();
        direction.x = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        direction.y = Math.sin(Math.toRadians(pitch));
        direction.z = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        direction.normalize(front);
    }

    public void updateZoom(float yOffset) {
        fov -= yOffset;
        fov = Math.clamp(1.0f, 45.0f, fov);
    }

    public Vector3f getLookAtPosition() {
        return Transforms.getSumOf2Vectors(position, front);
    }

    public void moveForwards(float cameraSpeed) {
        Vector3f product = new Vector3f();
        front.mul(cameraSpeed, product);
        position.add(product);
    }

    public void moveBackwards(float cameraSpeed) {
        Vector3f product = new Vector3f();
        front.mul(cameraSpeed, product);
        position.sub(product);
    }

    public void moveLeft(float cameraSpeed) {
        Vector3f product = new Vector3f();
        front.cross(up, product);
        product.normalize().mul(cameraSpeed);
        position.sub(product);
    }

    public void moveRight(float cameraSpeed) {
        Vector3f product = new Vector3f();
        front.cross(up, product);
        product.normalize().mul(cameraSpeed);
        position.add(product);
    }
}
