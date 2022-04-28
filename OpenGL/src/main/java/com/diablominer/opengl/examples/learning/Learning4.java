package com.diablominer.opengl.examples.learning;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Learning4 {

    private static long window;
    private static int shaderProgram, lsShaderProgram;
    private static int VAO;
    private static boolean keepWindowOpen = true;
    private static IntBuffer intBuffer;
    private static int texture1;
    private static Matrix4f model, view, projection;
    private static Vector3f cameraDirection, cameraPosition, cameraUp;
    private static float yaw, pitch, zoom;
    private static float lastMouseX = 50000f, lastMouseY = 50000f;

    private static Vector3f lightPos, lightColor;

    public static void init() throws Exception {
        if (!GLFW.glfwInit()) {
            System.err.println("GLFW could not be initialised.");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        window = GLFW.glfwCreateWindow(1280, 720, "Learning4", 0, 0);
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


        int vShader = createShader("L4VS", GL33.GL_VERTEX_SHADER);
        int fShader = createShader("L4FS", GL33.GL_FRAGMENT_SHADER);
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


        texture1 = createTexture("./src/main/java/com/diablominer/opengl/examples/basicopengl/hellotexture/container.png");
        GL33.glUseProgram(shaderProgram);
        GL33.glUniform1i(GL33.glGetUniformLocation(shaderProgram, "texture1"), 0);
        GL33.glUseProgram(0);


        int[] indices = {
                0, 1, 3,
                1, 2, 3
        };
        float[] vertices = {
                0.5f,  0.5f, 0.0f,
                0.5f, -0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                -0.5f,  0.5f, 0.0f
        };
        float[] textureCoords = {
                1.0f, 1.0f,
                1.0f, 0.0f,
                0.0f, 0.0f,
                0.0f, 1.0f
        };
        float[] normals = {
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f,
                0.0f, 0.0f, 1.0f
        };
        FloatBuffer fBuffer1 = createFloatBuffer(vertices);
        FloatBuffer fBuffer2 = createFloatBuffer(normals);
        FloatBuffer fBuffer3 = createFloatBuffer(textureCoords);
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

        int TBO = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, TBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fBuffer3, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 2 * Float.BYTES, 0);
        GL33.glEnableVertexAttribArray(2);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);

        GL33.glBindVertexArray(0);
        MemoryUtil.memFree(fBuffer1);
        MemoryUtil.memFree(fBuffer2);


        lightColor = new Vector3f(0.2f, 0.7f, 0.2f);
        lightPos = new Vector3f(0.0f, 5.0f, 0.0f);

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

        GL33.glUniform3f((GL33.glGetUniformLocation(shaderProgram, "lightColor")), lightColor.x, lightColor.y, lightColor.z);
        GL33.glUniform3f((GL33.glGetUniformLocation(shaderProgram, "lightPos")), lightPos.x, lightPos.y, lightPos.z);
        GL33.glUniform3f((GL33.glGetUniformLocation(shaderProgram, "viewPos")), cameraPosition.x, cameraPosition.y, cameraPosition.z);
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
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

        GL33.glUseProgram(shaderProgram);
        GL33.glBindVertexArray(VAO);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture1);

        GL33.glDrawElements(GL33.GL_TRIANGLES, intBuffer);
        GL33.glBindVertexArray(0);

        GL33.glUseProgram(lsShaderProgram);
        GL33.glBindVertexArray(VAO);
        GL33.glDrawElements(GL33.GL_TRIANGLES, intBuffer);
        GL33.glBindVertexArray(0);

        GLFW.glfwSwapBuffers(window);
    }

    public static void processInput() {
        float factor = 0.1f;
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
        GL33.glDeleteTextures(texture1);
        MemoryUtil.memFree(intBuffer);
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

    private static int createTexture(String path) {
        int texture = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);

        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_REPEAT);

        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);

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
