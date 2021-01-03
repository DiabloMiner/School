package com.diablominer.opengl.io;

import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

public class Input {

    private long id;
    public boolean[] keys;

    public Input(long id) {
        this.id = id;
        this.keys = new boolean[GLFW.GLFW_KEY_LAST];
        Arrays.fill(keys, false);
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(id, key) == GLFW.GLFW_TRUE;
    }

    public boolean isMouseButtonDown(int button) {
        return GLFW.glfwGetMouseButton(id, button) == 1;
    }

    public boolean isMouseButtonReleased(int button) {
        return GLFW.glfwGetMouseButton(id, button) == GLFW.GLFW_RELEASE;
    }

    public void update() {
        for (int i = 32; i < GLFW.GLFW_KEY_LAST; i++) {
            keys[i] = isKeyDown(i);
        }
    }

    public boolean isKeyPressed(int key) {
        return (isKeyDown(key) && !keys[key]);
    }

    public boolean isKeyReleased(int key) {
        return (!isKeyDown(key) && keys[key]);
    }

}
