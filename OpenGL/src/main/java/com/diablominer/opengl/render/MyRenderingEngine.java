package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.renderables.Renderable;
import com.diablominer.opengl.render.textures.CubeMap;
import com.diablominer.opengl.render.textures.Texture;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class MyRenderingEngine extends RenderingEngine {

    private Camera camera;
    private int frameBuffer, frameBuffer2, frameBuffer3;
    private int texColorBuffer, texColorBuffer2, texColorBuffer3;
    private int VAO;
    private ShaderProgram sP, reflectionShaderProgram;
    private Renderable reflectionCube;
    private Camera environmentMappingCamera;

    public MyRenderingEngine(DirectionalLight directionalLight, PointLight pointLight, SpotLight spotLight) throws Exception {
        camera = new Camera(45.0f, new Vector3f(1.0f, 1.0f, 5.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, 1.0f, 0.0f), 1280.0f / 720.0f);
        environmentMappingCamera = new Camera(90.0f, new Vector3f(-15.0f, 0.0f, 20.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f), 1.0f);

        reflectionShaderProgram = new ShaderProgram("VertexShader", "ReflectionFragmentShader");
        MyRenderingEngineUnit reflectionRenderingEngineUnit = new MyRenderingEngineUnit(reflectionShaderProgram, directionalLight, pointLight, spotLight);
        reflectionCube = new Model("./src/main/resources/models/HelloWorld/cube.obj", reflectionRenderingEngineUnit, new Vector3f(-8.0f, 0.0f, 20.0f));


        frameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);

        texColorBuffer = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, 1280, 720, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, texColorBuffer, 0);

        int RBO = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, 1280, 720);
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_RENDERBUFFER, RBO);

        if(GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("The framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);

        frameBuffer2 = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);

        texColorBuffer2 = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texColorBuffer2);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, 320, 180, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, texColorBuffer2, 0);

        int RBO2 = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, RBO2);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, 320, 180);
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
        addNewEngineUnit(skyboxRenderingEngineUnit);
    }

    @Override
    public void render(Window window) {
        GL33.glViewport(0, 0, 1280, 1280);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer3);

        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X, texColorBuffer3, 0);
        environmentMappingCamera.cameraPos = reflectionCube.getPosition();
        environmentMappingCamera.cameraFront = new Vector3f(1.0f, 0.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, -1.0f, 0.0f);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(-1.0f, 0.0f, 0.0f);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 1.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, 0.0f, 1.0f);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, -1.0f, 0.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, 0.0f, -1.0f);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 0.0f, 1.0f);
        environmentMappingCamera.cameraUp = new Vector3f(0.0f, -1.0f, 0.0f);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderable(window, reflectionCube);

        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, texColorBuffer3, 0);
        environmentMappingCamera.cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        updateAllEngineUnits(environmentMappingCamera);
        renderWithoutRenderable(window, reflectionCube);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        reflectionShaderProgram.setUniform1I("skybox", 0);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);
        GL33.glViewport(0, 0, 1280, 720);

        updateAllEngineUnits(camera);
        this.renderAllEngineUnits();

        Texture.unbindAll();
        CubeMap.unbindAll();


        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, texColorBuffer3);
        reflectionShaderProgram.setUniform1I("skybox", 0);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer2);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);
        GL33.glViewport(0, 0, 320, 180);
        camera.cameraFront = Transforms.getProductOf2Vectors(camera.cameraFront, new Vector3f(-1.0f));

        updateAllEngineUnits(camera);
        this.renderAllEngineUnits();

        Texture.unbindAll();
        CubeMap.unbindAll();

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

    public void renderWithoutRenderable(Window window, Renderable renderable) {
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

    @Override
    public void destroy() {
        this.destroyAllEngineUnits();

        GL33.glDeleteFramebuffers(new int[] {frameBuffer, frameBuffer2, frameBuffer3});
    }
}
