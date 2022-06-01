package com.diablominer.opengl.examples.learning;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;

public class MainRenderingEngine extends RenderingEngine {

    private final Camera camera;
    private final VecMatUniformBufferBlock matricesUniforms;
    private final SingleFramebufferRenderer mainRenderer;
    private final BlurRenderer blurRenderer;
    private final Framebuffer intermediateFb;
    private final QuadRenderingEngineUnit quadRenderingEngineUnit;

    public MainRenderingEngine(Window window, Camera camera) throws Exception {
        this.camera = camera;

        ShaderProgram shaderProgram = new ShaderProgram("L6VS", "L6FS");
        ShaderProgram lsShaderProgram = new ShaderProgram("L6VS", "L6FS_LS", false);
        ShaderProgram simpleShaderProgram = new ShaderProgram("L6SVS", "L6SFS", false);

        DirectionalLight dirLight = new DirectionalLight(new Vector3f(-0.7f, 1.0f, 2.9f), new Vector3f(0.0f, 0.0f, 0.2f), 1024);
        RenderablePointLight pointLight = new RenderablePointLight(new Vector3f(0.0f, 5.0f, 0.0f), new Vector3f(0.0f, 50.0f, 38.0f));
        SpotLight spotLight = new CameraUpdatedSpotLight(new Vector3f(camera.position), new Vector3f(camera.direction), new Vector3f(0.8f, 0.0f, 0.0f), 1024);

        Model helloWorld = new Model("./src/main/resources/models/HelloWorld/HelloWorld.obj", new Matrix4f().identity().rotate(Math.toRadians(-55.0f), new Vector3f(1.0f, 0.0f, 0.0f)));
        Model cube = new Model("./src/main/resources/models/HelloWorld/cube3.obj", new Matrix4f().identity().translate(new Vector3f(3.4f, -2.0f, -5.8f)));

        RenderingEngineUnit standardRenderingEngineUnit = new StandardRenderingEngineUnit(shaderProgram, new Renderable[] {helloWorld, cube});
        mainRenderer = new SingleFramebufferRenderer(new FramebufferTexture2D[] {new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT1)},
                new FramebufferRenderbuffer[] {new FramebufferRenderbuffer(GL33.GL_DEPTH24_STENCIL8, window.width, window.height, 4, FramebufferAttachment.DEPTH_AND_STENCIL_ATTACHMENT)},
                new RenderingEngineUnit[] {standardRenderingEngineUnit, new LightRenderingEngineUnit(lsShaderProgram)});
        intermediateFb = new Framebuffer(new FramebufferTexture2D[] {new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferTexture2D(window.width, window.height, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, FramebufferAttachment.COLOR_ATTACHMENT1)},
                new FramebufferRenderbuffer[] {new FramebufferRenderbuffer(GL33.GL_DEPTH24_STENCIL8, window.width, window.height, FramebufferAttachment.DEPTH_AND_STENCIL_ATTACHMENT)});
        blurRenderer = new BlurRenderer(window.width, window.height, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, 10, intermediateFb.getAttached2DTextures().get(1));
        quadRenderingEngineUnit = new QuadRenderingEngineUnit(simpleShaderProgram, new ArrayList<>(Arrays.asList(intermediateFb.getAttached2DTextures().get(0), blurRenderer.getFinalFramebuffer().getAttached2DTextures().get(0))));
        renderers = new ArrayList<>(Arrays.asList(mainRenderer, blurRenderer));

        matricesUniforms = new VecMatUniformBufferBlock(1, 2, GL33.GL_DYNAMIC_DRAW, "Matrices");
        shaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms});
        lsShaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms});

        Light.createShadowRenderers();
    }

    @Override
    public void render() {
        Light.renderShadowMaps();

        mainRenderer.render();

        Light.unbindAllShadowTextures();
        Framebuffer.blitFrameBuffers(mainRenderer.getFramebuffer(), intermediateFb);

        blurRenderer.render();

        Framebuffer.getStandardFramebuffer().bind();
        GL33.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glEnable(GL33.GL_FRAMEBUFFER_SRGB);

        quadRenderingEngineUnit.render();

        GL33.glDisable(GL33.GL_FRAMEBUFFER_SRGB);
        Learning6.engineInstance.getWindow().swapBuffers();
    }

    @Override
    public void update() {
        Matrix4f projection = new Matrix4f().identity();
        projection.perspective(Math.toRadians(camera.fov), (float) Learning6.engineInstance.getWindow().width / (float) Learning6.engineInstance.getWindow().height, camera.near, camera.far);

        matricesUniforms.setElements(new ArrayList<>(Arrays.asList(new Vector4FElement(camera.position), new Matrix4FElement(camera.getViewMatrix()), new Matrix4FElement(projection))));
        matricesUniforms.setUniformBlockData();
        Light.setUniformDataForAllLights();
    }

    @Override
    public void destroy() {
        matricesUniforms.destroy();
        mainRenderer.destroy();
        blurRenderer.destroy();
        intermediateFb.destroy();
        quadRenderingEngineUnit.destroy();
    }

    public Camera getCamera() {
        return camera;
    }

}
