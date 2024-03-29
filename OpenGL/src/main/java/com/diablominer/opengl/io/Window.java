package com.diablominer.opengl.io;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL33;

public class Window {

    private long id;
    public int WIDTH;
    public int HEIGHT;
    private boolean fullscreen;
    private boolean hasResized;
    private Input input;
    private Mouse mouse;
    private boolean firstMouse = true;

    private void setCallbacks(Camera camera) {
        GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                throw new IllegalStateException(GLFWErrorCallback.getDescription(description));
            }
        });

        GLFWWindowSizeCallback windowsizecallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                setSize(width, height);
                hasResized = true;
                GL33.glViewport(0, 0, width, height);
            }
        };
        GLFW.glfwSetWindowSizeCallback(id, windowsizecallback);

        GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (firstMouse) {
                    mouse.setPosition((float) xpos,(float) ypos);
                    firstMouse = false;
                }
                mouse.updatePosition((float) xpos,(float) ypos);
                camera.update(mouse);
            }
        };
        GLFW.glfwSetCursorPosCallback(id, cursorPosCallback);

        GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                camera.updateZoom((float) yoffset);
            }
        };
        GLFW.glfwSetScrollCallback(id, scrollCallback);
    }

    public Window(int width, int height, String title, Camera camera) {
        setSize(width, height);
        this.fullscreen = false;
        this.hasResized = false;

        id = GLFW.glfwCreateWindow(WIDTH, HEIGHT, title, fullscreen ? GLFW.glfwGetPrimaryMonitor() : 0, 0);
        if (id == 0) {
            PointerBuffer buffer = PointerBuffer.allocateDirect(1);
            GLFW.glfwGetError(buffer);
            throw new IllegalStateException("Failed to create window: " + buffer.getStringASCII());
        }

        if (!fullscreen) {
            GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            GLFW.glfwSetWindowPos(id, (videoMode.width() - WIDTH) / 2, (videoMode.height() - HEIGHT) / 2);
            GLFW.glfwShowWindow(id);
        }

        GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        input = new Input(id);
        mouse = new Mouse(WIDTH, HEIGHT);
        setCallbacks(camera);
    }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(id);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(id);
    }

    public void update() {
        input.update();
    }

    public void setSize(int width, int height) {
        this.WIDTH = width;
        this.HEIGHT = height;
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

    public long getId() {
        return this.id;
    }

    public Input getInput() {
        return input;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public boolean hasResized() {
        return this.hasResized;
    }

}
