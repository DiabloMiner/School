package com.diablominer.opengl.examples.learning;

public abstract class RenderingEngine implements SubEngine {

    protected LightManager lightManager;
    protected RenderableManager renderableManager;
    protected ShaderProgramManager shaderProgramManager;
    protected SkyboxManager skyboxManager;
    protected FramebufferManager framebufferManager;

    public RenderingEngine() throws Exception {
        lightManager = new LightManager();
        renderableManager = new RenderableManager();
        shaderProgramManager = new ShaderProgramManager();
        skyboxManager = new SkyboxManager();
        framebufferManager = new FramebufferManager();
    }

    public abstract void render();

    public abstract void resize();

    public LightManager getLightManager() {
        return lightManager;
    }

    public RenderableManager getRenderableManager() {
        return renderableManager;
    }

    public ShaderProgramManager getShaderProgramManager() {
        return shaderProgramManager;
    }

    public SkyboxManager getSkyboxManager() {
        return skyboxManager;
    }

    public FramebufferManager getFramebufferManager() {
        return framebufferManager;
    }

    public void destroyAllManagers() {
        renderableManager.destroyAllRenderables();
        shaderProgramManager.destroyAllShaderPrograms();
        skyboxManager.destroyAllSkyboxes();
        framebufferManager.destroy();
    }

}
