package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

public class Main {

    private ShaderProgram shaderProgram, lightSourceShader;
    private Model model;
    private Window window;
    private Camera camera;

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
        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        // Crete a camera and a window
        camera = new Camera(45.0f, new Vector3f(0.0f, 0.0f, 3.0f), new Vector3f(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        window = new Window(1280, 720, "Hello World",camera);

        // Set up OpenGL
        GL.createCapabilities();
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        GL33.glEnable(GL33.GL_DEPTH_TEST);

        // The shaders are set up
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader("VertexShader");
        shaderProgram.createFragmentShader("FragmentShader");
        shaderProgram.link();

        lightSourceShader = new ShaderProgram();
        lightSourceShader.createVertexShader("VertexShader");
        lightSourceShader.createFragmentShader("LightSourceFragmentShader");
        lightSourceShader.link();

        model = new Model("./src/main/resources/models/backpack/backpack.obj");
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
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT |GL33.GL_DEPTH_BUFFER_BIT);

        shaderProgram.setUniformVec3F("dirLight.direction", Transforms.vectorToUnitVector(0.3f, 0.8f, 1.5f));
        shaderProgram.setUniformVec3F("dirLight.ambient", 0.2f, 0.2f, 0.2f);
        shaderProgram.setUniformVec3F("dirLight.diffuse", 0.5f, 0.5f, 0.5f);
        shaderProgram.setUniformVec3F("dirLight.specular", 1.0f, 1.0f, 1.0f);
        shaderProgram.setUniform1F("material.shininess", 32.0f);

        shaderProgram.setUniformMat4F("model", new Matrix4f().identity().translate(new Vector3f(1.0f, 2.0f, -3.0f)));

        model.draw(shaderProgram);

        // Normal model matrix for non light source objects
        /*Matrix4f model;

        // Model matrix for the light source object
        Matrix4f modelLightSource;

        // Textures are bound
        texture.bind();
        texture2.bind();

        Vector3f[] pointLightPositions = {
            new Vector3f( 0.7f,  0.2f,  2.0f),
            new Vector3f( 2.3f, -3.3f, -4.0f),
            new Vector3f(-4.0f,  2.0f, -12.0f),
            new Vector3f( 0.0f,  0.0f, -3.0f)
        };

        Vector3f[] cubePositions = {
                new Vector3f( 0.0f,  0.0f,  0.0f),
                new Vector3f( 2.0f,  5.0f, -15.0f),
                new Vector3f(-1.5f, -2.2f, -2.5f),
                new Vector3f(-3.8f, -2.0f, -12.3f),
                new Vector3f( 2.4f, -0.4f, -3.5f),
                new Vector3f(-1.7f,  3.0f, -7.5f),
                new Vector3f( 1.3f, -2.0f, -2.5f),
                new Vector3f( 1.5f,  2.0f, -2.5f),
                new Vector3f( 1.5f,  0.2f, -1.5f),
                new Vector3f(-1.3f,  1.0f, -1.5f)
        };

        // Here Uniforms for non light source objects are set
        shaderProgram.setUniformVec3F("viewPos", camera.cameraPos);

        // Direction light
        shaderProgram.setUniformVec3F("dirLight.direction", Transforms.vectorToUnitVector(0.3f, 0.8f, 1.5f));
        shaderProgram.setUniformVec3F("dirLight.ambient", 0.2f, 0.2f, 0.2f);
        shaderProgram.setUniformVec3F("dirLight.diffuse", 0.5f, 0.5f, 0.5f);
        shaderProgram.setUniformVec3F("dirLight.specular", 1.0f, 1.0f, 1.0f);

        // Point lights
        Vector3f diffColor = new Vector3f(1.0f, 1.0f, 1.0f);
        lightSourceShader.setUniformVec3F("color", diffColor);
        for (int i = 0; i < pointLightPositions.length; i++) {
            shaderProgram.setUniformVec3F("pointLights[" + i + "].position", pointLightPositions[i]);
            shaderProgram.setUniformVec3F("pointLights[" + i + "].ambient", 0.2f, 0.2f, 0.2f);
            shaderProgram.setUniformVec3F("pointLights[" + i + "].diffuse", diffColor);
            shaderProgram.setUniformVec3F("pointLights[" + i + "].specular", 1.0f, 1.0f, 1.0f);
            shaderProgram.setUniform1F("pointLights[" + i + "].constant",  1.0f);
            shaderProgram.setUniform1F("pointLights[" + i + "].linear",    0.35f);
            shaderProgram.setUniform1F("pointLights[" + i + "].quadratic", 0.44f);
            shaderProgram.setUniform1F("pointLights[" + i + "].cutOff", Math.cos(Math.toRadians(12.5f)));
            shaderProgram.setUniform1F("pointLights[" + i + "].outerCutOff", Math.cos(Math.toRadians(17.5f)));
        }

        // Spot light
        shaderProgram.setUniformVec3F("spotLight.position", camera.cameraPos);
        shaderProgram.setUniformVec3F("spotLight.direction", camera.cameraFront);
        shaderProgram.setUniformVec3F("spotLight.ambient", 0.2f, 0.2f, 0.2f);
        shaderProgram.setUniformVec3F("spotLight.diffuse", 0.5f, 0.5f, 0.5f);
        shaderProgram.setUniformVec3F("spotLight.specular", 1.0f, 1.0f, 1.0f);
        shaderProgram.setUniform1F("spotLight.constant",  1.0f);
        shaderProgram.setUniform1F("spotLight.linear",    0.22f);
        shaderProgram.setUniform1F("spotLight.quadratic", 0.20f);
        shaderProgram.setUniform1F("spotLight.cutOff", Math.cos(Math.toRadians(12.5f)));
        shaderProgram.setUniform1F("spotLight.outerCutOff", Math.cos(Math.toRadians(17.5f)));

        shaderProgram.setUniform1F("material.shininess", 32.0f);
        shaderProgram.setUniform1I("material.diffuse", Texture.getIndexForTexture(texture));
        shaderProgram.setUniform1I("material.specular", Texture.getIndexForTexture(texture2));

        for (int i = 0; i < cubePositions.length; i++) {
            // Rendering for non light source objects
            model = new Matrix4f().identity();
            model.translate(cubePositions[i], model);
            model.rotate(Math.toRadians(i * 20.0f + 3.0f), Transforms.vectorToUnitVector(1.0f, 0.3f, 0.5f));
            shaderProgram.setUniformMat4F("model", model);

            shaderProgram.bind();
            GL33.glBindVertexArray(vao);
            GL33.glEnableVertexAttribArray(0);
            GL33.glEnableVertexAttribArray(1);
            GL33.glEnableVertexAttribArray(2);

            GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);

            GL33.glDisableVertexAttribArray(0);
            GL33.glDisableVertexAttribArray(1);
            GL33.glDisableVertexAttribArray(2);
            GL33.glBindVertexArray(0);
            shaderProgram.unbind();
        }

        Texture.unbindAll();

        for (int i = 0; i < pointLightPositions.length; i++) {
            modelLightSource = new Matrix4f().identity();
            modelLightSource.translate(pointLightPositions[i]);
            modelLightSource.scale(new Vector3f(0.5f), modelLightSource);
            lightSourceShader.setUniformMat4F("model", modelLightSource);

            // Rendering for the light source
            lightSourceShader.bind();
            GL33.glBindVertexArray(vaoLight);
            GL33.glEnableVertexAttribArray(0);

            GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);

            GL33.glDisableVertexAttribArray(0);
            GL33.glBindVertexArray(0);
            lightSourceShader.unbind();
        }*/

        window.swapBuffers();
    }

    private void update() {
        if (window.hasResized()) {
            GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        }

        Matrix4f view = new Matrix4f();
        view.lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp);
        shaderProgram.setUniformMat4F("view", view);
        lightSourceShader.setUniformMat4F("view", view);

        shaderProgram.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
        lightSourceShader.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));

        handleInputs();

        window.update();
    }

    private void handleInputs() {
        float cameraSpeed = 5f * deltaTime;

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
