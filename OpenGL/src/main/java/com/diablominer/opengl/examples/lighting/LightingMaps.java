package com.diablominer.opengl.examples.lighting;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class LightingMaps {

    private static long window;
    private static int shaderProgram, lightSourceShaderProgram;
    private static int VAO, diffuseTexture, specularTexture;
    private static float deltaTime = 0.0f, lastFrame = 0.0f;
    private static boolean firstMouse = true, constantDirection = false;
    private static float lastX = 400.0f, lastY = 300.0f, yaw, pitch = 0.0f, zoom = 45.0f;
    private static Vector3f cameraPosition, cameraDirection, cameraUp;

    public static void main(String[] args) throws Exception {
        init();
        while (!GLFW.glfwWindowShouldClose(window)) {
            float currentTime = (float) GLFW.glfwGetTime();
            deltaTime = currentTime - lastFrame;
            lastFrame = currentTime;

            processInput();

            update();

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

        window = GLFW.glfwCreateWindow(1280, 720, "Lighting Maps",0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            throw new IllegalStateException("Failed to create a GLFW window");
        }
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, (videoMode.width() - 1280) / 2, (videoMode.height() - 720) / 2);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        GLFW.glfwMakeContextCurrent(window);

        GL.createCapabilities();
        GL33.glViewport(0, 0, 1280, 720);
        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int width, int height) {
                GL33.glViewport(0, 0, width, height);
            }
        });
        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (firstMouse) {
                    lastX = (float) xpos;
                    lastY = (float) ypos;
                    firstMouse = false;
                }

                if (!constantDirection) {
                    float xOffset = (float) (xpos - lastX);
                    float yOffset = (float) (lastY - ypos);
                    lastX = (float) xpos;
                    lastY = (float) ypos;

                    float sensitivity = 0.1f;
                    xOffset *= sensitivity;
                    yOffset *= sensitivity;

                    yaw += xOffset;
                    pitch += yOffset;
                    pitch = Math.clamp(-89.0f, 89.0f, pitch);

                    cameraDirection.x = Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                    cameraDirection.y = Math.sin(Math.toRadians(pitch));
                    cameraDirection.z = Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
                    cameraDirection.normalize();
                }
            }
        });
        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                zoom -= (float) yoffset;
                zoom = Math.clamp(1.0f, 45.0f, zoom);
            }
        });

        GL33.glEnable(GL33.GL_DEPTH_TEST);


        int vertexShader = createShader("LM_VS", GL33.GL_VERTEX_SHADER);
        int fragmentShader = createShader("LM_FS", GL33.GL_FRAGMENT_SHADER);
        shaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(shaderProgram, vertexShader);
        GL33.glAttachShader(shaderProgram, fragmentShader);
        GL33.glLinkProgram(shaderProgram);
        if (GL33.glGetProgrami(shaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(shaderProgram, 1024));
        }
        GL33.glDeleteShader(fragmentShader);

        int fS = createShader("PL_FS_LightSource", GL33.GL_FRAGMENT_SHADER);
        lightSourceShaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(lightSourceShaderProgram, vertexShader);
        GL33.glAttachShader(lightSourceShaderProgram, fS);
        GL33.glLinkProgram(lightSourceShaderProgram);
        if (GL33.glGetProgrami(lightSourceShaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(shaderProgram, 1024));
        }
        GL33.glDeleteShader(vertexShader);
        GL33.glDeleteShader(fS);


        diffuseTexture = createTexture("./src/main/java/com/diablominer/opengl/examples/lighting/container2.png");
        specularTexture = createTexture("./src/main/java/com/diablominer/opengl/examples/lighting/container2_specular.png");
        GL33.glUseProgram(shaderProgram);
        GL33.glUniform1i(GL33.glGetUniformLocation(shaderProgram, "material.diffuse"), 0);
        GL33.glUniform1i(GL33.glGetUniformLocation(shaderProgram, "material.specular"), 1);
        GL33.glUseProgram(0);


        float[] vertices = {
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,
                0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  1.0f, 1.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f,  0.0f,  0.0f, -1.0f,  0.0f, 0.0f,

                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   1.0f, 1.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f,  0.0f,  0.0f, 1.0f,   0.0f, 0.0f,

                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
                -0.5f,  0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, -0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
                -0.5f,  0.5f,  0.5f, -1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,
                0.5f,  0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  0.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  1.0f,  0.0f,  0.0f,  1.0f, 0.0f,

                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,
                0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 1.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
                0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  1.0f, 0.0f,
                -0.5f, -0.5f,  0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 0.0f,
                -0.5f, -0.5f, -0.5f,  0.0f, -1.0f,  0.0f,  0.0f, 1.0f,

                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f,
                0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 1.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
                0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  1.0f, 0.0f,
                -0.5f,  0.5f,  0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 0.0f,
                -0.5f,  0.5f, -0.5f,  0.0f,  1.0f,  0.0f,  0.0f, 1.0f
        };

        int VBO = GL33.glGenBuffers();
        VAO = GL33.glGenVertexArrays();

        GL33.glBindVertexArray(VAO);

        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 8 * Float.BYTES, 0);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 8 * Float.BYTES, 3 * Float.BYTES);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 8 * Float.BYTES, 6 * Float.BYTES);

        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glEnableVertexAttribArray(2);

        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        cameraPosition = new Vector3f(0.0f, 0.0f, 3.0f);
        cameraDirection = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f cameraRight = new Vector3f();
        up.cross(cameraDirection, cameraRight);
        cameraRight.normalize();
        cameraUp = new Vector3f();
        cameraDirection.cross(cameraRight, cameraUp);

        yaw = (float) -Math.toDegrees(new Vector3f(1.0f, 0.0f, 0.0f).angle(cameraDirection));
    }

    public static void processInput() {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        final float cameraSpeed = 2.5f * deltaTime;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            Vector3f product = new Vector3f();
            cameraDirection.mul(cameraSpeed, product);
            cameraPosition.add(product);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            Vector3f product = new Vector3f();
            cameraDirection.mul(cameraSpeed, product);
            cameraPosition.sub(product);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            Vector3f product = new Vector3f();
            cameraDirection.cross(cameraUp, product);
            product.normalize().mul(cameraSpeed);
            cameraPosition.sub(product);
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            Vector3f product = new Vector3f();
            cameraDirection.cross(cameraUp, product);
            product.normalize().mul(cameraSpeed);
            cameraPosition.add(product);
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS) {
            constantDirection = !constantDirection;
        }
    }

    public static void render() {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        GL33.glUseProgram(shaderProgram);
        GL33.glBindVertexArray(VAO);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, diffuseTexture);
        GL33.glActiveTexture(GL33.GL_TEXTURE1);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, specularTexture);

        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);
        GL33.glBindVertexArray(0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);


        GL33.glUseProgram(lightSourceShaderProgram);
        GL33.glBindVertexArray(VAO);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);
        GL33.glBindVertexArray(0);
    }

    public static void update() {
        Matrix4f model = new Matrix4f().identity();
        float[] modelData = new float[4 * 4];
        model.get(modelData);

        Matrix4f view = new Matrix4f().identity();
        Vector3f sum = new Vector3f();
        cameraPosition.add(cameraDirection,sum);
        view.lookAt(cameraPosition, sum, cameraUp);
        float[] viewData = new float[4 * 4];
        view.get(viewData);

        Matrix4f projection = new Matrix4f().identity();
        projection.perspective(Math.toRadians(zoom), 1280.0f / 720.0f, 0.1f, 100.0f);
        float[] projectionData = new float[4 * 4];
        projection.get(projectionData);

        GL33.glUseProgram(shaderProgram);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "model"), false, modelData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "view"), false, viewData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "projection"), false, projectionData);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "material.specular"), 0.5f, 0.5f, 0.5f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "material.shininess"), 32.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "light.ambient"), 0.2f, 0.2f, 0.2f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "light.diffuse"), 0.8f, 0.8f, 0.8f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "light.specular"), 1.0f, 1.0f, 1.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "lightPos"), -1.5f, 1.0f, 1.5f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "viewPos"), cameraPosition.x, cameraPosition.y, cameraPosition.z);

        model.translate(new Vector3f(-1.5f, 1.0f, 1.5f));
        model.scale(0.3f);
        modelData = new float[4 * 4];
        model.get(modelData);

        GL33.glUseProgram(lightSourceShaderProgram);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(lightSourceShaderProgram, "model"), false, modelData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(lightSourceShaderProgram, "view"), false, viewData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(lightSourceShaderProgram, "projection"), false, projectionData);
        GL33.glUniform3f(GL33.glGetUniformLocation(lightSourceShaderProgram, "lightColor"), 1.0f, 1.0f, 1.0f);
        GL33.glUseProgram(0);
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
            reader = new BufferedReader(new FileReader("./src/main/java/com/diablominer/opengl/examples/lighting/" + filename + ".glsl"));
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

    private static int createTexture(String path) {
        int texture = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);

        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_REPEAT);

        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];
        STBImage.stbi_set_flip_vertically_on_load(true);
        ByteBuffer data = STBImage.stbi_load(path, width, height, channels, 4);
        if (data != null) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, width[0], height[0], 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, data);
            GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);

            STBImage.stbi_image_free(data);
        } else {
            System.err.println("Texture loading has failed");
        }

        return texture;
    }

}
