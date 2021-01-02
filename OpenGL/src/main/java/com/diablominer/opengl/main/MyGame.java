package com.diablominer.opengl.main;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.*;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.RenderablePointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.renderables.Renderable;
import com.diablominer.opengl.render.textures.CubeMap;
import com.diablominer.opengl.render.textures.Texture;
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

    private int frameBuffer, frameBuffer2, frameBuffer3;
    private int texColorBuffer, texColorBuffer2, texColorBuffer3;
    private int VAO;
    private ShaderProgram sP, reflectionShaderProgram;
    private Renderable reflectionCube;
    private Camera environmentMappingCamera;

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

        camera = new Camera(45.0f, new Vector3f(1.0f, 1.0f, 5.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f, 0.0f), 1280.0f / 720.0f);
        environmentMappingCamera = new Camera(90.0f, new Vector3f(-15.0f, 0.0f, 20.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f), 1.0f);
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

        renderingEngine = new MyRenderingEngine();
        logicalEngine = new MyLogicalEngine(true);

        ShaderProgram shaderProgram = new ShaderProgram("VertexShader", "FragmentShader");
        ShaderProgram lightSourceShaderProgram = new ShaderProgram("VertexShader", "LightSourceFragmentShader");
        ShaderProgram oneColorShaderProgram = new ShaderProgram("VertexShader", "OneColorShader");
        reflectionShaderProgram = new ShaderProgram("VertexShader", "ReflectionFragmentShader");

        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1.0f, 0.0f, 1.0f), new Vector3f(0.1f, 0.1f, 0.2f), new Vector3f(0.3f, 0.3f, 0.3f),  new Vector3f(0.8f, 0.8f, 0.8f));
        PointLight pointLight = new PointLight(new Vector3f(-8.0f, 2.0f, -2.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.22f, 0.20f);
        SpotLight spotLight = new SpotLight(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.35f, 0.7f, (float) Math.cos(Math.toRadians(17.5f)), (float) Math.cos(Math.toRadians(19.5f)));

        StencilTestRenderingEngineUnit renderingEngineUnit0 = new StencilTestRenderingEngineUnit(shaderProgram, directionalLight , pointLight, spotLight);
        MyRenderingEngineUnit renderingEngineUnit1 = new MyRenderingEngineUnit(shaderProgram, directionalLight , pointLight, spotLight);
        RenderingEngineUnit renderingEngineUnit2 = new RenderingEngineUnit(lightSourceShaderProgram) {
            @Override
            public void updateRenderState(Camera camera) {
                this.getShaderProgram().setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, camera.aspect, 0.1f, 100.0f));
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
            public void updateRenderState(Camera camera) {
                this.getShaderProgram().setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, camera.aspect, 0.1f, 100.0f));
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
        MyRenderingEngineUnit reflectionRenderingEngineUnit = new MyRenderingEngineUnit(reflectionShaderProgram, directionalLight, pointLight, spotLight);

        new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", renderingEngineUnit1, new Vector3f(0.0f, 0.0f, 0.0f));
        new Model("./src/main/resources/models/HelloWorld/cube.obj", renderingEngineUnit0, new Vector3f(8.0f, 0.0f, 25.0f));
        new Model("./src/main/resources/models/HelloWorld/biggerCube.obj", renderingEngineUnit3, new Vector3f(8.0f, 0.0f, 25.0f));
        reflectionCube = new Model("./src/main/resources/models/HelloWorld/cube.obj", reflectionRenderingEngineUnit, new Vector3f(-8.0f, 0.0f, 20.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, -1.0f, 12.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, 1.0f, 15.0f));
        new RenderablePointLight(pointLight, "./src/main/resources/models/HelloWorld/cube.obj", logicalEngine, renderingEngineUnit2);

        renderingEngine.addNewEngineUnit(renderingEngineUnit0);
        renderingEngine.addNewEngineUnit(renderingEngineUnit1);
        renderingEngine.addNewEngineUnit(renderingEngineUnit2);
        renderingEngine.addNewEngineUnit(renderingEngineUnit3);
        renderingEngine.addNewEngineUnit(reflectionRenderingEngineUnit);

        Runnable logicalEngineRunnable = logicalEngine;
        Thread logicalEngineThread = new Thread(logicalEngineRunnable);
        logicalEngineThread.start();

        float[] skyboxVertices = {
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
        CubeMap cubeMap = new CubeMap("./src/main/resources/textures/skybox", ".jpg");
        ShaderProgram skyboxShaderProgram = new ShaderProgram("SkyboxVertexShader", "SkyboxFragmentShader");
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
                cubeMap.bind();
                shaderProgram.setUniform1I("skybox", CubeMap.getIndexForTexture(cubeMap));
                shaderProgram.bind();
                GL33.glBindVertexArray(VAO2);
                GL33.glEnableVertexAttribArray(0);

                GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);

                GL33.glDisableVertexAttribArray(0);
                GL33.glBindVertexArray(0);
                shaderProgram.unbind();
                GL33.glDepthFunc(GL33.GL_LESS);
            }

            @Override
            public void destroy() {
                GL33.glDeleteVertexArrays(VAO2);
                GL33.glDeleteBuffers(VBO2);
            }
        };

        RenderingEngineUnit skyboxRenderingEngineUnit = new RenderingEngineUnit(skyboxShaderProgram) {
            @Override
            public void updateRenderState(Camera camera) {
                Matrix4f viewMatrix = new Matrix4f().identity();
                viewMatrix.set3x3(new Matrix4f().identity().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp));
                shaderProgram.setUniformMat4F("view", viewMatrix);
                shaderProgram.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, camera.aspect, 0.1f, 100.0f));
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
        GL33.glViewport(0, 0, 1280, 1280);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer3);

        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X, texColorBuffer3, 0);
        environmentMappingCamera.cameraPos = reflectionCube.getPosition();
        environmentMappingCamera.cameraFront = new Vector3f(1.0f, 0.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, -1.0f, 0.0f);
        renderingEngine.updateAllEngineUnits(environmentMappingCamera);
        renderingEngine.renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(-1.0f, 0.0f, 0.0f);
        renderingEngine.updateAllEngineUnits(environmentMappingCamera);
        renderingEngine.renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 1.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, 0.0f, 1.0f);
        renderingEngine.updateAllEngineUnits(environmentMappingCamera);
        renderingEngine.renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, -1.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, 0.0f, -1.0f);
        renderingEngine.updateAllEngineUnits(environmentMappingCamera);
        renderingEngine.renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 0.0f, 1.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, -1.0f, 0.0f);
        renderingEngine.updateAllEngineUnits(environmentMappingCamera);
        renderingEngine.renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        renderingEngine.updateAllEngineUnits(environmentMappingCamera);
        renderingEngine.renderWithoutRenderable(window, reflectionCube);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        reflectionShaderProgram.setUniform1I("skybox", 0);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glViewport(0, 0, 1280, 720);
        renderingEngine.updateAllEngineUnits(camera);
        renderingEngine.render(window);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        reflectionShaderProgram.setUniform1I("skybox", 0);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);
        GL33.glViewport(0, 0, 320, 180);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        camera.cameraFront = Transforms.getProductOf2Vectors(camera.cameraFront, new Vector3f(-1.0f));
        renderingEngine.updateAllEngineUnits(camera);
        renderingEngine.render(window);
        camera.cameraFront = Transforms.getProductOf2Vectors(camera.cameraFront, new Vector3f(-1.0f));
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
        renderingEngine.updateAllEngineUnits(camera);

        handleInputs();

        window.update();

        GLFW.glfwPollEvents();
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
        Texture.destroyAllTextures();
        CubeMap.destroyAllCubeMaps();

        logicalEngine.setShouldRun(false);

        renderingEngine.destroy();
        GLFW.glfwDestroyWindow(window.getWindow());

        GLFW.glfwTerminate();
    }
}
