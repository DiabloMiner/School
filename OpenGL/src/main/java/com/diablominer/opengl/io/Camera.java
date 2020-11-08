package com.diablominer.opengl.io;

import org.joml.Math;
import org.joml.Vector3f;

public class Camera {

    public float fov;
    public float yaw;
    public float pitch;
    public Vector3f cameraPos;
    public Vector3f cameraFront;
    public Vector3f cameraUp;

    public Camera(float fov, Vector3f pos, Vector3f front, Vector3f up) {
        this.fov = fov;
        cameraPos = pos;
        cameraFront = front;
        cameraUp = up;
        yaw = -90.0f;
        pitch = 0.0f;
    }

    public void update(Mouse mouse) {
        yaw += mouse.xOffset;
        pitch += mouse.yOffset;
        pitch = Math.clamp(-89.0f, 89.0f, pitch);

        changeDirection();
    }

    public void changeDirection() {
        Vector3f direction = new Vector3f();
        direction.x = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        direction.y = Math.sin(Math.toRadians(pitch));
        direction.z = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
        direction.normalize(cameraFront);
    }

    public void updateZoom(float yOffset) {
        fov -= yOffset;
        if (fov < 1.0f)
            fov = 1.0f;
        if (fov > 45.0f)
            fov = 45.0f;
    }

    public Vector3f getLookAtPosition() {
        Vector3f sum = new Vector3f();
        cameraPos.add(cameraFront, sum);
        return sum;
    }

    public void moveForwards(float cameraSpeed) {
        Vector3f product = new Vector3f();
        cameraFront.mul(cameraSpeed, product);
        cameraPos.add(product);
    }

    public void moveBackwards(float cameraSpeed) {
        Vector3f product = new Vector3f();
        cameraFront.mul(cameraSpeed, product);
        cameraPos.sub(product);
    }

    public void moveLeft(float cameraSpeed) {
        Vector3f product = new Vector3f();
        cameraFront.cross(cameraUp, product);
        product.normalize().mul(cameraSpeed);
        cameraPos.sub(product);
    }

    public void moveRight(float cameraSpeed) {
        Vector3f product = new Vector3f();
        cameraFront.cross(cameraUp, product);
        product.normalize().mul(cameraSpeed);
        cameraPos.add(product);
    }

}
