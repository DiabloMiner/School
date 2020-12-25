package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.RenderablePointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class MyGame extends Game {

    private Camera camera;
    private Window window;
    private MyRenderingEngine renderingEngine;
    private MyLogicalEngine logicalEngine;
    private float deltaTime = 0.0f;
    private float lastTime = 0.0f;

    private int frameBuffer;
    private int texColorBuffer;
    private int VAO;
    private ShaderProgram sP;

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

        camera = new Camera(45.0f, new Vector3f(20.0f, 1.0f, 20.0f), new Vector3f(-1.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        window = new Window(1280, 720, "OpenGL", camera);

        GL.createCapabilities();
        GL33.glViewport(0, 0, window.getWIDTH(), window.getHEIGHT());
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glCullFace(GL33.GL_BACK);
        GL33.glFrontFace(GL33.GL_CCW);

        ShaderProgram shaderProgram = new ShaderProgram("VertexShader", "FragmentShader");
        ShaderProgram lightSourceShaderProgram = new ShaderProgram("VertexShader", "LightSourceFragmentShader");
        ShaderProgram oneColorShaderProgram = new ShaderProgram("VertexShader", "OneColorShader");
        renderingEngine = new MyRenderingEngine();
        logicalEngine = new MyLogicalEngine(true);
        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1.0f, 0.0f, 1.0f), new Vector3f(0.1f, 0.1f, 0.2f), new Vector3f(0.3f, 0.3f, 0.3f),  new Vector3f(0.8f, 0.8f, 0.8f));
        PointLight pointLight = new PointLight(new Vector3f(-8.0f, 2.0f, -2.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.22f, 0.20f);
        SpotLight spotLight = new SpotLight(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.35f, 0.7f, (float) Math.cos(Math.toRadians(17.5f)), (float) Math.cos(Math.toRadians(19.5f)));
        StencilTestRenderingEngineUnit renderingEngineUnit0 = new StencilTestRenderingEngineUnit(shaderProgram, directionalLight , pointLight, spotLight);
        MyRenderingEngineUnit renderingEngineUnit1 = new MyRenderingEngineUnit(shaderProgram, directionalLight , pointLight, spotLight);
        RenderingEngineUnit renderingEngineUnit2 = new RenderingEngineUnit(lightSourceShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, Window window) {
                this.getShaderProgram().setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
                Matrix4f view = new Matrix4f().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp);
                this.getShaderProgram().setUniformMat4F("view", view);
                this.getShaderProgram().setUniformVec3F("color", 1.0f, 1.0f, 1.0f);
            }

            @Override
            public void render() {
                renderAllRenderables();
            }
        };
        RenderingEngineUnit renderingEngineUnit3 = new RenderingEngineUnit(oneColorShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, Window window) {
                this.getShaderProgram().setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
                Matrix4f view = new Matrix4f().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp);
                this.getShaderProgram().setUniformMat4F("view", view);
            }

            @Override
            public void render() {
                GL33.glStencilFunc(GL33.GL_NOTEQUAL, 1, 0xFF);
                GL33.glDisable(GL33.GL_DEPTH_TEST);
                renderAllRenderables();
                GL33.glStencilFunc(GL33.GL_ALWAYS, 0, 0x00);
                GL33.glEnable(GL33.GL_DEPTH_TEST);
            }
        };
        TransparencyRenderingEngineUnit transparencyRenderingEngineUnit = new TransparencyRenderingEngineUnit(shaderProgram, directionalLight, pointLight, spotLight);
        /*new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", renderingEngineUnit1, new Vector3f(0.0f, 0.0f, 0.0f));
        new Model("./src/main/resources/models/HelloWorld/cube.obj", renderingEngineUnit0, new Vector3f(0.0f, 0.0f, 25.0f));
        new Model("./src/main/resources/models/HelloWorld/biggerCube.obj", renderingEngineUnit3, new Vector3f(0.0f, 0.0f, 25.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, -1.0f, 12.5f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, 1.0f, 15.5f));
        new RenderablePointLight(pointLight, "./src/main/resources/models/HelloWorld/cube.obj", logicalEngine, renderingEngineUnit2);
        renderingEngine.addNewEngineUnit(renderingEngineUnit0);*/
        new Model("./src/main/resources/models/cube/simpleCube.obj", renderingEngineUnit1, new Vector3f(0.0f, 0.0f, 0.0f));
        renderingEngine.addNewEngineUnit(renderingEngineUnit1);
        /*renderingEngine.addNewEngineUnit(renderingEngineUnit2);
        renderingEngine.addNewEngineUnit(transparencyRenderingEngineUnit);
        renderingEngine.addNewEngineUnit(renderingEngineUnit3);*/

        Runnable logicalEngineRunnable = logicalEngine;
        Thread logicalEngineThread = new Thread(logicalEngineRunnable);
        logicalEngineThread.start();

        texColorBuffer = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, 1280, 720, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);

        frameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, texColorBuffer, 0);

        int RBO = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, 1280, 720);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO);

        if(GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("The framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(frameBuffer));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        sP = new ShaderProgram("SimpleVertexShader", "SimpleFragmentShader");
        float[] quadVertices = {
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };
        VAO = GL33.glGenVertexArrays();
        int VBO = GL33.glGenBuffers();

        GL33.glBindVertexArray(VAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, quadVertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL33.glVertexAttribPointer(1, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);
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
    }

    public void render() {
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        renderingEngine.render(window);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glEnable(GL33.GL_TEXTURE_2D);

        GL33.glActiveTexture(0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer);
        sP.setUniform1I("screenTexture", 0);

        sP.bind();
        GL33.glBindVertexArray(VAO);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);
        GL33.glBindVertexArray(0);
        sP.unbind();

        /*GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_LINE);
        GL33.glBlitFramebuffer(0, 0, 1280,720, 0,0, 1280,720, GL33.GL_COLOR_BUFFER_BIT, GL33.GL_LINEAR);*/

        GLFW.glfwSwapBuffers(window.getWindow());
    }

    public void update() {
        renderingEngine.updateAllEngineUnits(camera, window);

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
        logicalEngine.setShouldRun(false);

        GL33.glDeleteFramebuffers(this.frameBuffer);

        renderingEngine.end();

        GLFW.glfwTerminate();
    }
}
