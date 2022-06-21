package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

public class Learning6 implements Engine {

    public static Learning6 engineInstance;
    public static final long millisecondsPerFrame = 17;
    public static final long millisecondsPerSimulationStep = 10;

    public boolean continueEngineLoop = true, resize = false;
    private long deltaTime = 0, lastFrame = 0, accumulator = 0;
    private Window window;
    private EventManager eventManager;
    private MainRenderingEngine mainRenderingEngine;
    private MainPhysicsEngine mainPhysicsEngine;

    public static void main(String[] args) throws Exception {
        engineInstance = new Learning6();
        engineInstance.init();
        engineInstance.mainLoop();
        engineInstance.close();
    }

    public Learning6() {}

    public void init() throws Exception {
        if (!GLFW.glfwInit()) {
            System.err.println("GLFW could not be initialised.");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        Camera camera = new Camera(new Vector3f(0.0f, 0.0f, 3.0f), new Vector3f(0.0f, 0.0f, -1.0f));
        window = new Window(1280, 720, "Learning6", camera);
        GLFW.glfwMakeContextCurrent(window.getId());

        GL.createCapabilities();
        GL33.glViewport(0, 0, window.width, window.height);
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
        GL33.glEnable(GL33.GL_MULTISAMPLE);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glEnable(GL33.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        GL33.glCullFace(GL33.GL_BACK);

        mainPhysicsEngine = new MainPhysicsEngine();
        mainRenderingEngine = new MainRenderingEngine(window, camera);

        lastFrame = System.currentTimeMillis();
    }

    public void mainLoop() {
        while (continueEngineLoop) {
            long currentTime = System.currentTimeMillis();
            deltaTime = currentTime - lastFrame;
            lastFrame = currentTime;
            accumulator += deltaTime;

            resize();
            processInput(deltaTime / 1000.0f);

            while (accumulator >= millisecondsPerSimulationStep) {
                update(millisecondsPerSimulationStep / 1000.0);
                accumulator -= millisecondsPerSimulationStep;
            }
            render(accumulator / 1000.0);

            GLFW.glfwPollEvents();

            sleep(currentTime + millisecondsPerFrame - System.currentTimeMillis());
        }
    }

    public void processInput(float deltaTime) {
        float factor = 4.0f * deltaTime;
        if (window.isKeyPressed(GLFW.GLFW_KEY_W)) {
            getEventManager().executeEvent(new KeyPressEvent(factor, GLFW.GLFW_KEY_W));
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_S)) {
            getEventManager().executeEvent(new KeyPressEvent(factor, GLFW.GLFW_KEY_S));
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_A)) {
            getEventManager().executeEvent(new KeyPressEvent(factor, GLFW.GLFW_KEY_A));
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_D)) {
            getEventManager().executeEvent(new KeyPressEvent(factor, GLFW.GLFW_KEY_D));
        }

        if (window.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            continueEngineLoop = false;
        }
        if (window.isKeyPressed(GLFW.GLFW_KEY_F11)) {
            window.setFullscreen(!window.isFullscreen());
        }
    }

    public void update(double deltaTime) {
        mainPhysicsEngine.update(deltaTime);

        getEventManager().executeEvents();

        mainRenderingEngine.update();
    }

    public void render(double leftOverTime) {
        mainPhysicsEngine.predictTimeStep(leftOverTime);

        mainRenderingEngine.render();
    }

    public void resize() {
        if (resize) {
            mainRenderingEngine.resize();
            resize = false;
        }
    }

    private void sleep(long time) {
        if (time > 0) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        window.shouldClose();
        window.destroy();
        mainRenderingEngine.destroy();

        ShaderProgramManager.destroyAllStaticShaderPrograms();
        RenderingEngine.destroyAllRenderingEngines();
        Renderer.destroyAllRenderers();
        Framebuffer.destroyAll();
        Buffer.destroyAll();
        VAO.destroyAll();
        Texture.destroyAllTextures();
        Renderbuffer.destroyAll();
        GLFW.glfwTerminate();
    }

    public EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new EventManager();
        }
        return eventManager;
    }

    public MainPhysicsEngine getMainPhysicsEngine() {
        return mainPhysicsEngine;
    }

}
