package com.diablominer.opengl.examples.learning;

public class Mouse {

    public static final float sensitivity = 0.1f;
    public float x;
    public float y;
    public float deltaX = 0;
    public float deltaY = 0;
    public boolean firstMouse = true;

    public Mouse(float screenWidth, float screenHeight) {
        x = screenWidth / 2;
        y = screenHeight / 2;
    }

    public void updatePosition(float x, float y) {
        deltaX = x - this.x;
        deltaY = this.y - y;
        deltaX *= sensitivity;
        deltaY *= sensitivity;
        this.x = x;
        this.y = y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

}
