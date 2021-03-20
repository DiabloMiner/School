package com.diablominer.opengl.examples.advancedlighting;

import com.diablominer.opengl.examples.modelloading.Model;
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

public class AdvancedLighting {

    private static long window;
    private static int shaderProgram, lightSourceShaderProgram, shadowShaderProgram;
    private static float deltaTime = 0.0f, lastFrame = 0.0f;
    private static boolean firstMouse = true, constantDirection = false;
    private static float lastX = 400.0f, lastY = 300.0f, yaw, pitch = 0.0f, zoom = 45.0f;
    private static Vector3f cameraPosition, cameraDirection, cameraUp;
    private static Model model, lightSourceModel;

    private static int depthMapFBO, depthMap;

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

        window = GLFW.glfwCreateWindow(1280, 720, "Advanced Lighting",0, 0);
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


        int vertexShader = createShader("AL_VS", GL33.GL_VERTEX_SHADER);
        int fragmentShader = createShader("AL_FS", GL33.GL_FRAGMENT_SHADER);
        shaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(shaderProgram, vertexShader);
        GL33.glAttachShader(shaderProgram, fragmentShader);
        GL33.glLinkProgram(shaderProgram);
        if (GL33.glGetProgrami(shaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(shaderProgram, 1024));
        }
        GL33.glDeleteShader(fragmentShader);

        int fS = createShader("AL_FS_LightSource", GL33.GL_FRAGMENT_SHADER);
        lightSourceShaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(lightSourceShaderProgram, vertexShader);
        GL33.glAttachShader(lightSourceShaderProgram, fS);
        GL33.glLinkProgram(lightSourceShaderProgram);
        if (GL33.glGetProgrami(lightSourceShaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(lightSourceShaderProgram, 1024));
        }
        GL33.glDeleteShader(vertexShader);
        GL33.glDeleteShader(fS);

        int shadowVS = createShader("DM_VS", GL33.GL_VERTEX_SHADER);
        int shadowFS = createShader("DM_FS", GL33.GL_FRAGMENT_SHADER);
        shadowShaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(shadowShaderProgram, vertexShader);
        GL33.glAttachShader(shadowShaderProgram, fragmentShader);
        GL33.glLinkProgram(shadowShaderProgram);
        if (GL33.glGetProgrami(shadowShaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(shadowShaderProgram, 1024));
        }
        GL33.glDeleteShader(shadowVS);
        GL33.glDeleteShader(shadowFS);


        model = new Model("./src/main/resources/models/backpack/backpack.obj");
        lightSourceModel = new Model("./src/main/java/com/diablominer/opengl/examples/models/cube/cube.obj");


        cameraPosition = new Vector3f(0.0f, 0.0f, 3.0f);
        cameraDirection = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f cameraRight = new Vector3f();
        up.cross(cameraDirection, cameraRight);
        cameraRight.normalize();
        cameraUp = new Vector3f();
        cameraDirection.cross(cameraRight, cameraUp);

        yaw = (float) -Math.toDegrees(new Vector3f(1.0f, 0.0f, 0.0f).angle(cameraDirection));


        depthMapFBO = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, depthMapFBO);

        depthMap = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, depthMap);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_DEPTH_COMPONENT, 1024, 1024, 0, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, 0);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameterfv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_BORDER_COLOR, new float[] { 1.0f, 1.0f, 1.0f, 1.0f});
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_ATTACHMENT, GL33.GL_TEXTURE_2D, depthMap, 0);
        GL33.glDrawBuffer(GL33.GL_NONE);
        GL33.glReadBuffer(GL33.GL_NONE);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
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
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, depthMapFBO);
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        model.draw(shadowShaderProgram);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        GL33.glUseProgram(shaderProgram);
        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, depthMap);
        GL33.glUniform1i(GL33.glGetUniformLocation(shaderProgram, "shadowMap"), 0);

        model.draw(shaderProgram);
    }

    public static void update() {
        float nearPlane = 1.0f, farPlane = 10.0f;
        Matrix4f lightProjection = new Matrix4f().identity().ortho(-10.0f, 10.0f, -10.0f, 10.0f, nearPlane, farPlane);
        Matrix4f lightView = new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 2.0f), new Vector3f(0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        Matrix4f lightSpaceMatrix = new Matrix4f().identity();
        lightSpaceMatrix.mul(lightProjection);
        lightSpaceMatrix.mul(lightView);
        float[] lightSpaceData = new float[4 * 4];
        lightSpaceMatrix.get(lightSpaceData);

        Matrix4f model = new Matrix4f().identity();
        float[] modelData = new float[4 * 4];
        model.get(modelData);

        GL33.glUseProgram(shadowShaderProgram);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shadowShaderProgram, "model"), false, modelData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shadowShaderProgram, "lightSpaceMatrix"), false, lightSpaceData);

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
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "lightSpaceMatrix"), false, lightSpaceData);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "viewPos"), cameraPosition.x, cameraPosition.y, cameraPosition.z);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "material.specular"), 0.5f, 0.5f, 0.5f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "material.shininess"), 32.0f);

        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.direction"), 0.0f, 0.0f, -1.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.ambient"), 0.2f, 0.2f, 0.2f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.diffuse"), 0.8f, 0.8f, 0.8f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.specular"), 1.0f, 1.0f, 1.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.position"), -2.0f, 1.0f, 2.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.ambient"), 0.2f, 0.2f, 0.2f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.diffuse"), 0.8f, 0.8f, 0.8f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.specular"), 1.0f, 1.0f, 1.0f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.constant"), 1.0f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.linear"), 0.7f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.quadratic"), 1.8f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.position"), cameraPosition.x, cameraPosition.y, cameraPosition.z);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.direction"), cameraDirection.x, cameraDirection.y, cameraDirection.z);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.ambient"), 0.2f, 0.2f, 0.2f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.diffuse"), 0.8f, 0.8f, 0.8f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.specular"), 1.0f, 1.0f, 1.0f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.cutOff"), Math.cos(Math.toRadians(12.5f)));
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.outerCutOff"), Math.cos(Math.toRadians(17.5f)));
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.constant"), 1.0f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.linear"), 0.14f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "mySpotLight.quadratic"), 0.07f);
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
            reader = new BufferedReader(new FileReader("./src/main/java/com/diablominer/opengl/examples/advancedlighting/" + filename + ".glsl"));
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
