package com.diablominer.opengl.examples.learning;

import org.jblas.DoubleMatrix;
import org.jblas.NativeBlas;
import org.joml.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryUtil;

import java.lang.Math;
import java.util.*;
import java.util.stream.Collectors;

public class Learning6 extends Engine {

    public static Learning6 engineInstance;
    public static final long millisecondsPerFrame = 15;
    public static double maxTimeStep = 0.01, minTimeStep = 0.0005;

    public boolean continueEngineLoop = true, resize = false;
    private long lastFrame = 0, accumulator = 0;
    private EventManager eventManager;
    private MainRenderingEngine mainRenderingEngine;
    private MainPhysicsEngine mainPhysicsEngine;
    private MainIOEngine mainIOEngine;

    public static void main(String[] args) throws Exception {
        initializeJBlas();
        engineInstance = new Learning6();
        engineInstance.init();
        engineInstance.mainLoop();
        engineInstance.close();
    }

    public static void initializeJBlas() {
        // A new NativeBlas instance is created to force the loading of the library, so the Engine doesn't experience a lag spike while running
        NativeBlas nativeBlas = new NativeBlas();
        // A new JBlas Matrix is created to force the initialization of the JBlas matrix class, so the Engine doesn't experience a lag spike while running
        DoubleMatrix doubleMatrix = new DoubleMatrix(1, 1);
        // Different matrix operations are performed as there seems to be some loading time connected with the first use of a function
        doubleMatrix.mmul(DoubleMatrix.ones(1, 1));
        DoubleMatrix.zeros(1, 2).mmul(DoubleMatrix.ones(2, 1));
        DoubleMatrix.zeros(2, 2).subi(DoubleMatrix.ones(2, 2));
    }

    public Learning6() {
        super();
    }

    public void init() throws Exception {
        if (!GLFW.glfwInit()) {
            System.err.println("GLFW could not be initialised.");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        Camera camera = new Camera(new Vector3f(0.0f, 7.0f, 3.0f), new Vector3f(0.0f, 0.0f, -1.0f));
        Window window = new Window(1280, 720, false, "Learning6", camera);
        Window.setFocusedWindow(window);
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
        GL43.glEnable(GL43.GL_DEBUG_OUTPUT);
        GL43.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_MULTISAMPLE);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glEnable(GL33.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        GL33.glCullFace(GL33.GL_BACK);

        loadEntities();

        List<Entity> renderedEntities = entities.stream().filter(entity -> entity.hasComponent(Component.Type.Render)).collect(Collectors.toList());
        List<Entity> physicsEntities = entities.stream().filter(entity -> entity.hasComponent(Component.Type.Physics)).collect(Collectors.toList());
        mainPhysicsEngine = new MainPhysicsEngine(new MinMapNewtonConfiguration(25, 30, 10e-4, 0.5, 10e-10, 10e-50, 10e-20, false), physicsEntities, 0.01);
        mainRenderingEngine = new MainRenderingEngine(renderedEntities, window, camera);
        mainIOEngine = new MainIOEngine(new Window[]{window}, new Camera[]{camera}, new RenderingEngine[]{mainRenderingEngine});

        lastFrame = System.currentTimeMillis();
    }

    private void loadEntities() {
        Entity helloWorld = new Entity("0", new Component.Type[]{Component.Type.Transform, Component.Type.Render},
                new Component[] {new TransformComponent(new Matrix4d().identity().rotate(Math.toRadians(-55.0), new Vector3d(1.0, 0.0, 0.0))),
                        new AssimpModel("./src/main/resources/models/HelloWorld/HelloWorld.obj", new Matrix4f().identity().rotate((float) Math.toRadians(-55.0f), new Vector3f(1.0f, 0.0f, 0.0f)), true)});
        Entity cube = new Entity("1", new Component.Type[]{Component.Type.Transform, Component.Type.Render},
                new Component[]{new TransformComponent(new Matrix4d().identity().translate(new Vector3d(3.4, -1.5, -4.4))),
                        new AssimpModel("./src/main/resources/models/HelloWorld/cube3.obj", new Matrix4f().identity().translate(new Vector3f(3.4f, -1.5f, -4.4f)), true)});
        Entity physicsSphere1 = new Entity("2", new Component.Type[]{Component.Type.Transform, Component.Type.Render, Component.Type.Physics},
                new Component[]{new TransformComponent(new Matrix4d().translate(0.0, 8.0, 0.0)),
                        new AssimpModel("./src/main/resources/models/HelloWorld/sphere2.obj", new Matrix4f().translate(0.0f, 8.0f, 0.0f), true),
                        new PhysicsSphere(ObjectType.DYNAMIC, new Vector3d(0.0, 8.0, 0.0), new Vector3d(0.0, 0.0, 0.0),  new Quaterniond().identity(), new Vector3d(0.0), new HashSet<>(Collections.singletonList(new Gravity())), 10.0, 1.0, 1.0, 0.1, 0.14)});
        Entity physicsSphere2 = new Entity("3", new Component.Type[]{Component.Type.Transform, Component.Type.Render, Component.Type.Physics},
                new Component[]{new TransformComponent(new Matrix4d().translate(0.0, 6.0, 0.0)),
                        new AssimpModel("./src/main/resources/models/HelloWorld/sphere2.obj", new Matrix4f().translate(0.0f, 6.0f, 0.0f), true),
                        new PhysicsSphere(ObjectType.STATIC, new Vector3d(0.0, 6.0, 0.0), new Vector3d(0.0, 0.0, 0.0),  new Quaterniond().identity(), new Vector3d(0.0), new HashSet<>(), Double.POSITIVE_INFINITY, 1.0, 1.0, 0.1, 0.14)});
        entities.addAll(Arrays.asList(helloWorld, cube, physicsSphere1, physicsSphere2));
    }

    public void mainLoop() {
        while (continueEngineLoop) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = currentTime - lastFrame;
            lastFrame = currentTime;
            accumulator += deltaTime;

            accumulator = update(accumulator, deltaTime / 1000.0);
            render(accumulator / 1000.0);

            GLFW.glfwPollEvents();

            sleep(currentTime + millisecondsPerFrame - System.currentTimeMillis());
        }
    }

    public long update(double accumulator, double deltaTime) {
        mainIOEngine.processInputs(deltaTime);
        getEventManager().executeEvents();

        while (accumulator >= (mainPhysicsEngine.simulationTimeStep * 1000)) {
            mainPhysicsEngine.update();
            accumulator -= ((mainPhysicsEngine.simulationTimeStep + mainPhysicsEngine.getLeftOverTime()) * 1000);
        }
        return (long) accumulator;
    }

    public void render(double leftOverTime) {
        mainPhysicsEngine.predictTimeStep(leftOverTime / 1000.0);

        mainRenderingEngine.updateEntities();
        mainRenderingEngine.update();

        mainRenderingEngine.render();
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
        Texture.unbindAllTextures();
        AssimpModel.loadedTextures.clear();

        mainRenderingEngine.destroy();
        mainPhysicsEngine.destroy();
        mainIOEngine.destroy();

        ShaderProgramManager.destroyAllStaticShaderPrograms();
        RenderComponentManager.destroyAllStaticRenderables();
        Texture.destroyAllTextures();
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
