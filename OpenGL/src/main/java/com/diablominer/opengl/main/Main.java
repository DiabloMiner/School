package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.BufferUtil;
import com.diablominer.opengl.render.ShaderProgram;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;

public class Main {

    private ShaderProgram shaderProgram;
    private int vbo;
    private int vao;
    private float[] vertices;
    private Window window;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try { init(); } catch (Exception e) { e.printStackTrace(); }

        run();
    }

    private void init() throws Exception {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        window = new Window(1280, 720);
        window.createWindow("Hello world");

        GL.createCapabilities();
        GL11.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());

        // The shader are set up
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader("VertexShader");
        shaderProgram.createFragmentShader("FragmentShader");
        shaderProgram.link();

        // The vertices are set up and put into a FloatBuffer
        vertices = new float[]{
                0.0f,  0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        };
        FloatBuffer verticesBuffer = BufferUtil.createBuffer(vertices);

        // The VAO is set up and bound
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        // The VBO is set up and data is provided for the VBO
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);

        // The structure of the data is defined and stored in the VAO
        GL20.glVertexAttribPointer(0, 3, GL20.GL_FLOAT, false, 0, 0);

        // Unbind the VBO and the VAO
        GL15.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        // The memory allocated to the vertices buffer is freed
        if (verticesBuffer != null) {
            BufferUtil.destroyBuffer(verticesBuffer);
        }
    }

    private void run() {
        while (!window.shouldClose()) {
            update();

            render();
        }

        cleanup();
    }

    private void render() {
        GL11.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        shaderProgram.bind();

        // Bind to the VAO
        GL30.glBindVertexArray(vao);
        GL30.glEnableVertexAttribArray(0);

        // Draw the vertices
        GL15.glDrawArrays(GL15.GL_TRIANGLES, 0, 3);

        // Restore state
        GL20.glDisableVertexAttribArray(0);
        GL30.glBindVertexArray(0);

        shaderProgram.unbind();

        window.swapBuffers();
    }

    private void update() {
        if (window.hasResized()) {
            GL11.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
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

    private void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }

        GL30.glDisableVertexAttribArray(0);

        // Delete the VBO
        GL15.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(vbo);

        // Delete the VAO
        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vao);

        window.cleanUp();

        GLFW.glfwTerminate();
    }

}
