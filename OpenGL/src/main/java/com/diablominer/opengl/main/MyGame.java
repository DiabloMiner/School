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

public class MyGame implements Game {

    private Camera camera;
    private Window window;
    private MyRenderingEngine renderingEngine;
    private MyLogicalEngine logicalEngine;
    private float deltaTime = 0.0f;
    private float lastTime = 0.0f;

    private int frameBuffer, frameBuffer2;
    private int texColorBuffer, texColorBuffer2;
    private int VAO;
    private ShaderProgram sP;

    public static void main(String[] args) throws Exception {
        new MyGame();
    }

    public MyGame() {
        try { init(); } catch (Exception e) { System.err.println("An exception has occurred while initializing the game: " + e.getMessage()); }
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

        camera = new Camera(45.0f, new Vector3f(1.0f, 1.0f, 5.0f), new Vector3f(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        window = new Window(1280, 720, "OpenGL", camera);
        GLFW.glfwMakeContextCurrent(window.getWindow());

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
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_KEEP);
                renderAllRenderables();
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);
                GL33.glStencilFunc(GL33.GL_ALWAYS, 0, 0x00);
            }
        };
        TransparencyRenderingEngineUnit transparencyRenderingEngineUnit = new TransparencyRenderingEngineUnit(shaderProgram, directionalLight, pointLight, spotLight);
        new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", renderingEngineUnit1, new Vector3f(0.0f, 0.0f, 0.0f));
        new Model("./src/main/resources/models/HelloWorld/cube.obj", renderingEngineUnit0, new Vector3f(0.0f, 0.0f, 25.0f));
        new Model("./src/main/resources/models/HelloWorld/biggerCube.obj", renderingEngineUnit3, new Vector3f(0.0f, 0.0f, 25.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, -1.0f, 12.5f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, 1.0f, 15.5f));
        new RenderablePointLight(pointLight, "./src/main/resources/models/HelloWorld/cube.obj", logicalEngine, renderingEngineUnit2);
        renderingEngine.addNewEngineUnit(renderingEngineUnit0);
        renderingEngine.addNewEngineUnit(renderingEngineUnit1);
        renderingEngine.addNewEngineUnit(renderingEngineUnit2);
        renderingEngine.addNewEngineUnit(renderingEngineUnit3);

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

        texColorBuffer2 = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer2);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, 320, 180, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);

        frameBuffer2 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, texColorBuffer2, 0);

        int RBO2 = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO2);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, 320, 180);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO2);

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
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        CubeMap cubeMap = new CubeMap("./src/main/resources/textures/skybox", ".jpg");
        ShaderProgram skyboxShaderProgram = new ShaderProgram("SkyboxVertexShader", "SkyboxFragmentShader");
        float[] skyboxVertices = {
                // positions
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f
        };
        int VAO2 = GL33.glGenVertexArrays();
        int VBO2 = GL33.glGenBuffers();
        GL33.glBindVertexArray(VAO2);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO2);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, skyboxVertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);
        Renderable skybox = new Renderable(new Vector3f(0.0f, 0.0f, 0.0f)) {
            @Override
            public void draw(ShaderProgram shaderProgram) {
                GL33.glDepthFunc(GL33.GL_LEQUAL);
                GL33.glActiveTexture(GL33.GL_TEXTURE0);
                cubeMap.bind();
                shaderProgram.setUniform1I("skybox", 0);
                shaderProgram.bind();
                GL33.glBindVertexArray(VAO2);
                GL33.glEnableVertexAttribArray(0);

                GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);

                GL33.glDisableVertexAttribArray(0);
                GL33.glBindVertexArray(0);
                shaderProgram.unbind();
                CubeMap.unbindAll();
                GL33.glDepthFunc(GL33.GL_LESS);
                GL33.glEnable(GL33.GL_STENCIL_TEST);
            }

            @Override
            public void cleanUp() {
                GL33.glDeleteVertexArrays(VAO2);
                GL33.glDeleteBuffers(VBO2);
            }
        };

        RenderingEngineUnit skyboxRenderingEngineUnit = new RenderingEngineUnit(skyboxShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, Window window) {
                Matrix4f viewMatrix = new Matrix4f().identity();
                viewMatrix.set3x3(new Matrix4f().identity().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp));
                shaderProgram.setUniformMat4F("view", viewMatrix);
                shaderProgram.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
            }

            @Override
            public void render() {
                this.renderAllRenderables();
            }
        };
        skyboxRenderingEngineUnit.addNewRenderable(skybox);
        renderingEngine.addNewEngineUnit(skyboxRenderingEngineUnit);
        renderingEngine.addNewEngineUnit(transparencyRenderingEngineUnit);
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

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glViewport(0, 0, 320, 180);
        camera.changeYaw(-180.0f);
        renderingEngine.updateAllEngineUnits(camera, window);
        renderingEngine.render(window);
        camera.changeYaw(180.0f);
        GL33.glViewport(0, 0, 1280, 720);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer);
        sP.setUniform1I("screenTexture", 0);
        sP.bind();
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);
        GL33.glBindVertexArray(0);
        sP.unbind();

        GL33.glViewport(480, 540, 320, 180);
        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer2);
        sP.setUniform1I("screenTexture", 0);
        sP.bind();
        GL33.glBindVertexArray(VAO);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);
        GL33.glDisableVertexAttribArray(0);
        GL33.glDisableVertexAttribArray(1);
        GL33.glBindVertexArray(0);
        sP.unbind();
        GL33.glViewport(0, 0, 1280, 720);

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

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
        GLFW.glfwDestroyWindow(window.getWindow());

        logicalEngine.setShouldRun(false);

        GL33.glDeleteFramebuffers(this.frameBuffer);

        renderingEngine.end();

        GLFW.glfwTerminate();
    }
}
