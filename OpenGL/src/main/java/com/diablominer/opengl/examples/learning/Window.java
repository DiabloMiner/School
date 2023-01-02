package com.diablominer.opengl.examples.learning;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;

import java.util.HashMap;
import java.util.Map;

public class Window {

    public static final Map<Long, Window> allWindows = new HashMap<>();
    public static Window focusedWindow;

    private final long id;
    public int width, height;
    public int monitorWidth, monitorHeight;
    public int previousWidth, previousHeight;
    private boolean fullscreen, closed;
    private final com.diablominer.opengl.examples.learning.Mouse mouse;

    public Window(int width, int height, boolean fullscreen, String title, Camera camera) {
        setSize(width, height);
        initializePreviousSize();
        this.fullscreen = fullscreen;
        this.closed = false;

        id = GLFW.glfwCreateWindow(this.width, this.height, title, fullscreen ? GLFW.glfwGetPrimaryMonitor() : 0, 0);
        if (id == 0) {
            GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            monitorWidth = videoMode.width();
            monitorHeight = videoMode.height();
            PointerBuffer buffer = PointerBuffer.allocateDirect(1);
            GLFW.glfwGetError(buffer);
            throw new IllegalStateException("Failed to create window: " + buffer.getStringASCII());
        }

        if (!fullscreen) {
            GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
            GLFW.glfwSetWindowPos(id, (videoMode.width() - this.width) / 2, (videoMode.height() - this.height) / 2);
            monitorWidth = videoMode.width();
            monitorHeight = videoMode.height();
            GLFW.glfwShowWindow(id);
        }
        GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        mouse = new com.diablominer.opengl.examples.learning.Mouse(width, height);
        setCallbacks(camera);
        allWindows.put(id, this);
    }

    private void setCallbacks(com.diablominer.opengl.examples.learning.Camera camera) {
        GLFW.glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                throw new IllegalStateException(GLFWErrorCallback.getDescription(description));
            }
        });

        GLFWWindowSizeCallback windowsizecallback = new GLFWWindowSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                if (width == 1296) {
                    System.out.print("");
                }
                setSize(width, height);
                System.out.println(width +  " | " + height + "  |  Fullscreen: " + fullscreen);
                Learning6.engineInstance.getEventManager().executeEvent(new WindowResizeEvent(allWindows.get(id)));
            }
        };
        GLFW.glfwSetWindowSizeCallback(id, windowsizecallback);

        GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (mouse.firstMouse) {
                    mouse.setPosition((float) xpos,(float) ypos);
                    mouse.firstMouse = false;
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

        GLFWWindowCloseCallback closeCallback = new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                Learning6.engineInstance.continueEngineLoop = false;
            }
        };
        GLFW.glfwSetWindowCloseCallback(id, closeCallback);

        GLFWWindowFocusCallback focusCallback = new GLFWWindowFocusCallback() {
            @Override
            public void invoke(long window, boolean focused) {
                if (focused) {
                    focusedWindow = allWindows.get(id);
                }
            }
        };
        GLFW.glfwSetWindowFocusCallback(id, focusCallback);
    }

    public void swapBuffers() {
        GLFW.glfwSwapBuffers(id);
    }

    public void initializePreviousSize() {
        previousWidth = width;
        previousHeight = height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;

        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        if (fullscreen) {
            monitorWidth = videoMode.width();
            monitorHeight = videoMode.height();
            previousWidth = width;
            previousHeight = height;
            width = monitorWidth;
            height = monitorHeight;
            System.out.println("setFullscreen:   " + width + " | " + height + "  |  Fullscreen: " + fullscreen);
            GLFW.glfwSetWindowMonitor(id, GLFW.glfwGetPrimaryMonitor(), ((videoMode.width() - this.width) / 2), ((videoMode.height() - this.height) / 2), width, height, videoMode.refreshRate());
        } else {
            width = previousWidth;
            height = previousHeight;
            System.out.println("setFullscreen:   " + width + " | " + height + "  |  Fullscreen: " + fullscreen);
            GLFW.glfwSetWindowMonitor(id, 0, ((monitorWidth - this.width) / 2), ((monitorHeight - this.height) / 2), width, height, videoMode.refreshRate());
        }
    }

    public long getId() {
        return this.id;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(id, key) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyReleased(int key) {
        return GLFW.glfwGetKey(id, key) == GLFW.GLFW_RELEASE;
    }

    public boolean isClosed() {
        return closed;
    }

    public void destroy() {
        GLFW.glfwDestroyWindow(id);
    }

    public void shouldClose() {
        this.closed = true;
        GLFW.glfwSetWindowShouldClose(id, true);
    }

    public static void setFocusedWindow(Window window) {
        Window.focusedWindow = window;
    }

}
