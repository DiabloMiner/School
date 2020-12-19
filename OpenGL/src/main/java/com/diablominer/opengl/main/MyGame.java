package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.RenderablePointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

public class MyGame extends Game {

    private Camera camera;
    private Window window;
    private MyRenderingEngine renderingEngine;
    private MyLogicalEngine logicalEngine;
    private float deltaTime = 0.0f;
    private float lastTime = 0.0f;

    public static void main(String[] args) throws Exception {
        new MyGame();
    }

    public MyGame() throws Exception {
        init();
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

        camera = new Camera(45.0f, new Vector3f(20.0f, 1.0f, 20.0f), new Vector3f(-1.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        window = new Window(1280, 720, "OpenGL", camera);

        GL.createCapabilities();
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        GL33.glEnable(GL33.GL_DEPTH_TEST);

        ShaderProgram shaderProgram = new ShaderProgram("VertexShader", "FragmentShader");
        ShaderProgram lightSourceShaderProgram = new ShaderProgram("VertexShader", "LightSourceFragmentShader");
        renderingEngine = new MyRenderingEngine();
        logicalEngine = new MyLogicalEngine(true);
        PointLight pointLight = new PointLight(new Vector3f(-8.0f, 2.0f, -2.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.22f, 0.20f);
        MyRenderingEngineUnit renderingEngineUnit1 = new MyRenderingEngineUnit(shaderProgram, new DirectionalLight(new Vector3f(1.0f, 0.0f, 1.0f), new Vector3f(0.1f, 0.1f, 0.2f), new Vector3f(0.3f, 0.3f, 0.3f),  new Vector3f(0.8f, 0.8f, 0.8f)), pointLight, new SpotLight(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.35f, 0.7f, (float) Math.cos(Math.toRadians(17.5f)), (float) Math.cos(Math.toRadians(19.5f))));
        new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", renderingEngineUnit1, new Vector3f(0.0f, 0.0f, 0.0f));
        new Model("./src/main/resources/models/HelloWorld/cube.obj", renderingEngineUnit1, new Vector3f(0.0f, 0.0f, 0.0f));
        RenderingEngineUnit renderingEngineUnit2 = new RenderingEngineUnit(lightSourceShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, Window window) {
                this.getShaderProgram().setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
                Matrix4f view = new Matrix4f().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp);
                this.getShaderProgram().setUniformMat4F("view", view);
                this.getShaderProgram().setUniformVec3F("color", 1.0f, 1.0f, 1.0f);
            }

            @Override
            public void render() {
                renderAllRenderables();
            }
        };
        new RenderablePointLight(pointLight, "./src/main/resources/models/HelloWorld/cube.obj", logicalEngine, renderingEngineUnit2);
        renderingEngine.addNewEngineUnit(renderingEngineUnit1);
        renderingEngine.addNewEngineUnit(renderingEngineUnit2);

        Runnable logicalEngineRunnable = logicalEngine;
        Thread logicalEngineThread = new Thread(logicalEngineRunnable);
        logicalEngineThread.start();
    }

    @Override
    public void mainLoop() {
        while (!window.shouldClose()) {
            float currentTime = (float) GLFW.glfwGetTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            update();

            render();
        }
    }

    public void render() {
        renderingEngine.render(window);
    }

    public void update() {
        renderingEngine.updateAllEngineUnits(camera, window);

        handleInputs();

        window.update();
    }

    public void handleInputs() {
        float cameraSpeed = 10.0f * deltaTime;

        if (window.getInput().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            GLFW.glfwSetWindowShouldClose(window.getWindow(), true);
            GLFW.glfwDestroyWindow(window.getWindow());
        }

        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_W)) {
            camera.moveForwards(cameraSpeed);
        }
        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_S)) {
            camera.moveBackwards(cameraSpeed);
        }
        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_A)) {
            camera.moveLeft(cameraSpeed);
        }
        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_D)) {
            camera.moveRight(cameraSpeed);
        }
    }

    @Override
    public void cleanUp() {
        logicalEngine.setShouldRun(false);

        renderingEngine.end();

        GLFW.glfwTerminate();
    }
}
