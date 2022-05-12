package com.diablominer.opengl.examples.learning;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.Arrays;

public class Learning6 {

    public static boolean continueEngineLoop = true, resize = false;
    private static float deltaTime = 0.0f, lastFrame = 0.0f;
    private static Matrix4f view, projection;
    private static Window window;
    private static Camera camera;
    private static ExpandedUniformBufferBlock matricesUniforms, lightUniforms;
    private static SingleFramebufferRenderer mainRenderer;
    private static BlurRenderer blurRenderer;
    private static EventManager eventManager;
    private static Framebuffer intermediateFb;
    private static QuadRenderingEngineUnit quadRenderingEngineUnit;

    public static void init() throws Exception {
        if (!GLFW.glfwInit()) {
            System.err.println("GLFW could not be initialised.");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        camera = new Camera(new Vector3f(0.0f, 0.0f, 3.0f), new Vector3f(0.0f, 0.0f, -1.0f));
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

        ShaderProgram shaderProgram = new ShaderProgram("L6VS", "L6FS");
        ShaderProgram lsShaderProgram = new ShaderProgram("L6VS", "L4FS_LS");
        ShaderProgram simpleShaderProgram = new ShaderProgram("L6SVS", "L6SFS");
        ShaderProgram blurShaderProgram = new ShaderProgram("L6SVS", "L6_GaussianBlur");

        DirectionalLight dirLight = new DirectionalLight(new Vector3f(-0.7f, 1.0f, 2.9f), new Vector3f(0.0f, 0.0f, 0.2f));
        RenderablePointLight pointLight = new RenderablePointLight(new Vector3f(0.0f, 5.0f, 0.0f), new Vector3f(0.0f, 50.0f, 38.0f));
        SpotLight spotLight = new CameraUpdatedSpotLight(new Vector3f(camera.position), new Vector3f(camera.direction), new Vector3f(0.8f, 0.0f, 0.0f));

        Model helloWorld = new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", new Matrix4f().identity().rotate(Math.toRadians(-55.0f), new Vector3f(1.0f, 0.0f, 0.0f).normalize()));

        mainRenderer = new SingleFramebufferRenderer(new FramebufferTexture2D[] {new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT1)},
                new FramebufferRenderbuffer[] {new FramebufferRenderbuffer(GL33.GL_DEPTH24_STENCIL8, window.width, window.height, 4, FramebufferAttachment.DEPTH_AND_STENCIL_ATTACHMENT)},
                new RenderingEngineUnit[] {new StandardRenderingEngineUnit(shaderProgram, new Renderable[] {helloWorld}), new LightRenderingEngineUnit(lsShaderProgram)});
        intermediateFb = new Framebuffer(new FramebufferTexture2D[] {new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, FramebufferAttachment.COLOR_ATTACHMENT1)},
                new FramebufferRenderbuffer[] {new FramebufferRenderbuffer(GL33.GL_DEPTH24_STENCIL8, window.width, window.height, FramebufferAttachment.DEPTH_AND_STENCIL_ATTACHMENT)});
        blurRenderer = new BlurRenderer(window.width, window.height, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, blurShaderProgram, 10, intermediateFb.getAttached2DTextures().get(1));
        quadRenderingEngineUnit = new QuadRenderingEngineUnit(simpleShaderProgram, new ArrayList<>(Arrays.asList(intermediateFb.getAttached2DTextures().get(0), blurRenderer.getFinalFramebuffer().getAttached2DTextures().get(0))));

        matricesUniforms = new ExpandedUniformBufferBlock(1, 2, GL33.GL_DYNAMIC_DRAW, "Matrices");
        lightUniforms = new ExpandedUniformBufferBlock(7, 0, GL33.GL_DYNAMIC_DRAW, "Lights");
        shaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms, lightUniforms});
        lsShaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms, lightUniforms});

        view = new Matrix4f().identity();
        view.lookAt(camera.position, camera.getFront(), camera.up);
        projection = new Matrix4f().identity();
        projection.perspective(Math.toRadians(camera.fov), (float) window.width / (float) window.height, 0.1f, 100.0f);
    }

    public static void update() {
        if (resize) {
            resize();
            resize = false;
        }

        getEventManager().executeEvents();

        view = new Matrix4f().identity();
        view.lookAt(camera.position, camera.getFront(), camera.up);
        projection = new Matrix4f().identity();
        projection.perspective(Math.toRadians(camera.fov), (float) window.width / (float) window.height, 0.1f, 100.0f);

        matricesUniforms.setData(new Vector4f[] {new Vector4f(camera.position, 0.0f)}, new Matrix4f[]{view, projection});
        matricesUniforms.setUniformBlockData();
        lightUniforms.setData(Light.getDataOfAllLights(), new Matrix4f[]{});
        lightUniforms.setUniformBlockData();
    }

    public static void render() {
        mainRenderer.render();

        Framebuffer.blitFrameBuffers(mainRenderer.getFramebuffer(), intermediateFb);

        blurRenderer.render();

        Framebuffer.getStandardFramebuffer().bind();
        GL33.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glEnable(GL33.GL_FRAMEBUFFER_SRGB);

        quadRenderingEngineUnit.render();

        GL33.glDisable(GL33.GL_FRAMEBUFFER_SRGB);
        window.swapBuffers();
    }

    public static void resize() {
        for (Framebuffer framebuffer : Framebuffer.allFramebuffers) {
            framebuffer.resize(window.width, window.height);
            framebuffer.bind();
            GL33.glViewport(0, 0, window.width, window.height);
        }
        Framebuffer.unbind();
    }

    public static void processInput() {
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

    public static void main(String[] args) throws Exception {
        init();
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

    public static void close() {
        window.shouldClose();
        window.destroy();
        ShaderProgram.destroyAll();
        Renderable.destroyAll();
        Framebuffer.destroyAll();
        Buffer.destroyAll();
        VAO.destroyAll();
        Texture.destroyAllTextures();
        Renderbuffer.destroyAll();
        GLFW.glfwTerminate();
    }

    public static EventManager getEventManager() {
        if (eventManager == null) {
            eventManager = new EventManager();
        }
        return eventManager;
    }

}
