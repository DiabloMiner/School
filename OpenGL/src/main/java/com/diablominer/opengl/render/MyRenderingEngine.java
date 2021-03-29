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
import com.diablominer.opengl.render.textures.TwoDimensionalTexture;
import com.diablominer.opengl.utils.Transforms;
import org.joml.*;
import org.joml.Math;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashSet;
import java.util.Set;

public class MyRenderingEngine extends RenderingEngine {

    private Camera camera;
    private Window window;
    private int frameBuffer, frameBuffer2, frameBuffer3, shadowFrameBuffer, shadowFrameBuffer2, shadowFrameBuffer3;
    private int[] pingpongFBOs = new int[2];
    private int texColorBuffer, texColorBuffer2, brightColorBuffer;
    private int[] pingpongColorBuffers = new int[2];
    private int VAO;
    private Set<ShaderProgram> matricesUniformBufferBlockShaderPrograms, environmentMappingUniformBufferBlockShaderPrograms, shadowMappingShaderPrograms;
    private ShaderProgram sP, reflectionShaderProgram, refractionShaderProgram, shadowShaderProgram, shadowShaderProgram2, gaussianBlurShaderProgram;
    private Set<Renderable> notToBeRendered, shadowNotToBeRendered;
    private Camera environmentMappingCamera;
    private DirectionalLight directionalLight;
    private PointLight pointLight;
    private SpotLight spotLight;
    private TwoDimensionalTexture shadowTwoDimensionalTexture, shadowTwoDimensionalTexture2;
    private CubeMap environmentCubeMap, shadowCubeMap;

    public MyRenderingEngine(LogicalEngine logicalEngine, Window window, Camera camera) throws Exception {
        this.window = window;
        this.camera = camera;
        environmentMappingCamera = new Camera(90.0f, new Vector3f(-15.0f, 0.0f, 20.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f), 1.0f);

        matricesUniformBufferBlockShaderPrograms = new HashSet<>();
        environmentMappingUniformBufferBlockShaderPrograms = new HashSet<>();
        shadowMappingShaderPrograms = new HashSet<>();

        ShaderProgram shaderProgram = new ShaderProgram("./normalShaders/VertexShader", "FragmentShader");
        ShaderProgram lightSourceShaderProgram = new ShaderProgram("./normalShaders/VertexShader", "LightSourceFragmentShader");
        ShaderProgram oneColorShaderProgram = new ShaderProgram("./normalShaders/VertexShader", "OneColorShader");
        ShaderProgram skyboxShaderProgram = new ShaderProgram("./normalShaders/SkyboxVertexShader", "SkyboxFragmentShader");

        ShaderProgram alternativeShaderProgram = new ShaderProgram("./alternativeShaders/VertexShader", "./alternativeShaders/GeometryShader", "FragmentShader");
        ShaderProgram alternativeLightSourceShaderProgram = new ShaderProgram("./alternativeShaders/VertexShader", "./alternativeShaders/GeometryShader", "LightSourceFragmentShader");
        ShaderProgram alternativeOneColorShaderProgram = new ShaderProgram("./alternativeShaders/VertexShader", "./alternativeShaders/GeometryShader", "OneColorShader");
        ShaderProgram alternativeSkyboxShaderProgram = new ShaderProgram("./alternativeShaders/SkyboxVertexShader", "./alternativeShaders/SkyboxGeometryShader", "SkyboxFragmentShader");

        reflectionShaderProgram = new ShaderProgram("./normalShaders/VertexShader", "ReflectionFragmentShader");
        refractionShaderProgram = new ShaderProgram("./normalShaders/VertexShader", "RefractionFragmentShader");
        sP = new ShaderProgram("SimpleVertexShader", "SimpleFragmentShader");
        shadowShaderProgram = new ShaderProgram("SimpleDepthVertexShader", "SimpleDepthFragmentShader");
        shadowShaderProgram2 = new ShaderProgram("ShadowVertexShader", "ShadowGeometryShader", "ShadowFragmentShader");
        gaussianBlurShaderProgram = new ShaderProgram("SimpleVertexShader", "gaussianBlurFragmentShader");

            matricesUniformBufferBlockShaderPrograms.add(shaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(lightSourceShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(oneColorShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(skyboxShaderProgram);

            matricesUniformBufferBlockShaderPrograms.add(alternativeShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(alternativeLightSourceShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(alternativeOneColorShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(alternativeSkyboxShaderProgram);

            matricesUniformBufferBlockShaderPrograms.add(reflectionShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(refractionShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(shadowShaderProgram);
            matricesUniformBufferBlockShaderPrograms.add(gaussianBlurShaderProgram);

            environmentMappingUniformBufferBlockShaderPrograms.add(alternativeShaderProgram);
            environmentMappingUniformBufferBlockShaderPrograms.add(alternativeLightSourceShaderProgram);
            environmentMappingUniformBufferBlockShaderPrograms.add(alternativeOneColorShaderProgram);
            environmentMappingUniformBufferBlockShaderPrograms.add(alternativeSkyboxShaderProgram);

            shadowMappingShaderPrograms.add(shaderProgram);
            shadowMappingShaderPrograms.add(alternativeShaderProgram);
            shadowMappingShaderPrograms.add(reflectionShaderProgram);
            shadowMappingShaderPrograms.add(refractionShaderProgram);

        directionalLight = new DirectionalLight(new Vector3f(1.0f, 0.0f, -0.15f), new Vector3f(0.6f, 0.6f, 0.6f));
        pointLight = new PointLight(new Vector3f(-8.0f, 2.0f, -2.0f), new Vector3f(200.0f, 200.0f, 200.0f), 35.0f);
        spotLight = new SpotLight(new Vector3f(0.0f), new Vector3f(5.0f, 5.0f, 5.0f));

        StencilTestRenderingEngineUnit stencilTestRenderingEngineUnit = new StencilTestRenderingEngineUnit(shaderProgram, alternativeShaderProgram, directionalLight , pointLight, spotLight);
        MyRenderingEngineUnit normalRenderingEngineUnit = new MyRenderingEngineUnit(shaderProgram, alternativeShaderProgram, directionalLight , pointLight, spotLight);
        TransparencyRenderingEngineUnit transparencyRenderingEngineUnit = new TransparencyRenderingEngineUnit(shaderProgram, alternativeShaderProgram, directionalLight, pointLight, spotLight);
        MyRenderingEngineUnit reflectionRenderingEngineUnit = new MyRenderingEngineUnit(reflectionShaderProgram, directionalLight, pointLight, spotLight);
        MyRenderingEngineUnit refractionRenderingEngineUnit = new MyRenderingEngineUnit(refractionShaderProgram, directionalLight, pointLight, spotLight);
        RenderingEngineUnit lightSourceRenderingEngineUnit = new RenderingEngineUnit(lightSourceShaderProgram, alternativeLightSourceShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
                shaderProgram.setUniformVec3F("color", 2.0f, 2.0f, 2.0f);
            }

            @Override
            public void render() {
                renderAllRenderables();
            }

            @Override
            public void renderAlternative() {
                renderAllRenderablesAlternative();
            }
        };
        RenderingEngineUnit stencilObjectRenderingEngineUnit = new RenderingEngineUnit(oneColorShaderProgram, alternativeOneColorShaderProgram) {
            @Override
            public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
                shaderProgram.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, camera.aspect, 0.1f, 100.0f));
                Matrix4f view = new Matrix4f().lookAt(camera.position, camera.getLookAtPosition(), camera.up);
                shaderProgram.setUniformMat4F("view", view);
            }

            @Override
            public void render() {
                GL33.glStencilFunc(GL33.GL_NOTEQUAL, 1, 0xFF);
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_KEEP);
                renderAllRenderables();
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);
                GL33.glStencilFunc(GL33.GL_ALWAYS, 0, 0x00);
            }

            @Override
            public void renderAlternative() {
                GL33.glStencilFunc(GL33.GL_NOTEQUAL, 1, 0xFF);
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_KEEP);
                renderAllRenderablesAlternative();
                GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);
                GL33.glStencilFunc(GL33.GL_ALWAYS, 0, 0x00);
            }
        };

        new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", normalRenderingEngineUnit, new Vector3f(0.0f, 0.0f, 1.0f));
        new Model("./src/main/resources/models/HelloWorld/bigPlane.obj", normalRenderingEngineUnit, new Vector3f(0.0f, 0.0f, 20.0f));
        new Model("./src/main/resources/models/HelloWorld/cube.obj", stencilTestRenderingEngineUnit, new Vector3f(8.0f, 0.0f, 16.0f));
        new Model("./src/main/resources/models/HelloWorld/biggerCube.obj", stencilObjectRenderingEngineUnit, new Vector3f(8.0f, 0.0f, 16.0f));
        Renderable reflectionCube = new Model("./src/main/resources/models/HelloWorld/cube.obj", reflectionRenderingEngineUnit, new Vector3f(-15.0f, 0.0f, 20.0f));
        Renderable refractionText = new Model("./src/main/resources/models/HelloWorld/refractionText.obj", refractionRenderingEngineUnit, new Vector3f(-15.0f, 2.0f, 20.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, -1.0f, 12.0f));
        new Model("./src/main/resources/models/transparentPlane/transparentWindowPlane.obj", transparencyRenderingEngineUnit, new Vector3f(0.0f, 1.0f, 15.0f));
        new RenderablePointLight(pointLight, "./src/main/resources/models/HelloWorld/cube.obj", logicalEngine, lightSourceRenderingEngineUnit);

        addNewEngineUnit(stencilTestRenderingEngineUnit);
        addNewEngineUnit(normalRenderingEngineUnit);
        addNewEngineUnit(lightSourceRenderingEngineUnit);
        addNewEngineUnit(stencilObjectRenderingEngineUnit);
        addNewEngineUnit(reflectionRenderingEngineUnit);
        addNewEngineUnit(refractionRenderingEngineUnit);

        notToBeRendered = new HashSet<>();
        notToBeRendered.add(reflectionCube);
        notToBeRendered.add(refractionText);


        frameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);

        texColorBuffer = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D_MULTISAMPLE, texColorBuffer);
        GL33.glTexImage2DMultisample(GL33.GL_TEXTURE_2D_MULTISAMPLE, 4, GL33.GL_RGBA16F, 1280,  720, true);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D_MULTISAMPLE, texColorBuffer, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D_MULTISAMPLE, 0);

        int brightColorBuffer2 = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D_MULTISAMPLE, brightColorBuffer2);
        GL33.glTexImage2DMultisample(GL33.GL_TEXTURE_2D_MULTISAMPLE, 4, GL33.GL_RGBA16F, 1280,  720, true);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT1, GL33.GL_TEXTURE_2D_MULTISAMPLE, brightColorBuffer2, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D_MULTISAMPLE, 0);

        GL33.glDrawBuffers(new int[] {GL33.GL_COLOR_ATTACHMENT0, GL33.GL_COLOR_ATTACHMENT1});
        GL33.glReadBuffer(GL33.GL_COLOR_ATTACHMENT0);

        int RBO = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO);
        GL33.glRenderbufferStorageMultisample(GL33.GL_RENDERBUFFER, 4, GL33.GL_DEPTH24_STENCIL8, 1280, 720);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO);

        if(GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer 1 has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        frameBuffer2 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);

        texColorBuffer2 = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer2);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA16F, 1280, 720, 0, GL33.GL_RGBA, GL33.GL_FLOAT, (ByteBuffer) null);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, texColorBuffer2, 0);

        brightColorBuffer = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, brightColorBuffer);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA16F, 1280, 720, 0, GL33.GL_RGBA, GL33.GL_FLOAT, (ByteBuffer) null);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT1, GL33.GL_TEXTURE_2D, brightColorBuffer, 0);

        GL33.glDrawBuffer(GL33.GL_COLOR_ATTACHMENT0);

        int RBO2 = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO2);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, 1280, 720);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO2);

        if(GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer 2 has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        frameBuffer3 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer3);

        environmentCubeMap = new CubeMap(1024, 1024, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT);
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, environmentCubeMap.id, 0);

        CubeMap depthAndStencilTexture = new CubeMap(1024, 1024, GL33.GL_DEPTH24_STENCIL8, GL33.GL_DEPTH_STENCIL, GL33.GL_UNSIGNED_INT_24_8);
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, depthAndStencilTexture.id, 0);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer 3 has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        shadowFrameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, shadowFrameBuffer);

        shadowTwoDimensionalTexture = TwoDimensionalTexture.createShadowTexture(2048, 2048, GL33.GL_DEPTH24_STENCIL8, GL33.GL_DEPTH_STENCIL, GL33.GL_UNSIGNED_INT_24_8);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, shadowTwoDimensionalTexture.id);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_TEXTURE_2D, shadowTwoDimensionalTexture.id, 0);
        GL33.glDrawBuffer(GL33.GL_NONE);
        GL33.glReadBuffer(GL33.GL_NONE);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Shadow framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        shadowFrameBuffer2 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, shadowFrameBuffer2);

        shadowCubeMap = new CubeMap(2048, 2048, GL33.GL_DEPTH24_STENCIL8, GL33.GL_DEPTH_STENCIL, GL33.GL_UNSIGNED_INT_24_8);
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, shadowCubeMap.id, 0);
        GL33.glDrawBuffer(GL33.GL_NONE);
        GL33.glReadBuffer(GL33.GL_NONE);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Omnidirectional-Shadow framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        shadowFrameBuffer3 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, shadowFrameBuffer3);

        shadowTwoDimensionalTexture2 = TwoDimensionalTexture.createShadowTexture(2048, 2048, GL33.GL_DEPTH24_STENCIL8, GL33.GL_DEPTH_STENCIL, GL33.GL_UNSIGNED_INT_24_8);
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, shadowTwoDimensionalTexture2.id, 0);
        GL33.glDrawBuffer(GL33.GL_NONE);
        GL33.glReadBuffer(GL33.GL_NONE);

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Omnidirectional-Shadow framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);


        GL33.glGenFramebuffers(pingpongFBOs);
        GL33.glGenTextures(pingpongColorBuffers);
        for (int i = 0; i < 2; i++) {
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, pingpongFBOs[i]);

            GL33.glBindTexture(GL33.GL_TEXTURE_2D, pingpongColorBuffers[i]);
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA16F, 1280, 720, 0, GL33.GL_RGBA, GL33.GL_FLOAT, (ByteBuffer) null);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);

            GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, pingpongColorBuffers[i], 0);
        }


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
                shaderProgram.bind();
                GL33.glBindVertexArray(VAO2);
                GL33.glEnableVertexAttribArray(0);

                GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);

                GL33.glDisableVertexAttribArray(0);
                GL33.glBindVertexArray(0);
                shaderProgram.unbind();
            }

            @Override
            public void destroy() {
                GL33.glDeleteVertexArrays(VAO2);
                GL33.glDeleteBuffers(VBO2);
            }
        };

        RenderingEngineUnit skyboxRenderingEngineUnit = new RenderingEngineUnit(skyboxShaderProgram, alternativeSkyboxShaderProgram) {

            private CubeMap cubeMap = CubeMap.equirectangularMapToCubeMap("./src/main/resources/textures/skybox/Newport_Loft_8k.jpg",1024, true);

            @Override
            public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
                cubeMap.bind();
                shaderProgram.setUniform1I("skybox", cubeMap.index);
            }

            @Override
            public void render() {
                GL33.glDepthFunc(GL33.GL_LEQUAL);
                this.renderAllRenderables();
                GL33.glDepthFunc(GL33.GL_LESS);
            }

            @Override
            public void renderAlternative() {
                GL33.glDepthFunc(GL33.GL_LEQUAL);
                this.renderAllRenderablesAlternative();
                GL33.glDepthFunc(GL33.GL_LESS);
            }
        };
        shadowNotToBeRendered = new HashSet<>();
        shadowNotToBeRendered.add(skybox);
        shadowNotToBeRendered.add(refractionText);

        skyboxRenderingEngineUnit.addNewRenderable(skybox);
        addNewEngineUnit(skyboxRenderingEngineUnit);
        addNewEngineUnit(transparencyRenderingEngineUnit);
    }

    @Override
    public void render() {
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);

        // Directional shadow-mapping
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, shadowFrameBuffer);
        GL33.glViewport(0, 0, 2048, 2048);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glCullFace(GL33.GL_FRONT);
        directionalLight.setLightSpaceMatrix(updateDirectionalShadowMatrices(shadowShaderProgram, Transforms.getProductOf2Vectors(directionalLight.getDirection(), new Vector3f(-20.0f)), new Vector3f(0.0f, 0.0f, 0.0f)));
        updateAllEngineUnitsWithAnotherShaderProgram(camera, shadowShaderProgram);
        renderAllEngineUnitsWithoutRenderablesWithAlternativeShaderProgram(shadowNotToBeRendered, shadowShaderProgram);
        GL33.glCullFace(GL33.GL_BACK);

        Texture.unbindAllTextures();

        // Omnidirectional shadow-mapping for the point-light
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, shadowFrameBuffer2);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        updateOmniDirectionalShadowMatrices(shadowShaderProgram2, pointLight.getPosition(), pointLight.getFarPlane());
        updateAllEngineUnitsWithAnotherShaderProgram(camera, shadowShaderProgram2);
        renderAllEngineUnitsWithoutRenderablesWithAlternativeShaderProgram(shadowNotToBeRendered, shadowShaderProgram2);

        Texture.unbindAllTextures();

        // Directional shadow-mapping for the spot-light
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, shadowFrameBuffer3);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glCullFace(GL33.GL_FRONT);
        spotLight.setLightSpaceMatrix(updateDirectionalShadowMatricesForSpotLight(shadowShaderProgram, camera.position, camera.getLookAtPosition()));
        updateAllEngineUnitsWithAnotherShaderProgram(camera, shadowShaderProgram);
        renderAllEngineUnitsWithoutRenderablesWithAlternativeShaderProgram(shadowNotToBeRendered, shadowShaderProgram);
        GL33.glCullFace(GL33.GL_BACK);

        Texture.unbindAllTextures();

        // Environment-mapping
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer3);
        GL33.glViewport(0, 0, 1024, 1024);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        updateShadowMaps();
        updateUniformBufferBlocks(environmentMappingCamera);
        updateSpecialUniformBufferBlocks(environmentMappingCamera);
        updateAllEngineUnitsAlternative(environmentMappingCamera);
        renderAllEngineUnitsWithoutRenderablesAlternative(notToBeRendered);

        Texture.unbindAllTextures();


        // Normal rendering
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glViewport(0, 0, 1280, 720);

        updateUniformBufferBlocks(camera);
        updateAllEngineUnits(camera);
        updateUniforms();
        renderAllEngineUnits();

        blitFramebuffers(frameBuffer, frameBuffer2, 0, 0, 1280, 720, 0, 0, 1280, 720);

        Texture.unbindAllTextures();

        // Normal rendering for the back "mirror"
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glViewport(0, 0, 320, 180);

        camera.front = Transforms.getProductOf2Vectors(camera.front, new Vector3f(-1.0f));
        updateUniformBufferBlocks(camera);
        updateAllEngineUnits(camera);
        updateUniforms();
        renderAllEngineUnits();
        camera.front = Transforms.getProductOf2Vectors(camera.front, new Vector3f(-1.0f));

        blitFramebuffers(frameBuffer, frameBuffer2, 0, 0, 320, 180, 480, 540, 800, 720);

        GL33.glViewport(0, 0, 1280, 720);
        Texture.unbindAllTextures();


        // Blurring the final picture for the bloom effect
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
        pingpongBlur(gaussianBlurShaderProgram, pingpongFBOs, pingpongColorBuffers, brightColorBuffer);
        Texture.unbindAllTextures();


        // Rendering the result onto a quad
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glEnable(GL33.GL_FRAMEBUFFER_SRGB);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer2);
        sP.setUniform1I("screenTexture", 0);
        GL33.glActiveTexture(GL33.GL_TEXTURE1);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, pingpongColorBuffers[0]);
        sP.setUniform1I("bloomBlur", 1);

        renderQuad(sP);

        GL33.glDisable(GL33.GL_FRAMEBUFFER_SRGB);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        GLFW.glfwSwapBuffers(window.getId());
    }

    @Override
    public void update() {
        updateUniformBufferBlocks(camera);
        updateUniforms();

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

    private void updateUniformBufferBlocks(Camera cam) {
        int uniformBufferBlock = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, uniformBufferBlock);
        GL33.glBufferData(GL33.GL_UNIFORM_BUFFER, 128, GL33.GL_STATIC_DRAW);

        Matrix4f view = new Matrix4f().identity().lookAt(cam.position, cam.getLookAtPosition(), cam.up);
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
        for (ShaderProgram shaderProgram : matricesUniformBufferBlockShaderPrograms) {
            GL33.glUniformBlockBinding(shaderProgram.getProgramId(), GL33.glGetUniformBlockIndex(shaderProgram.getProgramId(), "Matrices"), 0);
        }
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);

        MemoryUtil.memFree(buffer);
    }

    public void updateSpecialUniformBufferBlocks(Camera cam) {
        int uniformBufferBlock = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, uniformBufferBlock);
        GL33.glBufferData(GL33.GL_UNIFORM_BUFFER, 448, GL33.GL_STATIC_DRAW);

        Matrix4f[] viewMatrices = new Matrix4f[6];
        Matrix4f projection = Transforms.createProjectionMatrix(cam.fov, true, cam.aspect, 0.1f, 100.0f);

        cam.front = new Vector3f(1.0f, 0.0f, 0.0f);
        cam.up = new Vector3f(0.0f, -1.0f, 0.0f);
        viewMatrices[0] = Transforms.createViewMatrix(cam);

        cam.front = new Vector3f(-1.0f, 0.0f, 0.0f);
        viewMatrices[1] = Transforms.createViewMatrix(cam);

        cam.front = new Vector3f(0.0f, 1.0f, 0.0f);
        cam.up = new Vector3f(0.0f, 0.0f, 1.0f);
        viewMatrices[2] = Transforms.createViewMatrix(cam);

        cam.front = new Vector3f(0.0f, -1.0f, 0.0f);
        cam.up = new Vector3f(0.0f, 0.0f, -1.0f);
        viewMatrices[3] = Transforms.createViewMatrix(cam);

        cam.front = new Vector3f(0.0f, 0.0f, 1.0f);
        cam.up = new Vector3f(0.0f, -1.0f, 0.0f);
        viewMatrices[4] = Transforms.createViewMatrix(cam);

        cam.front = new Vector3f(0.0f, 0.0f, -1.0f);
        viewMatrices[5] = Transforms.createViewMatrix(cam);

        FloatBuffer buffer = MemoryUtil.memAllocFloat(4);
        Vector4f column = new Vector4f();

        for (int i = 0 ; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                viewMatrices[i].getColumn(j, column);
                column.get(buffer);
                GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, i * 64 + j * 16, buffer);
            }
        }
        for (int j = 0; j < 4; j++) {
            projection.getColumn(j, column);
            column.get(buffer);
            GL33.glBufferSubData(GL33.GL_UNIFORM_BUFFER, 384 + j * 16, buffer);
        }

        GL33.glBindBufferBase(GL33.GL_UNIFORM_BUFFER, 1, uniformBufferBlock);
        for (ShaderProgram shaderProgram : environmentMappingUniformBufferBlockShaderPrograms) {
            GL33.glUniformBlockBinding(shaderProgram.getProgramId(), GL33.glGetUniformBlockIndex(shaderProgram.getProgramId(), "environmentMappingMatrices"), 1);
        }
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);
    }

    private void updateUniforms() {
        updateShadowMaps();

        environmentCubeMap.bind();
        reflectionShaderProgram.setUniform1I("cubeMap", environmentCubeMap.index);
        refractionShaderProgram.setUniform1I("cubeMap", environmentCubeMap.index);
    }

    private void updateShadowMaps() {
        // Here textures and uniforms needed for shadow mapping are set
        shadowTwoDimensionalTexture.bind();
        shadowTwoDimensionalTexture2.bind();
        shadowCubeMap.bind();
        for (ShaderProgram shaderProgram : shadowMappingShaderPrograms) {
            shaderProgram.setUniform1I("dirLight.shadowMap", shadowTwoDimensionalTexture.index);
            shaderProgram.setUniform1I("pointLight.shadowMap", shadowCubeMap.index);
            shaderProgram.setUniform1F("pointLight.farPlane", pointLight.getFarPlane());
            shaderProgram.setUniform1I("spotLight.shadowMap", shadowTwoDimensionalTexture2.index);
            shaderProgram.setUniformMat4F("dirLightLightSpaceMatrix", directionalLight.getLightSpaceMatrix());
            shaderProgram.setUniformMat4F("spotLightLightSpaceMatrix", spotLight.getLightSpaceMatrix());
        }
    }

    public Matrix4f updateDirectionalShadowMatrices(ShaderProgram shaderProgram, Vector3f position, Vector3f lookAtPosition) {
        Matrix4f lightProjection = new Matrix4f().identity().ortho(-30.0f, 30.0f, -20.0f, 20.0f, 1.0f, 35.0f);
        Matrix4f lightView = new Matrix4f().identity().lookAt(position, lookAtPosition, new Vector3f(0.0f, 1.0f, 0.0f));
        Matrix4f lightSpaceMatrix = new Matrix4f().identity();
        lightProjection.mul(lightView, lightSpaceMatrix);

        shaderProgram.setUniformMat4F("lightSpaceMatrix", lightSpaceMatrix);
        return lightSpaceMatrix;
    }

    public Matrix4f updateDirectionalShadowMatricesForSpotLight(ShaderProgram shaderProgram, Vector3f position, Vector3f lookAtPosition) {
        Matrix4f lightProjection = new Matrix4f().identity().perspective(Math.toRadians(90.0f), 1.0f, 1.0f, 35.0f);
        Matrix4f lightView = new Matrix4f().identity().lookAt(position, lookAtPosition, new Vector3f(0.0f, 1.0f, 0.0f));
        Matrix4f lightSpaceMatrix = new Matrix4f().identity();
        lightProjection.mul(lightView, lightSpaceMatrix);

        shaderProgram.setUniformMat4F("lightSpaceMatrix", lightSpaceMatrix);
        return lightSpaceMatrix;
    }

    public void updateOmniDirectionalShadowMatrices(ShaderProgram shaderProgram, Vector3f position, float farPlane) {
        Matrix4f projection = new Matrix4f().identity().perspective(Math.toRadians(90.0f), 1.0f, 1.0f, farPlane);
        Matrix4f[] viewMatrices = {
                new Matrix4f().identity().lookAt(position, Transforms.getSumOf2Vectors(position, new Vector3f(1.0f, 0.0f, 0.0f)), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, Transforms.getSumOf2Vectors(position, new Vector3f(-1.0f, 0.0f, 0.0f)), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, Transforms.getSumOf2Vectors(position, new Vector3f(0.0f, 1.0f, 0.0f)), new Vector3f(0.0f, 0.0f, 1.0f)),
                new Matrix4f().identity().lookAt(position, Transforms.getSumOf2Vectors(position, new Vector3f(0.0f, -1.0f, 0.0f)), new Vector3f(0.0f, 0.0f, -1.0f)),
                new Matrix4f().identity().lookAt(position, Transforms.getSumOf2Vectors(position, new Vector3f(0.0f, 0.0f, 1.0f)), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, Transforms.getSumOf2Vectors(position, new Vector3f(0.0f, 0.0f, -1.0f)), new Vector3f(0.0f, -1.0f, 0.0f)),
        };
        Matrix4f[] transformMatrices = {
                Transforms.getProductOf2Matrices(projection, viewMatrices[0]),
                Transforms.getProductOf2Matrices(projection, viewMatrices[1]),
                Transforms.getProductOf2Matrices(projection, viewMatrices[2]),
                Transforms.getProductOf2Matrices(projection, viewMatrices[3]),
                Transforms.getProductOf2Matrices(projection, viewMatrices[4]),
                Transforms.getProductOf2Matrices(projection, viewMatrices[5]),
        };
        shaderProgram.setUniform1F("farPlane", farPlane);
        for (int i = 0; i < 6; i++) {
            shaderProgram.setUniformMat4F("shadowMatrices[" + i + "]", transformMatrices[i]);
        }
    }

    private void blitFramebuffers(int frameBuffer1, int frameBuffer2, int srcX0, int srcY0, int srcX1, int srcY1, int destX0, int destY0, int destX1, int destY1) {
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer1);
        GL33.glReadBuffer(GL33.GL_COLOR_ATTACHMENT0);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);
        GL33.glDrawBuffer(GL33.GL_COLOR_ATTACHMENT0);
        GL33.glBindFramebuffer(GL33.GL_READ_FRAMEBUFFER, frameBuffer1);
        GL33.glBindFramebuffer(GL33.GL_DRAW_FRAMEBUFFER, frameBuffer2);
        GL33.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, GL33.GL_COLOR_BUFFER_BIT, GL33.GL_LINEAR);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer1);
        GL33.glReadBuffer(GL33.GL_COLOR_ATTACHMENT1);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);
        GL33.glDrawBuffer(GL33.GL_COLOR_ATTACHMENT1);
        GL33.glBindFramebuffer(GL33.GL_READ_FRAMEBUFFER, frameBuffer1);
        GL33.glBindFramebuffer(GL33.GL_DRAW_FRAMEBUFFER, frameBuffer2);
        GL33.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, GL33.GL_COLOR_BUFFER_BIT, GL33.GL_LINEAR);
    }

    private void pingpongBlur(ShaderProgram blurShaderProgram, int[] pingpongFBOs, int[] pingpongBuffers, int initialTexture) {
        boolean horizontal = true, firstIteration = true;
        blurShaderProgram.bind();
        for (int i = 0; i < 10; i++) {
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, pingpongFBOs[(horizontal ? 1 : 0)]);
            GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

            blurShaderProgram.setUniform1I("horizontal", (horizontal ? 1 : 0));
            GL33.glActiveTexture(GL33.GL_TEXTURE0);
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, firstIteration ? initialTexture : pingpongBuffers[(horizontal ? 0 : 1)]);
            blurShaderProgram.setUniform1I("image", 0);

            renderQuad(blurShaderProgram);

            horizontal = !horizontal;
            if (firstIteration) {
                firstIteration = false;
            }
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }

    private void renderQuad(ShaderProgram shaderProgram) {
        shaderProgram.bind();
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);

        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 6);

        GL33.glBindVertexArray(0);
        shaderProgram.unbind();

        Texture.unbindAllTextures();
    }

    private void renderAllEngineUnitsWithoutRenderablesAlternative(Set<Renderable> renderables) {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        for (RenderingEngineUnit renderingEngineUnit : this.renderingEngineUnits) {
            Set<Renderable> needToBeRemoved = renderingEngineUnit.containsRenderables(renderables);
            if (needToBeRemoved == null) {
                renderingEngineUnit.renderAlternative();
            } else {
                renderingEngineUnit.getRenderables().removeAll(needToBeRemoved);
                renderingEngineUnit.renderAlternative();
                renderingEngineUnit.getRenderables().addAll(needToBeRemoved);
            }
        }
    }

    private void renderAllEngineUnitsWithoutRenderablesWithAlternativeShaderProgram(Set<Renderable> renderables, ShaderProgram shaderProgram) {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        for (RenderingEngineUnit renderingEngineUnit : this.renderingEngineUnits) {
            Set<Renderable> needToBeRemoved = renderingEngineUnit.containsRenderables(renderables);
            if (needToBeRemoved == null) {
                renderingEngineUnit.renderWithAnotherShaderProgram(shaderProgram);
            } else {
                renderingEngineUnit.getRenderables().removeAll(needToBeRemoved);
                renderingEngineUnit.renderWithAnotherShaderProgram(shaderProgram);
                renderingEngineUnit.getRenderables().addAll(needToBeRemoved);
            }
        }
    }

    @Override
    public void destroy() {
        this.destroyAllEngineUnits();

        GL33.glDeleteFramebuffers(new int[] {frameBuffer, frameBuffer2, frameBuffer3});

        GLFW.glfwDestroyWindow(window.getId());
    }

    public Camera getCamera() {
        return camera;
    }

    public Window getWindow() {
        return window;
    }

}
