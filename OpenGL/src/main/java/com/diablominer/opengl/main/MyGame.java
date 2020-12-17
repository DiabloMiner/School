package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

public class MyGame extends Game {

    private Camera camera;
    private Window window;
    private MyEngine engine;
    private float deltaTime = 0.0f;
    private float lastTime = 0.0f;

    public static void main(String[] args) throws Exception {
        new MyGame();
    }

    public MyGame() throws Exception {
        init();
        mainLoop();
    }

    @Override
    public void init() throws Exception {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Failed to initialize GLFW");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);

        camera = new Camera(45.0f, new Vector3f(20.0f, 1.0f, 20.0f), new Vector3f(-1.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        window = new Window(1280, 720, "OpenGL", camera);

        GL.createCapabilities();
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        GL33.glEnable(GL33.GL_DEPTH_TEST);

        ShaderProgram shaderProgram = new ShaderProgram("VertexShader", "FragmentShader");
        ShaderProgram lightSourceShaderProgram = new ShaderProgram("VertexShader", "LightSourceFragmentShader");
        engine = new MyEngine();
        MyEngine.pointLight.setPosition(Transforms.getProductOf2Vectors(MyEngine.pointLight.getPosition(), new Vector3f((float) Math.cos(GLFW.glfwGetTime()), (float) Math.sin(GLFW.glfwGetTime()), (float) Math.cos(GLFW.glfwGetTime()))));
        EngineUnit engineUnit1 = new MyEngineUnit(shaderProgram, MyEngine.directionLight, MyEngine.pointLight, MyEngine.spotLight);
        engineUnit1.addNewRenderable(new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", engineUnit1, new Matrix4f().translate(new Vector3f(1.0f, 0.0f, 3.0f))));
        engineUnit1.addNewRenderable(new Model("./src/main/resources/models/HelloWorld/cube.obj", engineUnit1, new Matrix4f().translate(new Vector3f(2.0f, 1.0f, 0.0f))));
        EngineUnit engineUnit2 = new EngineUnit(lightSourceShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, Window window) {
                this.getShaderProgram().setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
                Matrix4f view = new Matrix4f().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp);
                this.getShaderProgram().setUniformMat4F("view", view);
                this.getShaderProgram().setUniformVec3F("viewPos", camera.cameraPos);
                this.getShaderProgram().setUniformVec3F("color", 1.0f, 1.0f, 1.0f);
            }

            @Override
            public void render() {
                renderAllRenderables();
            }
        };
        engineUnit2.addNewRenderable(new Model("./src/main/resources/models/HelloWorld/cube.obj", engineUnit1, new Matrix4f().translate(MyEngine.pointLight.getPosition())));
        engine.addNewEngineUnit(engineUnit1);
        engine.addNewEngineUnit(engineUnit2);
    }

    @Override
    public void mainLoop() {
        while (!window.shouldClose()) {
            float currentTime = (float) GLFW.glfwGetTime();
            deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            update();

            render();
        }
        cleanUp();
    }

    public void render() {
        engine.render(window);
    }

    public void update() {
        engine.updateAllEngineUnits(camera, window);

        handleInputs();

        window.update();
    }

    public void handleInputs() {
        float cameraSpeed = 10.0f * deltaTime;

        if (window.getInput().isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            GLFW.glfwSetWindowShouldClose(window.getWindow(), true);
            GLFW.glfwDestroyWindow(window.getWindow());
        }

        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_W)) {
            camera.moveForwards(cameraSpeed);
        }
        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_S)) {
            camera.moveBackwards(cameraSpeed);
        }
        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_A)) {
            camera.moveLeft(cameraSpeed);
        }
        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_D)) {
            camera.moveRight(cameraSpeed);
        }
    }

    @Override
    public void cleanUp() {
        engine.end();

        GLFW.glfwTerminate();
    }
}
