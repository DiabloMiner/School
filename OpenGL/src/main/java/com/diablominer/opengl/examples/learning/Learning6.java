package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

public class Learning6 {

    public static Learning6 engineInstance;

    public boolean continueEngineLoop = true, resize = false;
    private float deltaTime = 0.0f, lastFrame = 0.0f;
    private Window window;
    private EventManager eventManager;
    private MainRenderingEngine mainRenderingEngine;

    public static void main(String[] args) throws Exception {
        engineInstance = new Learning6();
        engineInstance.init();
        engineInstance.mainLoop();
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
        GL33.glCullFace(GL33.GL_BACK);

        mainRenderingEngine = new MainRenderingEngine(window, camera);
    }

    public void mainLoop() {
        while (continueEngineLoop) {
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

    public void processInput() {
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

    public void update() {
        if (resize) {
            resize();
            resize = false;
        }

        getEventManager().executeEvents();

        mainRenderingEngine.update();
    }

    public void resize() {
        for (Framebuffer framebuffer : Framebuffer.allFramebuffers) {
            framebuffer.resize(window.width, window.height);
            framebuffer.bind();
            GL33.glViewport(0, 0, window.width, window.height);
        }
        Framebuffer.unbind();
    }

    public void render() {
        mainRenderingEngine.render();
    }

    public void close() {
        window.shouldClose();
        window.destroy();
        mainRenderingEngine.destroy();

        RenderingEngine.destroyAllRenderingEngines();
        Renderer.destroyAllRenderers();
        ShaderProgram.destroyAll();
        Renderable.destroyAll();
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

    public Window getWindow() {
        return this.window;
    }

    public MainRenderingEngine getMainRenderingEngine() {
        return mainRenderingEngine;
    }

}
