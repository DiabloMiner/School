package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.render.textures.CubeMap;
import com.diablominer.opengl.render.textures.Texture;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

public class MyGame implements Game {

    private MyRenderingEngine renderingEngine;
    private MyLogicalEngine logicalEngine;
    private float deltaTime = 0.0f;
    private float lastTime = 0.0f;

    public static void main(String[] args) throws Exception {
        new MyGame();
    }

    public MyGame() {
        try { init(); } catch (Exception e) { System.err.println("An exception has occurred while initializing the game: " + e.getMessage()); }
        mainLoop();
        cleanUp();
    }

    @Override
    public void init() throws Exception {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);

        Camera camera = new Camera(45.0f, new Vector3f(1.0f, 1.0f, 5.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f, 0.0f), 1280.0f / 720.0f);
        Window window = new Window(1280, 720, "OpenGL", camera);

        GLFW.glfwMakeContextCurrent(window.getId());
        GL.createCapabilities();
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
        GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glCullFace(GL33.GL_BACK);
        GL33.glFrontFace(GL33.GL_CCW);
        GL33.glEnable(GL33.GL_MULTISAMPLE);

        logicalEngine = new MyLogicalEngine(true);
        renderingEngine = new MyRenderingEngine(logicalEngine, window, camera);

        Runnable logicalEngineRunnable = logicalEngine;
        Thread logicalEngineThread = new Thread(logicalEngineRunnable);
        logicalEngineThread.start();
    }

    @Override
    public void mainLoop() {
        while (!renderingEngine.getWindow().shouldClose()) {
            float currentTime = (float) GLFW.glfwGetTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            update();

            render();
        }
    }

    public void render() {
        renderingEngine.render();
    }

    public void update() {
        renderingEngine.setDeltaTime(deltaTime);
        renderingEngine.update();

        GLFW.glfwPollEvents();
    }

    @Override
    public void cleanUp() {
        Texture.destroyAllTextures();
        CubeMap.destroyAllCubeMaps();

        logicalEngine.setShouldRun(false);

        renderingEngine.destroy();

        GLFW.glfwTerminate();
    }
}
