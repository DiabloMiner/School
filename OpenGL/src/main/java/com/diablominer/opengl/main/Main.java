package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.BufferUtil;
import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.render.Texture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.w3c.dom.Text;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Main {

    private ShaderProgram shaderProgram, shaderProgram2;
    private int vbo;
    private int vao;
    private int ebo;
    private int vbo2, vao2, ebo2;
    private Texture texture;
    private Texture texture2;
    private IntBuffer indicesBuffer, indicesBuffer2;
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
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());

        // The shader are set up
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader("VertexShader");
        shaderProgram.createFragmentShader("FragmentShader");
        shaderProgram.link();

        // Texture uniforms are set
        shaderProgram.setUniformI("texture1", 0);
        shaderProgram.setUniformI("texture2", 1);

        // The vertices and the indices are set up and put into a FloatBuffer, also the EBO is set up and a buffer is created for it
        float[] vertices = {
                // positions          // colors           // texture coords
                 0.5f,  0.5f, 0.0f,    1.0f, 0.0f, 0.0f,    1.0f, 0.0f,   // top right
                 0.5f, -0.5f, 0.0f,    0.0f, 1.0f, 0.0f,    1.0f, 1.0f,   // bottom right
                -0.5f, -0.5f, 0.0f,    0.0f, 0.0f, 1.0f,    0.0f, 1.0f,   // bottom left
                -0.5f,  0.5f, 0.0f,    1.0f, 1.0f, 0.0f,    0.0f, 0.0f    // top left
        };
        int[] indices = new int[] {  // note that we start from 0!
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };
        FloatBuffer verticesBuffer = BufferUtil.createBuffer(vertices);
        indicesBuffer = BufferUtil.createBuffer(indices);
        texture = new Texture("container.png");
        texture2 = new Texture("awesomeface.png");

        // The VAO is set up and bound
        vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vao);

        // The VBO is set up and data is provided for the VBO
        vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verticesBuffer, GL33.GL_STATIC_DRAW);

        // The EBO is set up and data is provided for the EBO
        ebo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL33.GL_STATIC_DRAW);

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
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        // The memory allocated to the vertices buffer is freed
        BufferUtil.destroyBuffer(verticesBuffer);

        shaderProgram2 = new ShaderProgram();
        shaderProgram2.createVertexShader("VertexShader");
        shaderProgram2.createFragmentShader("FragmentShader");
        shaderProgram2.link();
        shaderProgram2.setUniformI("texture1", 0);
        shaderProgram2.setUniformI("texture2", 1);

        // TODO: Delete again
        float[] vertices2 = {
                // positions          // colors           // texture coords
                0.5f,  0.5f, 0.0f,    1.0f, 0.0f, 0.0f,    1.0f, 0.0f,   // top right
                0.5f, -0.5f, 0.0f,    0.0f, 1.0f, 0.0f,    1.0f, 1.0f,   // bottom right
                -0.5f, -0.5f, 0.0f,    0.0f, 0.0f, 1.0f,    0.0f, 1.0f,   // bottom left
                -0.5f,  0.5f, 0.0f,    1.0f, 1.0f, 0.0f,    0.0f, 0.0f    // top left
        };
        int[] indices2 = new int[] {  // note that we start from 0!
                0, 1, 3,   // first triangle
                1, 2, 3    // second triangle
        };
        FloatBuffer verticesBuffer2 = BufferUtil.createBuffer(vertices2);
        indicesBuffer2 = BufferUtil.createBuffer(indices2);

        vao2 = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vao2);

        vbo2 = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo2);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verticesBuffer2, GL33.GL_STATIC_DRAW);

        ebo2 = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, ebo2);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer2, GL33.GL_STATIC_DRAW);

        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, (8 * Float.BYTES), 0);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, (8 * Float.BYTES), (3 * Float.BYTES));
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, (8 * Float.BYTES), (6 * Float.BYTES));

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        BufferUtil.destroyBuffer(verticesBuffer2);
    }

    private void run() {
        while (!window.shouldClose()) {
            update();

            render();
        }

        cleanup();
    }

    private void render() {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

        shaderProgram.bind();

        // Bind to the VAO
        GL33.glBindVertexArray(vao);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glEnableVertexAttribArray(2);
        texture.bind();
        texture2.bind();

        // Draw the vertices
        GL33.glDrawElements(GL33.GL_TRIANGLES, indicesBuffer);

        // Restore state
        GL33.glDisableVertexAttribArray(0);
        GL33.glDisableVertexAttribArray(1);
        GL33.glDisableVertexAttribArray(2);
        GL33.glBindVertexArray(0);

        shaderProgram.unbind();

        Texture.unbindAll();

        shaderProgram2.bind();

        GL33.glBindVertexArray(vao2);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glEnableVertexAttribArray(2);
        texture.bind();

        GL33.glDrawElements(GL33.GL_TRIANGLES, indicesBuffer2);

        GL33.glDisableVertexAttribArray(0);
        GL33.glDisableVertexAttribArray(1);
        GL33.glDisableVertexAttribArray(2);
        GL33.glBindVertexArray(0);
        shaderProgram2.unbind();

        window.swapBuffers();
    }

    private void update() {
        if (window.hasResized()) {
            GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        }

        handleInputs();

        Matrix4f trans = new Matrix4f().identity();
        trans.translate(new Vector3f(0.5f, -0.5f, 0.0f), trans);
        trans.rotate((float) GLFW.glfwGetTime(), new Vector3f(0.0f, 0.0f, 1.0f), trans);
        float[] data = new float[4 * 4];
        trans.get(data);
        shaderProgram.setUniform4Fv("transform", data);

        Matrix4f transformation = new Matrix4f().identity();
        transformation.translate(new Vector3f(-0.5f, 0.5f, 0.0f), transformation);
        transformation.scale((float) Math.sin(GLFW.glfwGetTime()), transformation);
        float[] result = new float[4 * 4];
        transformation.get(result);
        shaderProgram2.setUniform4Fv("transform", result);

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

        GL33.glDisableVertexAttribArray(0);

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

        window.cleanUp();

        GLFW.glfwTerminate();
    }

}
