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
        Camera camera = new Camera(new Vector3f(-1.5f, 0.5f, -0.2f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
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
                new Component[]{new TransformComponent(new Matrix4d().translate(0.0, 0.10715, 200 * -0.1143).scale(0.05715)),
                        new AssimpModel("./src/main/resources/models/HelloWorld/sphere2.obj", new Matrix4f().translate(0.0f, 0.10715f, -0.1143f).scale(0.05715f), true),
                        new PhysicsSphere(Material.Ball, ObjectType.DYNAMIC, new Vector3d(0.0, 0.10715, 200 * -0.1143), new Vector3d(0.0, 0.0, 0.0),  new Quaterniond().identity(), new Vector3d(0.0), new HashSet<>(Collections.singletonList(new Gravity())), 0.163, 0.05715)});
        Entity physicsSphere3 = new Entity("2", new Component.Type[]{Component.Type.Transform, Component.Type.Render, Component.Type.Physics},
                new Component[]{new TransformComponent(new Matrix4d().translate(-0.05715, 0.10715, -0.1143 - (0.05715 * Math.sqrt(3.0))).scale(0.05715)),
                        new AssimpModel("./src/main/resources/models/HelloWorld/sphere2.obj", new Matrix4f().translate(-0.05715f, 0.10715f, -0.1143f - (0.05715f * (float) Math.sqrt(3.0))).scale(0.05715f), true),
                        new PhysicsSphere(Material.Ball, ObjectType.DYNAMIC, new Vector3d(-0.05715, 0.10715, -0.1143 - 1 * (0.05715 * Math.sqrt(3.0))), new Vector3d(0.0, 0.0, 0.0),  new Quaterniond().identity(), new Vector3d(0.0), new HashSet<>(Collections.singletonList(new Gravity())), 0.163, 0.05715)});
        Entity physicsSphere4 = new Entity("2", new Component.Type[]{Component.Type.Transform, Component.Type.Render, Component.Type.Physics},
                new Component[]{new TransformComponent(new Matrix4d().translate(0.05715, 0.10715, -0.1143 - (0.05715 * Math.sqrt(3.0))).scale(0.05715)),
                        new AssimpModel("./src/main/resources/models/HelloWorld/sphere2.obj", new Matrix4f().translate(0.05715f, 0.10715f, -0.1143f - (0.05715f * (float) Math.sqrt(3.0))).scale(0.05715f), true),
                        new PhysicsSphere(Material.Ball, ObjectType.DYNAMIC, new Vector3d(0.05715, 0.10715, -0.1143 - 1 * (0.05715 * Math.sqrt(3.0))), new Vector3d(0.0, 0.0, 0.0),  new Quaterniond().identity(), new Vector3d(0.0), new HashSet<>(Collections.singletonList(new Gravity())), 0.163, 0.05715)});
        Entity physicsSphere2 = new Entity("3", new Component.Type[]{Component.Type.Transform, Component.Type.Render, Component.Type.Physics},
                new Component[]{new TransformComponent(new Matrix4d().translate(0.0, 0.10715, 0.0).scale(0.05715)),
                        new AssimpModel("./src/main/resources/models/HelloWorld/sphere2.obj", new Matrix4f().translate(0.0f, 0.10715f, 0.0f).scale(0.05715f), true),
                        new PhysicsSphere(Material.Ball, ObjectType.DYNAMIC, new Vector3d(0.0, 0.10715, 1.0), new Vector3d(0.0, 0.0, -3.0),  new Quaterniond().identity(), new Vector3d(3 * (3.0/ 0.05715), 0.0, 0.0), new HashSet<>(Collections.singletonList(new Gravity())), 0.163, 0.05715)});
        Entity plate = new Entity("4", new Component.Type[]{Component.Type.Transform, Component.Type.Render, Component.Type.Physics},
                new Component[]{new TransformComponent(new Matrix4d().translate(new Vector3d(0.0, 0.0, 0.0)).scale(1.0, 0.5, 1.0)),
                    new AssimpModel("./src/main/resources/models/HelloWorld/billardPlate.obj", new Matrix4f().translate(0.0f, 0.0f, 0.0f).scale(1.0f, 0.5f, 1.0f), true),
                    new PhysicsBox(new Matrix4d().translate(0.0, 0.0, 0.0), new Vector3d(1.378, 0.05, 2.648), new Vector3d(2.756, 0.1, 5.296), Material.Cloth, ObjectType.STATIC, new Vector3d(), new Vector3d(), new HashSet<>(), 5.97219e24)});
        // Changed the sorting algorithm because a smaller timestep was sorted after a larger one
        // Original desynchronization of objects comes from different forces applied when timestep > 0
        // Change the update of earlier objects to actually work
        // Added rounding in application of impulse to prevent numerical errors
        // Moved coefficient determination and tangent generation to collision
        // Changed tangent generation: Made to be abstract in collision
        // Allowed the camera to have a pitch of 90 degrees and start in position looking down
        // Changed calculation of angular velocity in Collision to normalize the direction vectors
        // Added rotational friction according to the SIGGRAPH paper though the implementation is based on essentially guessing that the resulting impulse should be applied among the direction
        // Angular velocity in willCollide and collectCollisionData was removed; Changed collision vector computation in collectCollisionData to compute from B to A instead of A to B
        // TODO: Test multi-body collisions: Test current friction some more; Just add rails + queue to test
        // TODO: Fix full screen bug: Seems like frame buffers are acting weird, Maybe quad isnt resized
        // TODO: Add billard plates: Floor + 4 rails
        entities.addAll(Arrays.asList(helloWorld, cube, physicsSphere1, physicsSphere2, physicsSphere3, physicsSphere4, plate));
    }

    public void mainLoop() {
        while (continueEngineLoop) {
            long currentTime = System.currentTimeMillis();
            long deltaTime = (currentTime - lastFrame) / 4;
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
