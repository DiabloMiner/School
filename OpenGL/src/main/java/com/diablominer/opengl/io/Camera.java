package com.diablominer.opengl.io;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Math;
import org.joml.Vector3f;

public class Camera {

    private static final Vector3f zeroDegreeVector = new Vector3f(1.0f, 0.0f, 0.0f);
    public float fov;
    private float yaw;
    private float pitch;
    public Vector3f cameraPos;
    public Vector3f cameraFront;
    public Vector3f cameraUp;

    public Camera(float fov, Vector3f pos, Vector3f front, Vector3f up) {
        this.fov = fov;
        cameraPos = pos;
        cameraFront = front.normalize();
        cameraUp = up;
        yaw = (float) -Math.toDegrees(zeroDegreeVector.angle(cameraFront));
        pitch = 0.0f;
    }

    public void update(Mouse mouse) {
        yaw += mouse.xOffset;
        pitch += mouse.yOffset;

        changeDirection();
    }

    private void changeDirection() {
        pitch = Math.clamp(-89.0f, 89.0f, pitch);
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
        return Transforms.getSumOf2Vectors(cameraPos, cameraFront);
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

    public void changeYaw(float changeValue) {
        this.yaw += changeValue;
        changeDirection();
    }

    public void changePitch(float changeValue) {
        this.pitch += changeValue;
        changeDirection();
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        changeDirection();
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        changeDirection();
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }
}
