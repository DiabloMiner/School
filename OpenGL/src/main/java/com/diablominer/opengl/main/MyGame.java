package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.render.textures.Texture;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

public class MyGame implements Game {

    private MyRenderingEngine renderingEngine;
    private MyLogicalEngine logicalEngine;
    private int frames = 0;
    private long frameTime;
    private long lastTime;
    private long accumulator;

    public static final long millisecondsPerFrame = 16;
    public static final long millisecondsPerSimulationFrame = 10;

    public static void main(String[] args) throws Exception {
        new MyGame();
    }

    public MyGame() throws Exception {
        init();
        mainLoop();
        cleanUp();
    }

    @Override
    public void init() throws Exception {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);

        Camera camera = new Camera(45.0f, new Vector3f(1.0f, 1.0f, 5.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f, 0.0f), 1280.0f / 720.0f);
        Window window = new Window(1280, 720, "OpenGL", camera);

        GLFW.glfwMakeContextCurrent(window.getId());
        GL.createCapabilities();
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
        GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glCullFace(GL33.GL_BACK);
        GL33.glFrontFace(GL33.GL_CCW);
        GL33.glEnable(GL33.GL_MULTISAMPLE);
        GL33.glEnable(GL33.GL_TEXTURE_CUBE_MAP_SEAMLESS);

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

        logicalEngine = new MyLogicalEngine();
        renderingEngine = new MyRenderingEngine(logicalEngine, window, camera);

        frameTime = System.currentTimeMillis();
        lastTime = System.currentTimeMillis();
    }

    @Override
    public void mainLoop() throws Exception {
        while (!renderingEngine.getWindow().shouldClose()) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            accumulator += deltaTime;

            renderingEngine.handleInputs(((float) deltaTime) / 1000.0f);

            while (accumulator >= millisecondsPerSimulationFrame) {
                update(millisecondsPerSimulationFrame / 1000.0);
                accumulator -= millisecondsPerSimulationFrame;
            }

            render(accumulator / 1000.0);

            sleep(currentTime + millisecondsPerFrame - System.currentTimeMillis());
        }
    }

    public void render(double leftOverTime) {
        logicalEngine.predict(leftOverTime);
        renderingEngine.update();
        renderingEngine.render();

        if (System.currentTimeMillis() >= (frameTime + 1000)) {
            System.out.println("FPS: " + frames / ((System.currentTimeMillis() - frameTime) / 1000));
            frameTime = System.currentTimeMillis();
            frames = 0;
        } else {
            frames++;
        }
    }

    public void update(double time) {
        logicalEngine.update(time);

        GLFW.glfwPollEvents();
    }

    @Override
    public void cleanUp() {
        Texture.destroyAllTextures();

        renderingEngine.destroy();

        GLFW.glfwTerminate();
    }

    private void sleep(long time) throws InterruptedException {
        if (time > 0) {
            Thread.sleep(time);
        }
    }
}
