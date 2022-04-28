package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.examples.modelloading.Model;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Learning5 {

    private static long window;
    private static int shaderProgram, lsShaderProgram;
    private static boolean keepWindowOpen = true;
    private static Matrix4f model, view, projection;
    private static float deltaTime = 0.0f, lastFrame = 0.0f;
    private static Vector3f cameraDirection, cameraPosition, cameraUp;
    private static float yaw, pitch, zoom;
    private static float lastMouseX = 50000f, lastMouseY = 50000f;
    private static Model mod1, lsMod;

    private static Vector3f lightPos, lightColor;

    public static void init() throws Exception {
        if (!GLFW.glfwInit()) {
            System.err.println("GLFW could not be initialised.");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        window = GLFW.glfwCreateWindow(1280, 720, "Learning5", 0, 0);
        if (window == 0) {
            GLFW.glfwTerminate();
            System.err.println("Window could not be created.");
        }
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        GLFW.glfwSetWindowPos(window, (videoMode.width() - 1280) / 2, (videoMode.height() - 720) / 2);
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        GL.createCapabilities();
        GL33.glViewport(0, 0, 1280, 720);

        GLDebugMessageCallback callback = new GLDebugMessageCallback() {
            @Override
            public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
                if (severity != GL43.GL_DEBUG_SEVERITY_NOTIFICATION) {
                    System.err.println("|  Error / Debug message  |");
                    switch (source) {
                        case GL43.GL_DEBUG_SOURCE_API:
                            System.err.println("Source: API");
                            break;
                        case GL43.GL_DEBUG_SOURCE_WINDOW_SYSTEM:
                            System.err.println("Source: Window System");
                            break;
                        case GL43.GL_DEBUG_SOURCE_SHADER_COMPILER:
                            System.err.println("Source: Shader Compiler");
                            break;
                        case GL43.GL_DEBUG_SOURCE_THIRD_PARTY:
                            System.err.println("Source: Third Party");
                            break;
                        case GL43.GL_DEBUG_SOURCE_APPLICATION:
                            System.err.println("Source: Application");
                            break;
                        case GL43.GL_DEBUG_SOURCE_OTHER:
                            System.err.println("Source: Other");
                            break;
                    }

                    switch (type) {
                        case GL43.GL_DEBUG_TYPE_ERROR:
                            System.err.println("Type: Error");
                            break;
                        case GL43.GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
                            System.err.println("Type: Deprecated Behavior");
                            break;
                        case GL43.GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
                            System.err.println("Type: Undefined Behavior");
                            break;
                        case GL43.GL_DEBUG_TYPE_PORTABILITY:
                            System.err.println("Type: Portability");
                            break;
                        case GL43.GL_DEBUG_TYPE_PERFORMANCE:
                            System.err.println("Type: Performance");
                            break;
                        case GL43.GL_DEBUG_TYPE_MARKER:
                            System.err.println("Type: Marker");
                            break;
                        case GL43.GL_DEBUG_TYPE_PUSH_GROUP:
                            System.err.println("Type: Push Group");
                            break;
                        case GL43.GL_DEBUG_TYPE_POP_GROUP:
                            System.err.println("Type: Pop Group");
                            break;
                        case GL43.GL_DEBUG_TYPE_OTHER:
                            System.err.println("Type: Other");
                            break;
                    }

                    switch (severity) {
                        case GL43.GL_DEBUG_SEVERITY_HIGH:
                            System.err.println("Severity: High");
                            break;
                        case GL43.GL_DEBUG_SEVERITY_MEDIUM:
                            System.err.println("Severity: Medium");
                            break;
                        case GL43.GL_DEBUG_SEVERITY_LOW:
                            System.err.println("Severity: Low");
                            break;
                        case GL43.GL_DEBUG_SEVERITY_NOTIFICATION:
                            System.err.println("Severity: Notification");
                            break;
                    }
                    System.err.println("Message: " + MemoryUtil.memUTF8(message));
                }
            }
        };
        GL43.glDebugMessageCallback(callback, 0);
        GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        GL33.glEnable(GL33.GL_DEPTH_TEST);


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
        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (lastMouseX == 50000f) {
                    lastMouseX = (float) xpos;
                    lastMouseY = (float) ypos;
                }

                float sensitivity = 0.1f;

                float deltaX = (float) (xpos - lastMouseX) * sensitivity;
                float deltaY = (float) (lastMouseY - ypos) * sensitivity;
                lastMouseX = (float) xpos;
                lastMouseY = (float) ypos;

                yaw += deltaX;
                pitch += deltaY;
                pitch = Math.clamp(-89.0f, 89.0f, pitch);

                cameraDirection.set(Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch)), Math.sin(Math.toRadians(pitch)), Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch))).normalize();
            }
        });
        GLFW.glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                zoom -= (float) yoffset;
                zoom = Math.clamp(1.0f, 45.0f, zoom);
            }
        });


        int vShader = createShader("L5VS", GL33.GL_VERTEX_SHADER);
        int fShader = createShader("L5FS", GL33.GL_FRAGMENT_SHADER);
        shaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(shaderProgram, vShader);
        GL33.glAttachShader(shaderProgram, fShader);
        GL33.glLinkProgram(shaderProgram);
        if (GL33.glGetProgrami(shaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(shaderProgram, 1024));
        }
        GL33.glDeleteShader(vShader);
        GL33.glDeleteShader(fShader);

        int vShader2 = createShader("L4VS", GL33.GL_VERTEX_SHADER);
        int fShader2 = createShader("L4FS_LS", GL33.GL_FRAGMENT_SHADER);
        lsShaderProgram = GL33.glCreateProgram();
        GL33.glAttachShader(lsShaderProgram, vShader2);
        GL33.glAttachShader(lsShaderProgram, fShader2);
        GL33.glLinkProgram(lsShaderProgram);
        if (GL33.glGetProgrami(lsShaderProgram, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(lsShaderProgram, 1024));
        }
        GL33.glDeleteShader(vShader2);
        GL33.glDeleteShader(vShader2);


        mod1 = new Model("./src/main/resources/models/backpack/backpack.obj");
        lsMod = new Model("./src/main/java/com/diablominer/opengl/examples/models/cube/cube.obj");


        lightColor = new Vector3f(0.2f, 0.7f, 0.2f);
        lightPos = new Vector3f(0.0f, 10.0f, 0.0f);

        yaw = -90.0f;
        pitch = 0.0f;
        zoom = 45.0f;
        cameraPosition = new Vector3f(0.0f, 0.0f, 3.0f);
        cameraDirection = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f cameraRight = new Vector3f(up).cross(cameraDirection).normalize();
        cameraUp = new Vector3f(cameraDirection).cross(cameraRight);
        model = new Matrix4f().identity();
        model.rotate(Math.toRadians(-55.0f), new Vector3f(1.0f, 0.0f, 0.0f).normalize());
    }

    public static void update() {
        float[] modelData = new float[4 * 4];
        model.get(modelData);

        view = new Matrix4f().identity();
        view.lookAt(cameraPosition, new Vector3f(cameraPosition).add(cameraDirection), cameraUp);
        float[] viewData = new float[4 * 4];
        view.get(viewData);

        projection = new Matrix4f().identity();
        projection.perspective(Math.toRadians(zoom), 1280.0f / 720.0f, 0.1f, 100.0f);
        float[] projectionData = new float[4 * 4];
        projection.get(projectionData);

        GL33.glUseProgram(shaderProgram);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "model"), false, modelData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "view"), false, viewData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(shaderProgram, "projection"), false, projectionData);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "viewPos"), cameraPosition.x, cameraPosition.y, cameraPosition.z);

        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "material.shininess"), 32.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.direction"), -0.7f, 1.0f, 2.9f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.ambient"), 0.2f, 0.2f, 0.2f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.diffuse"), 0.8f, 0.8f, 0.8f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myDirLight.specular"), 1.0f, 1.0f, 1.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.position"), -2.0f, 1.0f, 2.0f);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.ambient"), lightColor.x, lightColor.y, lightColor.z);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.diffuse"), lightColor.x, lightColor.y, lightColor.z);
        GL33.glUniform3f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.specular"), lightColor.x, lightColor.y, lightColor.z);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.constant"), 1.0f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.linear"), 5.8f);
        GL33.glUniform1f(GL33.glGetUniformLocation(shaderProgram, "myPointLight.quadratic"), 10.0f);
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
        GL33.glUseProgram(0);

        Matrix4f model2 = new Matrix4f().identity().scale(0.2f).translate(lightPos);
        float[] modelData2 = new float[4 * 4];
        model2.get(modelData2);

        GL33.glUseProgram(lsShaderProgram);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(lsShaderProgram, "model"), false, modelData2);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(lsShaderProgram, "view"), false, viewData);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(lsShaderProgram, "projection"), false, projectionData);

        GL33.glUniform3f((GL33.glGetUniformLocation(lsShaderProgram, "lightColor")), lightColor.x, lightColor.y, lightColor.z);
        GL33.glUseProgram(0);

    }

    public static void render() {
        GL33.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        mod1.draw(shaderProgram);
        lsMod.draw(lsShaderProgram);

        GLFW.glfwSwapBuffers(window);
    }

    public static void processInput() {
        float factor = 2.0f * deltaTime;
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) {
            cameraPosition.add(new Vector3f(cameraDirection).mul(factor));
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) {
            cameraPosition.sub(new Vector3f(cameraDirection).mul(factor));
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) {
            Vector3f sideDir = new Vector3f(cameraDirection).cross(cameraUp);
            cameraPosition.sub(new Vector3f(sideDir).mul(factor));
        }
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) {
            Vector3f sideDir = new Vector3f(cameraDirection).cross(cameraUp);
            cameraPosition.add(new Vector3f(sideDir).mul(factor));
        }

        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS) {
            keepWindowOpen = false;
        }
    }

    public static void main(String[] args) throws Exception {
        init();
        while (keepWindowOpen) {
            float currentTime = (float) GLFW.glfwGetTime();
            deltaTime = currentTime - lastFrame;
            lastFrame = currentTime;

            processInput();

            update();
            render();

            GLFW.glfwPollEvents();
        }
        close();
    }

    public static void close() {
        GLFW.glfwTerminate();
        GL33.glDeleteProgram(shaderProgram);
        GL33.glDeleteProgram(lsShaderProgram);
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

}
