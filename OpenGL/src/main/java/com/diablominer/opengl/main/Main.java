package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Timer;
import com.diablominer.opengl.io.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Main {

    private Window window;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        init();
        run();
    }

    private void init() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        window = new Window();
        window.createWindow("Hello world");

        GL.createCapabilities();
    }

    private void run() {
        double frameCap = 1.0/60.0;
        double time = Timer.getTime();
        double unprocessed = 0;

        while (!window.shouldClose()) {
            boolean canRender = false;
            double time2 = Timer.getTime();
            double passed = time2 - time;

            unprocessed += passed;
            time = time2;

            while (unprocessed >= frameCap) {
                unprocessed -= frameCap;
                canRender = true;

                update();
            }

            if (canRender) {
                render();
            }

        }

        window.cleanUp();
        GLFW.glfwTerminate();
    }

    private void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        window.swapBuffers();
    }

    private void update() {
        if (window.hasResized()) {
            GL13.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        }
        handleInputs();
        window.update();
    }

    private void handleInputs() {
        if (window.getInput().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            GLFW.glfwSetWindowShouldClose(window.getWindow(), true);
            GLFW.glfwDestroyWindow(window.getWindow());
        }
    }

}
