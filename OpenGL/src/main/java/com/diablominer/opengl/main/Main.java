package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Timer;
import com.diablominer.opengl.io.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Main {

    private Window win;

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
        win = new Window();
        win.createWindow("Hello world");

        GL.createCapabilities();
    }

    private void run() {
        double frameCap = 1.0/60.0;
        double time = Timer.getTime();
        double unprocessed = 0;

        while (!win.shouldClose()) {
            boolean canRender = false;
            double time2 = Timer.getTime();
            double passed = time2 - time;

            unprocessed += passed;
            time = time2;

            while (unprocessed >= frameCap) {
                unprocessed -= frameCap;
                canRender = true;

                update(win);
            }

            if (canRender) {
                render();
            }

        }

        GLFW.glfwTerminate();
    }

    private void render() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        win.swapBuffers();
    }

    private void update(Window win) {
        if (win.hasResized()) {
            GL13.glViewport(0, 0, win.getWIDTH(), win.getHEIGHT());
        }
        handleInputs();
        win.update();
    }

    private void handleInputs() {
        if (win.getInput().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            GLFW.glfwSetWindowShouldClose(win.getWindow(), true);
            GLFW.glfwDestroyWindow(win.getWindow());
        }
    }

}
