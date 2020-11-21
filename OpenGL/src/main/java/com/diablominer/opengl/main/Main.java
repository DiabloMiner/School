package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.Model;
import com.diablominer.opengl.utils.BufferUtil;
import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.render.Texture;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Main {

    private ShaderProgram shaderProgram, lightSourceShader;
    private int vbo;
    private int vao, vaoLight;
    private int ebo;
    private Texture texture, texture2;
    private IntBuffer indicesBuffer;
    private Window window;
    private Camera camera;

    private float deltaTime = 0.0f; // Time between current frame and last frame
    private float lastTime = 0.0f; // Time of last frame

    public static void main(String[] args) throws IOException {
        new Main();
    }

    public Main() throws IOException {
        try { init(); } catch (Exception e) { e.printStackTrace(); }

        run();
    }

    private void init() throws Exception {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        camera = new Camera(45.0f, new Vector3f(0.0f, 0.0f, 3.0f), new Vector3f(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        window = new Window(1280, 720, "Hello World",camera);

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

        texture = new Texture("./src/main/resources/textures/container2.png");
        texture2 = new Texture("./src/main/resources/textures/container2_specular.png");

        // The vertices and the indices are set up and put into a FloatBuffer, also the EBO is set up and a buffer is created for it
        /*float[] vertices = {
                // positions          // colors           // texture coords
                 0.5f,  0.5f, 0.0f,    1.0f, 0.0f, 0.0f,    1.0f, 0.0f,   // top right
                 0.5f, -0.5f, 0.0f,    0.0f, 1.0f, 0.0f,    1.0f, 1.0f,   // bottom right
                -0.5f, -0.5f, 0.0f,    0.0f, 0.0f, 1.0f,    0.0f, 1.0f,   // bottom left
                -0.5f,  0.5f, 0.0f,    1.0f, 1.0f, 0.0f,    0.0f, 0.0f    // top left
        };*/
        float[] vertices = {
                // positions          // normals           // texture coords
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,
                0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  0.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f,  1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f,  0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,
                0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  1.0f,  1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f,  1.0f,  0.0f,  0.0f,

                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
                -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
                -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f,  0.0f,

                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,
                0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  1.0f,
                0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f,  0.0f,
                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f,  0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f,  0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f,  1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f,  0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f,  1.0f
        };
        /*int[] indices = new int[] {  // note that we start from 0!
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };
        indicesBuffer = BufferUtil.createBuffer(indices);*/
        FloatBuffer verticesBuffer = BufferUtil.createBuffer(vertices);

        // The VAO is set up and bound
        vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vao);

        // The VBO is set up and data is provided for the VBO
        vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verticesBuffer, GL33.GL_STATIC_DRAW);

        // The EBO is set up and data is provided for the EBO
        /*ebo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL33.GL_STATIC_DRAW);*/

        // The structure of the data is defined and stored in the VAO
        //
        // https://learnopengl.com/Getting-started/Textures has a very good graph at the chapter applying textures
        //
        // Stride = Float.BYTES * number of coordinates/size(xyz=3) * number of variables that have so many coordinates(position, color = 2) = 6 * Float.BYTES
        // Pointer = Float.BYTES * number of coordinates/size * number of variables that have so many coordinates * index = Float.BYTES * 3 * 2  * 0/1/2(or any other index that you use)
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, (8 * Float.BYTES), 0);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, (8 * Float.BYTES), (3 * Float.BYTES));
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, (8 * Float.BYTES), (6 * Float.BYTES));

        // Unbind the VBO, the VAO and the EBO
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        /*GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);*/
        GL33.glBindVertexArray(0);

        // The memory allocated to the vertices buffer is freed
        BufferUtil.destroyBuffer(verticesBuffer);


        FloatBuffer verticesBufferLight = BufferUtil.createBuffer(vertices);

        vaoLight = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vaoLight);

        int vboLight = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vboLight);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verticesBufferLight, GL33.GL_STATIC_DRAW);

        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, (8 * Float.BYTES), 0);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        BufferUtil.destroyBuffer(verticesBufferLight);
    }

    private void run() throws IOException {
        while (!window.shouldClose()) {
            float currentTime = (float) GLFW.glfwGetTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            update();

            render();
        }

        cleanup();
    }

    private void render() throws IOException {
        GL33.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT |GL33.GL_DEPTH_BUFFER_BIT);

        /*shaderProgram.bind();*/

        // Bind to the VAO
        /*GL33.glBindVertexArray(vao);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        texture.bind();
        texture2.bind();*/

        // Draw the vertices
        /*GL33.glDrawElements(GL33.GL_TRIANGLES, indicesBuffer);*/

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

            // Rendering for the light sources
            lightSourceShader.bind();
            GL33.glBindVertexArray(vaoLight);
            GL33.glEnableVertexAttribArray(0);

            GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);

            GL33.glDisableVertexAttribArray(0);
            GL33.glBindVertexArray(0);
            lightSourceShader.unbind();
        }*/
        lightSourceShader.setUniformMat4F("model", new Matrix4f().identity());
        lightSourceShader.bind();
        GL33.glBindVertexArray(vaoLight);
        GL33.glEnableVertexAttribArray(0);

        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);

        GL33.glDisableVertexAttribArray(0);
        GL33.glBindVertexArray(0);
        lightSourceShader.unbind();

        Model model = new Model("./src/main/resources/models/cube/cube.obj");
        shaderProgram.setUniformMat4F("model", new Matrix4f().identity());
        shaderProgram.setUniformVec3F("dirLight.direction", Transforms.vectorToUnitVector(0.3f, 0.8f, 1.5f));
        shaderProgram.setUniformVec3F("dirLight.ambient", 0.2f, 0.2f, 0.2f);
        shaderProgram.setUniformVec3F("dirLight.diffuse", 0.5f, 0.5f, 0.5f);
        shaderProgram.setUniformVec3F("dirLight.specular", 1.0f, 1.0f, 1.0f);
        model.draw(shaderProgram);
        // Restore state
        /*GL33.glDisableVertexAttribArray(0);
        GL33.glDisableVertexAttribArray(1);
        GL33.glBindVertexArray(0);

        shaderProgram.unbind();

        Texture.unbindAll();*/

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
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }

        // Delete the indices buffer
        BufferUtil.destroyBuffer(indicesBuffer);

        // Delete the VBO
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(vbo);

        // Delete EBO
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glDeleteBuffers(ebo);

        // Delete the VAO
        GL33.glBindVertexArray(0);
        GL33.glDeleteVertexArrays(vao);

        GLFW.glfwTerminate();
    }

}
