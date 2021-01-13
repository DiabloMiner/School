package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.main.LogicalEngine;
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
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class MyRenderingEngine extends RenderingEngine {

    private Camera camera;
    private Window window;
    private int frameBuffer, frameBuffer2, frameBuffer3, frameBuffer4;
    private int texColorBuffer, texColorBuffer2, texColorBuffer3, texColorBuffer4;
    private int VAO;
    private ShaderProgram sP, reflectionShaderProgram, refractionShaderProgram, shaderProgram, environmentMappingShaderProgram;
    private Renderable[] notToBeRendered;
    private Camera environmentMappingCamera;
    private float deltaTime = 0.0f;

    public MyRenderingEngine(LogicalEngine logicalEngine, Window window, Camera camera) throws Exception {
        this.window = window;
        this.camera = camera;
        environmentMappingCamera = new Camera(90.0f, new Vector3f(-15.0f, 0.0f, 20.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f), 1.0f);

        shaderProgram = new ShaderProgram("VertexShader", "FragmentShader");
        ShaderProgram lightSourceShaderProgram = new ShaderProgram("VertexShader", "LightSourceFragmentShader");
        ShaderProgram oneColorShaderProgram = new ShaderProgram("VertexShader", "OneColorShader");
        reflectionShaderProgram = new ShaderProgram("VertexShader", "ReflectionFragmentShader");
        refractionShaderProgram = new ShaderProgram("VertexShader", "RefractionFragmentShader");
        environmentMappingShaderProgram = new ShaderProgram("EnvironmentMappingVertexShader", "EnvironmentMappingGeometryShader", "EnvironmentMappingFragmentShader");

        DirectionalLight directionalLight = new DirectionalLight(new Vector3f(1.0f, -1.0f, 1.0f), new Vector3f(0.1f, 0.1f, 0.1f), new Vector3f(0.3f, 0.3f, 0.3f),  new Vector3f(0.8f, 0.8f, 0.8f));
        PointLight pointLight = new PointLight(new Vector3f(-8.0f, 2.0f, -2.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.22f, 0.20f);
        SpotLight spotLight = new SpotLight(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.35f, 0.7f, (float) Math.cos(Math.toRadians(17.5f)), (float) Math.cos(Math.toRadians(19.5f)));

        StencilTestRenderingEngineUnit renderingEngineUnit0 = new StencilTestRenderingEngineUnit(shaderProgram, directionalLight , pointLight, spotLight);
        MyRenderingEngineUnit renderingEngineUnit1 = new MyRenderingEngineUnit(shaderProgram, directionalLight , pointLight, spotLight);
        TransparencyRenderingEngineUnit transparencyRenderingEngineUnit = new TransparencyRenderingEngineUnit(shaderProgram, directionalLight, pointLight, spotLight);
        MyRenderingEngineUnit reflectionRenderingEngineUnit = new MyRenderingEngineUnit(reflectionShaderProgram, directionalLight, pointLight, spotLight);
        MyRenderingEngineUnit refractionRenderingEngineUnit = new MyRenderingEngineUnit(refractionShaderProgram, directionalLight, pointLight, spotLight);
        RenderingEngineUnit renderingEngineUnit2 = new RenderingEngineUnit(lightSourceShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
                this.getShaderProgram().setUniformVec3F("color", 1.0f, 1.0f, 1.0f);
            }

            @Override
            public void render() {
                renderAllRenderables();
            }
        };
        RenderingEngineUnit renderingEngineUnit3 = new RenderingEngineUnit(oneColorShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
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

        new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", renderingEngineUnit1, new Vector3f(0.0f, 0.0f, 0.0f));
        new Model("./src/main/resources/models/HelloWorld/cube.obj", renderingEngineUnit0, new Vector3f(8.0f, 0.0f, 25.0f));
        new Model("./src/main/resources/models/HelloWorld/biggerCube.obj", renderingEngineUnit3, new Vector3f(8.0f, 0.0f, 25.0f));
        Renderable reflectionCube = new Model("./src/main/resources/models/HelloWorld/cube.obj", reflectionRenderingEngineUnit, new Vector3f(-8.0f, 0.0f, 20.0f));
        Renderable refractionCube = new Model("./src/main/resources/models/HelloWorld/refractionText.obj", refractionRenderingEngineUnit, new Vector3f(-8.0f, 2.0f, 20.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, -1.0f, 12.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, 1.0f, 15.0f));
        new RenderablePointLight(pointLight, "./src/main/resources/models/HelloWorld/cube.obj", logicalEngine, renderingEngineUnit2);

        addNewEngineUnit(renderingEngineUnit0);
        addNewEngineUnit(renderingEngineUnit1);
        addNewEngineUnit(renderingEngineUnit2);
        addNewEngineUnit(renderingEngineUnit3);
        addNewEngineUnit(reflectionRenderingEngineUnit);
        addNewEngineUnit(refractionRenderingEngineUnit);
        notToBeRendered = new Renderable[] {reflectionCube, refractionCube};


        frameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);

        texColorBuffer = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D_MULTISAMPLE, texColorBuffer);
        GL33.glTexImage2DMultisample(GL33.GL_TEXTURE_2D_MULTISAMPLE, 4, GL33.GL_RGBA, 1280,  720, true);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D_MULTISAMPLE, texColorBuffer, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D_MULTISAMPLE, 0);

        int RBO = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO);
        GL33.glRenderbufferStorageMultisample(GL33.GL_RENDERBUFFER, 4, GL33.GL_DEPTH24_STENCIL8, 1280, 720);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO);

        if(GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("The framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        frameBuffer2 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);

        texColorBuffer2 = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer2);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, 1280, 720, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, texColorBuffer2, 0);

        int RBO2 = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO2);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, 1280, 720);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO2);

        if(GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("The framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        frameBuffer3 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer3);

        texColorBuffer3 = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL33.GL_RGBA, 1280, 1280, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X, texColorBuffer3, 0);

        int RBO3 = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO3);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, 1280, 1280);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO3);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("The framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        frameBuffer4 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer4);

        texColorBuffer4 = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer4);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL33.GL_RGBA, 1280, 1280, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        }
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, texColorBuffer4, 0);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("The framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        float[] quadVertices = {
                -1.0f,  1.0f,  0.0f, 1.0f,
                -1.0f, -1.0f,  0.0f, 0.0f,
                1.0f, -1.0f,  1.0f, 0.0f,

                -1.0f,  1.0f,  0.0f, 1.0f,
                1.0f, -1.0f,  1.0f, 0.0f,
                1.0f,  1.0f,  1.0f, 1.0f
        };

        sP = new ShaderProgram("SimpleVertexShader", "SimpleFragmentShader");
        VAO = GL33.glGenVertexArrays();
        int VBO = GL33.glGenBuffers();

        GL33.glBindVertexArray(VAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, quadVertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 0);
        GL33.glVertexAttribPointer(1, 2, GL33.GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);


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
            public void updateRenderState(Camera camera, ShaderProgram shaderProgram) { }

            @Override
            public void render() {
                this.renderAllRenderables();
            }
        };
        skyboxRenderingEngineUnit.addNewRenderable(skybox);
        addNewEngineUnit(skyboxRenderingEngineUnit);
        addNewEngineUnit(transparencyRenderingEngineUnit);
    }

    @Override
    public void render() {
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);

        GL33.glViewport(0, 0, 1280, 1280);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer3);

        Matrix4f[] viewMatrices = new Matrix4f[6];

        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X, texColorBuffer3, 0);
        environmentMappingCamera.cameraPos = notToBeRendered[0].getPosition();
        environmentMappingCamera.cameraFront = new Vector3f(1.0f, 0.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, -1.0f, 0.0f);
        viewMatrices[0] = new Matrix4f().identity().lookAt(environmentMappingCamera.cameraPos, environmentMappingCamera.getLookAtPosition(), environmentMappingCamera.cameraUp);
        updateUniformBufferBlocks(environmentMappingCamera);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderables(notToBeRendered);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(-1.0f, 0.0f, 0.0f);
        viewMatrices[1] = new Matrix4f().identity().lookAt(environmentMappingCamera.cameraPos, environmentMappingCamera.getLookAtPosition(), environmentMappingCamera.cameraUp);
        updateUniformBufferBlocks(environmentMappingCamera);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderables(notToBeRendered);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 1.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, 0.0f, 1.0f);
        viewMatrices[2] = new Matrix4f().identity().lookAt(environmentMappingCamera.cameraPos, environmentMappingCamera.getLookAtPosition(), environmentMappingCamera.cameraUp);
        updateUniformBufferBlocks(environmentMappingCamera);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderables(notToBeRendered);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, -1.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, 0.0f, -1.0f);
        viewMatrices[3] = new Matrix4f().identity().lookAt(environmentMappingCamera.cameraPos, environmentMappingCamera.getLookAtPosition(), environmentMappingCamera.cameraUp);
        updateUniformBufferBlocks(environmentMappingCamera);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderables(notToBeRendered);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 0.0f, 1.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, -1.0f, 0.0f);
        viewMatrices[4] = new Matrix4f().identity().lookAt(environmentMappingCamera.cameraPos, environmentMappingCamera.getLookAtPosition(), environmentMappingCamera.cameraUp);
        updateUniformBufferBlocks(environmentMappingCamera);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderables(notToBeRendered);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        viewMatrices[5] = new Matrix4f().identity().lookAt(environmentMappingCamera.cameraPos, environmentMappingCamera.getLookAtPosition(), environmentMappingCamera.cameraUp);
        updateUniformBufferBlocks(environmentMappingCamera);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderables(notToBeRendered);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer4);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        environmentMappingShaderProgram.setUniformMat4F("environmentMappingProjectionMatrix", Transforms.createProjectionMatrix(environmentMappingCamera.fov, true, environmentMappingCamera.aspect, 0.1f, 100.0f));
        for (int i = 0; i < 6; i++) {
            environmentMappingShaderProgram.setUniformMat4F("environmentMappingViewMatrices[" + i + "]", viewMatrices[i]);
        }
        updateUniformBufferBlocks(environmentMappingCamera);
        updateWithAnotherShaderProgram(environmentMappingCamera, environmentMappingShaderProgram);
        renderWithoutRenderablesWithAnotherShaderProgram(notToBeRendered, environmentMappingShaderProgram);
        System.out.println(GL33.glGetError());


        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glViewport(0, 0, 1280, 720);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer4);
        reflectionShaderProgram.setUniform1I("cubeMap", 0);

        updateUniformBufferBlocks(camera);
        updateAllEngineUnits(camera);
        renderAllEngineUnits();

        Texture.unbindAll();
        CubeMap.unbindAll();

        GL33.glBindFramebuffer(GL33.GL_READ_FRAMEBUFFER, frameBuffer);
        GL33.glBindFramebuffer(GL33.GL_DRAW_FRAMEBUFFER, frameBuffer2);
        GL33.glBlitFramebuffer(0, 0, 1280, 720, 0, 0, 1280, 720, GL33.GL_COLOR_BUFFER_BIT, GL33.GL_LINEAR);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glViewport(0, 0, 320, 180);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        reflectionShaderProgram.setUniform1I("cubeMap", 0);

        camera.cameraFront = Transforms.getProductOf2Vectors(camera.cameraFront, new Vector3f(-1.0f));
        updateUniformBufferBlocks(camera);
        updateAllEngineUnits(camera);
        renderAllEngineUnits();
        camera.cameraFront = Transforms.getProductOf2Vectors(camera.cameraFront, new Vector3f(-1.0f));

        GL33.glBindFramebuffer(GL33.GL_READ_FRAMEBUFFER, frameBuffer);
        GL33.glBindFramebuffer(GL33.GL_DRAW_FRAMEBUFFER, frameBuffer2);
        GL33.glBlitFramebuffer(0, 0, 320, 180, 480, 540, 800, 720, GL33.GL_COLOR_BUFFER_BIT, GL33.GL_LINEAR);

        Texture.unbindAll();
        CubeMap.unbindAll();
        GL33.glViewport(0, 0, 1280, 720);


        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer2);
        sP.setUniform1I("screenTexture", 0);

        sP.bind();
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);
        GL33.glBindVertexArray(0);
        sP.unbind();

        Texture.unbindAll();
        CubeMap.unbindAll();

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        GLFW.glfwSwapBuffers(window.getId());
    }

    public void renderWithoutRenderable(Renderable renderable) {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            if (!renderingEngineUnit.containsRenderable(renderable)) {
                renderingEngineUnit.render();
            } else {
                renderingEngineUnit.getRenderables().remove(renderable);
                renderingEngineUnit.render();
                renderingEngineUnit.addNewRenderable(renderable);
            }
        }

        Texture.unbindAll();
        CubeMap.unbindAll();
        // TODO: Maybe uncomment the code
        // GLFW.glfwSwapBuffers(window.getWindow());
    }

    public void renderWithoutRenderables(Renderable[] renderables) {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);
        for (Renderable renderable : renderables) {
            for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
                if (!renderingEngineUnit.containsRenderable(renderable)) {
                    renderingEngineUnit.render();
                } else {
                    renderingEngineUnit.getRenderables().remove(renderable);
                    renderingEngineUnit.render();
                    renderingEngineUnit.addNewRenderable(renderable);
                }
            }
        }
        Texture.unbindAll();
        CubeMap.unbindAll();
        // TODO: Maybe uncomment the code
        // GLFW.glfwSwapBuffers(window.getWindow());
    }

    public void renderWithoutRenderablesWithAnotherShaderProgram(Renderable[] renderables, ShaderProgram shaderProgram) {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);
        for (Renderable renderable : renderables) {
            for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
                if (!renderingEngineUnit.containsRenderable(renderable)) {
                    renderingEngineUnit.renderWithAnotherShaderProgram(shaderProgram);
                } else {
                    renderingEngineUnit.getRenderables().remove(renderable);
                    renderingEngineUnit.renderWithAnotherShaderProgram(shaderProgram);
                    renderingEngineUnit.addNewRenderable(renderable);
                }
            }
        }
        Texture.unbindAll();
        CubeMap.unbindAll();
        // TODO: Maybe uncomment the code
        // GLFW.glfwSwapBuffers(window.getWindow());
    }

    @Override
    public void destroy() {
        this.destroyAllEngineUnits();

        GL33.glDeleteFramebuffers(new int[] {frameBuffer, frameBuffer2, frameBuffer3});

        GLFW.glfwDestroyWindow(window.getId());
    }

    private void updateUniformBufferBlocks(Camera cam) {
        // Uniform buffer blocks are set
        int uniformBufferBlock = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, uniformBufferBlock);
        GL33.glBufferData(GL33.GL_UNIFORM_BUFFER, 128, GL33.GL_STATIC_DRAW);

        Matrix4f view = new Matrix4f().identity().lookAt(cam.cameraPos, cam.getLookAtPosition(), cam.cameraUp);
        Matrix4f projection = Transforms.createProjectionMatrix(cam.fov, true, cam.aspect, 0.1f, 100.0f);
        FloatBuffer buffer = MemoryUtil.memAllocFloat(4);
        Vector4f column = new Vector4f();
        for (int i = 0; i < 4; i++) {
            view.getColumn(i, column);
            column.get(buffer);
            GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, i * 16, buffer);
        }
        for (int i = 0; i < 4; i++) {
            projection.getColumn(i, column);
            column.get(buffer);
            GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, i * 16 + 64, buffer);
        }

        GL33.glBindBufferBase(GL33.GL_UNIFORM_BUFFER, 0, uniformBufferBlock);
        GL33.glUniformBlockBinding(shaderProgram.getProgramId(), GL33.glGetUniformBlockIndex(shaderProgram.getProgramId(), "Matrices"), 0);
        GL33.glUniformBlockBinding(environmentMappingShaderProgram.getProgramId(), GL33.glGetUniformBlockIndex(shaderProgram.getProgramId(), "Matrices"), 0);
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);
    }

    @Override
    public void update() {
        updateUniformBufferBlocks(camera);

        updateAllEngineUnits(camera);

        window.update();
    }

    public void handleInputs(float deltaTime) {
        float cameraSpeed = 10.0f * deltaTime;

        if (window.getInput().isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
            GLFW.glfwSetWindowShouldClose(window.getId(), true);
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

    public Camera getCamera() {
        return camera;
    }

    public Window getWindow() {
        return window;
    }

    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

}
