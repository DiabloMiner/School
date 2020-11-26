package com.diablominer.opengl.io;

public class Mouse {

    public static final float sensitivity = 0.1f;
    public float mouseX;
    public float mouseY;
    public float xOffset = 0;
    public float yOffset = 0;

    public Mouse(float screenWidth, float screenHeight) {
        mouseX = screenWidth / 2;
        mouseY = screenHeight / 2;
    }

    public void updatePosition(float x, float y) {
        xOffset = x - mouseX;
        yOffset = mouseY - y;
        xOffset *= sensitivity;
        yOffset *= sensitivity;
        mouseX = x;
        mouseY = y;
    }

    public void setPosition(float x, float y) {
        mouseX = x;
        mouseY = y;
    }

}
