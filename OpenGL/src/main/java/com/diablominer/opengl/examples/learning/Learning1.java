package com.diablominer.opengl.examples.learning;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Learning1 {

    private static long window;
    private static int shaderProgram;
    private static int VAO;
    private static boolean keepWindowOpen = true;
    private static IntBuffer intBuffer;

    public static void init() throws Exception {
        if (!GLFW.glfwInit()) {
            System.err.println("GLFW could not be initialised.");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        window = GLFW.glfwCreateWindow(1280, 720, "Learning1", 0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            System.err.println("Window could not be created.");
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
        GLFW.glfwSetWindowCloseCallback(window, new GLFWWindowCloseCallback() {
            @Override
            public void invoke(long window) {
                keepWindowOpen = false;
            }
        });

        int vShader = createShader("L1VS", GL33.GL_VERTEX_SHADER);
        int fShader = createShader("L1FS", GL33.GL_FRAGMENT_SHADER);
        shaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(shaderProgram, vShader);
        GL33.glAttachShader(shaderProgram, fShader);
        GL33.glLinkProgram(shaderProgram);
        if (GL33.glGetProgrami(shaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(shaderProgram, 1024));
        }
        GL33.glDeleteShader(vShader);
        GL33.glDeleteShader(fShader);

        int[] indices = {
                0, 1, 2
        };
        float[] data1 = {
                0.5f, 0.0f, 0.0f,
                -0.5f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f,
        };
        float[] data2 = {
                0.2f, 0.7f, 0.1f,
                0.2f, 0.7f, 0.1f,
                0.2f, 0.7f, 0.1f,
        };
        FloatBuffer fBuffer1 = createFloatBuffer(data1);
        FloatBuffer fBuffer2 = createFloatBuffer(data2);
        intBuffer = createIntBuffer(indices);

        VAO = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(VAO);

        int EBO = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, EBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, intBuffer, GL33.GL_STATIC_DRAW);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);

        int buffer1 = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, buffer1);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fBuffer1, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);

        int buffer2 = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, buffer2);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fBuffer2, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);

        GL33.glBindVertexArray(0);
        MemoryUtil.memFree(fBuffer1);
        MemoryUtil.memFree(fBuffer2);
    }

    public static void render() {
        GL33.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

        GL33.glUseProgram(shaderProgram);
        GL33.glBindVertexArray(VAO);
        GL33.glDrawElements(GL33.GL_TRIANGLES, intBuffer);
        GL33.glBindVertexArray(0);

        GLFW.glfwSwapBuffers(window);
    }

    public static void processInput() {

    }

    public static void main(String[] args) throws Exception {
        init();
        while (keepWindowOpen) {
            processInput();

            render();

            GLFW.glfwPollEvents();
        }
        close();
    }

    public static void close() {
        GLFW.glfwTerminate();
        GL33.glDeleteProgram(shaderProgram);
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
            reader = new BufferedReader(new FileReader(new File("./src/main/java/com/diablominer/opengl/examples/learning/" + filename + ".glsl")));
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

    private static FloatBuffer createFloatBuffer(float[] data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        return buffer;
    }

}
