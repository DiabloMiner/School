package com.diablominer.opengl.examples.instancedrendering;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Quads {

    private static long window;
    private static int shaderProgram;
    private static int VAO;
    private static Vector2f[] translations;

    public static void main(String[] args) throws Exception {
        init();
        while (!GLFW.glfwWindowShouldClose(window)) {
            processInput();

            render();

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
        GLFW.glfwTerminate();
        GL33.glDeleteProgram(shaderProgram);
    }

    public static void init() throws Exception {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("GLFW could not be initialized");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        window = GLFW.glfwCreateWindow(1280, 720, "Hello Triangle",0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            throw new IllegalStateException("Failed to create a GLFW window");
        }
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, (videoMode.width() - 1280) / 2, (videoMode.height() - 720) / 2);

        GLFW.glfwMakeContextCurrent(window);

        GL.createCapabilities();
        GL33.glViewport(0, 0, 1280, 720);
        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                GL33.glViewport(0, 0, width, height);
            }
        });


        int vertexShader = createShader("IR_VS", GL33.GL_VERTEX_SHADER);
        int fragmentShader = createShader("IR_FS", GL33.GL_FRAGMENT_SHADER);
        shaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(shaderProgram, vertexShader);
        GL33.glAttachShader(shaderProgram, fragmentShader);
        GL33.glLinkProgram(shaderProgram);
        if (GL33.glGetProgrami(shaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(shaderProgram, 1024));
        }
        GL33.glDeleteShader(vertexShader);
        GL33.glDeleteShader(fragmentShader);

        GL33.glUseProgram(shaderProgram);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "color"), 1.0f, 0.5f, 0.7f);
        GL33.glUseProgram(0);

        translations = new Vector2f[100];
        int index = 0;
        float offset = 0.1f;
        for (int y = -10; y < 10; y += 2) {
            for (int x = -10; x < 10; x += 2) {
                Vector2f translation = new Vector2f(0.0f, 0.0f);
                translation.x = x / 10.0f + offset;
                translation.y = y / 10.0f + offset;
                translations[index++] = translation;
            }
        }
        FloatBuffer translationBuffer = MemoryUtil.memAllocFloat(translations.length * 2);
        for (Vector2f translation : translations) {
            translationBuffer.put(translation.x);
            translationBuffer.put(translation.y);
        }
        translationBuffer.flip();

        float[] vertices = {
                -0.05f,  0.05f,  1.0f, 0.0f, 0.0f,
                0.05f, -0.05f,  0.0f, 1.0f, 0.0f,
                -0.05f, -0.05f,  0.0f, 0.0f, 1.0f,

                -0.05f,  0.05f,  1.0f, 0.0f, 0.0f,
                0.05f, -0.05f,  0.0f, 1.0f, 0.0f,
                0.05f,  0.05f,  0.0f, 1.0f, 1.0f
        };
        int VBO = GL33.glGenBuffers();
        VAO = GL33.glGenVertexArrays();
        int instanceVBO = GL33.glGenBuffers();

        GL33.glBindVertexArray(VAO);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 2, GL33.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 5 * Float.BYTES, 2 * Float.BYTES);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, instanceVBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, translationBuffer, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL33.glVertexAttribDivisor(2, 1);
        GL33.glEnableVertexAttribArray(2);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);
    }

    public static void processInput() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }
    }

    public static void render() {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

        GL33.glUseProgram(shaderProgram);
        for (int i = 0; i < 100; i++) {
            GL33.glUniform2f(GL33.glGetUniformLocation(shaderProgram, "offsets[" + i + "]"), translations[i].x, translations[i].y);
        }

        GL33.glBindVertexArray(VAO);
        GL33.glDrawArraysInstanced(GL33.GL_TRIANGLES, 0, 6, 100);
        GL33.glBindVertexArray(0);
    }

    public static int createShader(String filename, int shaderType) throws Exception {
        int shader = GL33.glCreateShader(shaderType);
        GL33.glShaderSource(shader, readOutShader(filename));
        GL33.glCompileShader(shader);
        if (GL33.glGetShaderi(shader, GL33.GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + GL33.glGetShaderInfoLog(shader, 1024));
        }
        return shader;
    }

    private static String readOutShader(String filename) {
        StringBuilder string = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(new File("./src/main/java/com/diablominer/opengl/examples/instancedrendering/" + filename + ".glsl")));
            String line;
            while ((line = reader.readLine()) != null) {
                string.append(line);
                string.append("\n");
            }
            reader.close();
        } catch (IOException e) { e.printStackTrace(); }

        return string.toString();
    }

    private static IntBuffer createIntBuffer(int[] data) {
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        buffer.put(data).flip();
        return buffer;
    }

}
