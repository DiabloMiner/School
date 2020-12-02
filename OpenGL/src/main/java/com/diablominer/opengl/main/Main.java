package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.render.lightSourceModels.DirectionalLightSource;
import com.diablominer.opengl.render.lightSourceModels.PointLightSource;
import com.diablominer.opengl.render.lightSourceModels.SpotLightSource;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.util.List;

public class Main {

    private ShaderProgram shaderProgram, lightSourceShader, oneColorShader;
    private Model model, model2;
    private Window window;
    private Camera camera;
    private DirectionalLightSource dirLight;
    private PointLightSource pointLight;
    private SpotLightSource spotLight;
    private List<PointLightSource> pointLights;
    private Vector3f[] pointLightPositions;

    private float deltaTime = 0.0f; // Time between current frame and last frame
    private float lastTime = 0.0f; // Time of last frame

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try { init(); } catch (Exception e) { e.printStackTrace(); }

        run();
    }

    private void init() throws Exception {
        // Initialize GLFW and tell GLFW we use only the core profile functionalities of OpenGL Version 3.3
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        // Create a camera and a window
        camera = new Camera(45.0f, new Vector3f(20.0f, 1.0f, 20.0f), new Vector3f(-1.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        window = new Window(1280, 720, "Hello World", camera);

        // Set up OpenGL
        GL.createCapabilities();
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        // The shaders are set up
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader("VertexShader");
        shaderProgram.createFragmentShader("FragmentShader");
        shaderProgram.link();

        lightSourceShader = new ShaderProgram();
        lightSourceShader.createVertexShader("VertexShader");
        lightSourceShader.createFragmentShader("LightSourceFragmentShader");
        lightSourceShader.link();

        oneColorShader = new ShaderProgram();
        oneColorShader.createVertexShader("VertexShader");
        oneColorShader.createFragmentShader("OneColorShader");
        oneColorShader.link();

        // Projection matrices are set
        shaderProgram.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
        oneColorShader.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
        lightSourceShader.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));

        model = new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj");
        model2 = new Model("./src/main/resources/models/HelloWorld/TheGreatBox.obj");
        dirLight = new DirectionalLightSource();
        spotLight = new SpotLightSource("./src/main/resources/models/cube/cube.obj");
        pointLights = PointLightSource.createMultiplePointLights(new String[] {
                "./src/main/resources/models/cube/cube.obj",
                "./src/main/resources/models/cube/cube.obj",
                "./src/main/resources/models/cube/cube.obj"
        });
        pointLightPositions = new Vector3f[] {
                new Vector3f(8.0f, 0.2f, -2.0f),
                new Vector3f(2.3f, -3.3f, -4.0f),
                new Vector3f(-4.0f, 2.0f, -12.0f)
        };
    }

    private void run() {
        while (!window.shouldClose()) {
            float currentTime = (float) GLFW.glfwGetTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            update();

            render();
        }

        cleanup();
    }

    private void render() {
        GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);

        dirLight.setUniforms(Transforms.vectorToUnitVector(-2.0f, -2.0f, -2.0f), new Vector3f(0.1f, 0.1f, 0.1f), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.8f, 0.8f, 0.8f), shaderProgram);
        spotLight.setUniforms(camera.cameraPos, camera.cameraFront, new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.11f, 0.10f, 12.5f, 17.5f, shaderProgram);
        for (int i = 0; i < pointLights.size(); i++) {
            pointLights.get(i).setUniforms(Transforms.getProductOf2Vectors(pointLightPositions[i], new Vector3f((float)Math.cos(GLFW.glfwGetTime()), (float)Math.sin(GLFW.glfwGetTime()), (float)Math.tan(GLFW.glfwGetTime()))), new Vector3f(0.3f, 0.3f, 0.3f), new Vector3f(0.7f, 0.7f, 0.7f), new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.22f, 0.20f, i, shaderProgram);
        }

        shaderProgram.setUniform1F("material.shininess", 32.0f);

        // The model matrix for shaderProgram is set
        shaderProgram.setUniformMat4F("model", new Matrix4f().identity());

        GL33.glStencilMask(0x00);
        // Stencil test functions are set and Hello World and light sources are rendered
        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
        GL33.glStencilMask(0xFF);
        model.draw(shaderProgram);
        for (PointLightSource pointLight : pointLights) {
            pointLight.draw(lightSourceShader);
        }
        model2.draw(shaderProgram);

        // More Stencil test functions are set and a outline is rendered
        GL33.glStencilFunc(GL33.GL_NOTEQUAL, 1, 0xFF);
        GL33.glStencilMask(0x00);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        oneColorShader.setUniformMat4F("model", new Matrix4f().identity().scale(1.1f));
        model2.draw(oneColorShader);
        GL33.glStencilMask(0xFF);
        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
        GL33.glEnable(GL33.GL_DEPTH_TEST);

        window.swapBuffers();
    }

    private void update() {
        if (window.hasResized()) {
            GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        }

        // View matrices and matrices that do something with the camera are set
        Matrix4f view = new Matrix4f();
        view.lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp);
        shaderProgram.setUniformMat4F("view", view);
        oneColorShader.setUniformMat4F("view", view);
        lightSourceShader.setUniformMat4F("view", view);

        shaderProgram.setUniformVec3F("viewPos", camera.cameraPos);

        handleInputs();

        window.update();
    }

    private void handleInputs() {
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

    private void cleanup() {
        // Clean up all shaders
        shaderProgram.cleanup();
        lightSourceShader.cleanup();

        // Clean up all models
        Model.cleanUpAllModels();

        // Terminate GLFW
        GLFW.glfwTerminate();
    }

}
