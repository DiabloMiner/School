package com.diablominer.opengl.examples.learning;

import org.joml.*;
import org.joml.Math;
import org.lwjgl.opengl.GL33;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

public class MainRenderingEngine extends RenderingEngine {

    private boolean resize;

    private final Camera camera;
    private final Window window;
    private final VecMatUniformBufferBlock matricesUniforms;
    private final SingleFramebufferRenderer mainRenderer;
    private final BlurRenderer blurRenderer;
    private final Framebuffer intermediateFb;
    private final QuadRenderingUnit quadRenderingEngineUnit;

    public MainRenderingEngine(Window window, Camera camera) throws Exception {
        super();
        this.camera = camera;
        this.window = window;
        this.resize = false;

        ShaderProgram shaderProgram = shaderProgramManager.addShaderProgram(new ShaderProgram("L6VS", "L6FS"), true, true);
        ShaderProgram lsShaderProgram = shaderProgramManager.addShaderProgram(new ShaderProgram("L6VS", "L6FS_LS"), false, false);
        ShaderProgram simpleShaderProgram = shaderProgramManager.addShaderProgram(new ShaderProgram("L6SVS", "L6SFS"), false, false);

        lightManager.addDirectionalLight(new DirectionalLight(new Vector3f(-0.7f, 1.0f, 2.9f), new Vector3f(0.0f, 0.0f, 1.0f), 1024));
        lightManager.addRenderablePointLight(new RenderablePointLight(new Vector3f(0.0f, 5.0f, 0.0f), new Vector3f(0.0f, 50.0f, 38.0f), 1024));
        lightManager.addSpotLight(new CameraUpdatedSpotLight(new Vector3f(camera.position), new Vector3f(camera.direction), new Vector3f(0.8f, 0.0f, 0.0f), 1024, camera));

        AssimpModel helloWorld = (AssimpModel) renderableManager.addRenderable(new AssimpModel("./src/main/resources/models/HelloWorld/HelloWorld.obj", new Matrix4f().identity().rotate(Math.toRadians(-55.0f), new Vector3f(1.0f, 0.0f, 0.0f))), true);
        AssimpModel cube = (AssimpModel) renderableManager.addRenderable(new AssimpModel("./src/main/resources/models/HelloWorld/cube3.obj", new Matrix4f().identity().translate(new Vector3f(3.4f, -1.5f, -4.4f))), true);
        TestPhysicsCube physicsCube1 = (TestPhysicsCube) renderableManager.addRenderable(new TestPhysicsCube("./src/main/resources/models/HelloWorld/cube2.obj", new Vector3d(10.0, 6.0, 0.0), new Vector3d(0.0, 0.0, 0.0), new Vector3d(0.0),  new Quaterniond().identity(), new Vector3d(0.0), new Vector3d(0.0), 10, 2), true);
        TestPhysicsCube physicsCube2 = (TestPhysicsCube) renderableManager.addRenderable(new TestPhysicsCube("./src/main/resources/models/HelloWorld/cube2.obj", new Vector3d(-10.0, 6.0, 0.0), new Vector3d(0.0, 0.0, 0.0), new Vector3d(0.0),  new Quaterniond().identity(), new Vector3d(0.0), new Vector3d(0.0), 10, 2), true);
        Learning6.engineInstance.getMainPhysicsEngine().physicsObjects.addAll(Arrays.asList(physicsCube1, physicsCube2));

        RenderingUnit standardRenderingUnit = new StandardRenderingUnit(shaderProgram, new Renderable[] {helloWorld, cube,  physicsCube1, physicsCube2});
        RenderingUnit lightRenderingUnit = new LightRenderingUnit(lsShaderProgram, lightManager.allRenderableLights);
        RenderingUnit skyboxRenderingUnit = new SkyboxRenderingUnit(skyboxManager.addSkybox(new Skybox("./src/main/resources/textures/skybox", ".jpg", false)));
        mainRenderer = new SingleFramebufferRenderer(framebufferManager.addFramebuffer(new Framebuffer(new FramebufferTexture2D[] {new FramebufferMSAATexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferMSAATexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, 4, FramebufferAttachment.COLOR_ATTACHMENT1)},
                new FramebufferRenderbuffer[] {new FramebufferMSAARenderbuffer(window.width, window.height, Renderbuffer.InternalFormat.DEPTH24_STENCIL8, 4, FramebufferAttachment.DEPTH_AND_STENCIL_ATTACHMENT)})),
                new RenderingUnit[] {standardRenderingUnit, lightRenderingUnit, skyboxRenderingUnit});
        intermediateFb = framebufferManager.addFramebuffer(new Framebuffer(new FramebufferTexture2D[] {new FramebufferTexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, Texture.Format.RGBA, Texture.Type.FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0), new FramebufferTexture2D(window.width, window.height, Texture.InternalFormat.RGBA16F, Texture.Format.RGBA, Texture.Type.FLOAT, FramebufferAttachment.COLOR_ATTACHMENT1)}));
        blurRenderer = new BlurRenderer(window.width, window.height, Texture.InternalFormat.RGBA16F, Texture.Format.RGBA, Texture.Type.FLOAT, 10, intermediateFb.getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT1).storedTexture);
        quadRenderingEngineUnit = new QuadRenderingUnit(simpleShaderProgram, new ArrayList<>(Arrays.asList(intermediateFb.getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT0).storedTexture, blurRenderer.getFinalFramebuffer().getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT0).storedTexture)));
        framebufferManager.addFramebuffers(blurRenderer.getFramebuffers());

        matricesUniforms = new VecMatUniformBufferBlock(1, 2, Buffer.Usage.DYNAMIC_DRAW, "Matrices");
        shaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms});
        lsShaderProgram.setUniformBlockBindings(new UniformBufferBlock[]{matricesUniforms});

        lightManager.createShadowRenderers(renderableManager.allRenderablesThrowingShadows.toArray(new Renderable[0]));

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glCullFace(GL33.GL_BACK);
    }

    public void update() {
        Matrix4f projection = new Matrix4f().identity();
        projection.perspective(Math.toRadians(camera.fov), (float) window.width / (float) window.height, camera.near, camera.far);

        matricesUniforms.setElements(new ArrayList<>(Arrays.asList(new Vector4FElement(camera.position), new Matrix4FElement(camera.getViewMatrix()), new Matrix4FElement(projection))));
        matricesUniforms.setUniformBlockData();
        lightManager.setLightUniforms(shaderProgramManager.allShaderProgramsUsingShadows);
        skyboxManager.setSkyboxUniforms(shaderProgramManager.allShaderProgramsUsingSkyboxes);

        mainRenderer.updateAllRenderingEngineUnits();
    }

    @Override
    public void render() {
        lightManager.renderShadowMaps();

        mainRenderer.render(RenderingIntoFlag.COLOR_DEPTH);

        Framebuffer.blitFrameBuffers(mainRenderer.getFramebuffer(), intermediateFb, new FramebufferAttachment[]{FramebufferAttachment.COLOR_ATTACHMENT0, FramebufferAttachment.COLOR_ATTACHMENT1});

        blurRenderer.render(RenderingIntoFlag.COLOR_ONLY);

        Framebuffer.getStandardFramebuffer().bind();
        GL33.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
        GL33.glEnable(GL33.GL_FRAMEBUFFER_SRGB);
        if (resize) { GL33.glViewport(0, 0, window.width, window.height); resize = false;}

        quadRenderingEngineUnit.render(new AbstractMap.SimpleEntry<>(RenderingIntoFlag.COLOR_ONLY, RenderingParametersFlag.COLOR_ENABLED));

        GL33.glDisable(GL33.GL_FRAMEBUFFER_SRGB);
        window.swapBuffers();
    }

    @Override
    public void resize() {
        framebufferManager.resize(window.width, window.height);
        resize = true;
    }

    public void destroy() {
        destroyAllManagers();
        matricesUniforms.destroy();
        mainRenderer.destroy();
        blurRenderer.destroy();
        intermediateFb.destroy();
        quadRenderingEngineUnit.destroy();
    }

    public Camera getCamera() {
        return camera;
    }

    public LightManager getLightManager() {
        return lightManager;
    }

}
