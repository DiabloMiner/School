package com.diablominer.opengl.io;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;

import java.nio.DoubleBuffer;

public class Window {

    private long window;
    public int WIDTH;
    public int HEIGHT;
    private boolean fullscreen;
    private boolean hasResized;
    private Input input;
    private GLFWWindowSizeCallback windowsizecallback;

    public static void setCallbacks() {
        GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                throw new IllegalStateException(GLFWErrorCallback.getDescription(description));
            }
        });
    }

    private void setLocalCallbacks() {
        windowsizecallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                setSize(width, height);
                hasResized = true;
            }
        };
    }

    public Window() {
        setSize(1280, 720);
        this.fullscreen = false;
        this.hasResized = false;
    }

    public void createWindow(String title) {
        window = GLFW.glfwCreateWindow(WIDTH, HEIGHT, title, fullscreen ? GLFW.glfwGetPrimaryMonitor() : 0, 0);
        if (window == 0) {
            throw new IllegalStateException("Failed to create window");
        }

        if (!fullscreen) {
            GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            GLFW.glfwSetWindowPos(window, (videoMode.width() - WIDTH) / 2, (videoMode.height() - HEIGHT) / 2);
            GLFW.glfwShowWindow(window);
        }

        GLFW.glfwMakeContextCurrent(window);

        input = new Input(window);
        setLocalCallbacks();
    }

    public void cleanUp() {
        windowsizecallback.close();
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(window);
    }

    public void update() {
        input.update();
        GLFW.glfwPollEvents();
    }

    public void setSize(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
    }

    public double getCursorPosX() {
        DoubleBuffer posX = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(window, posX, null);
        return posX.get(0);
    }

    public double getCursorPosY() {
        DoubleBuffer posY = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(window, null, posY);
        return posY.get(0);
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public long getWindow() {
        return this.window;
    }

    public Input getInput() {
        return input;
    }

    public boolean hasResized() {
        return this.hasResized;
    }

}
