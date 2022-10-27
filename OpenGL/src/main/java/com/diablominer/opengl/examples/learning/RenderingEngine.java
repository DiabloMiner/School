package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderingEngine implements SubEngine {

    protected LightManager lightManager;
    protected RenderComponentManager renderComponentManager;
    protected ShaderProgramManager shaderProgramManager;
    protected SkyboxManager skyboxManager;
    protected FramebufferManager framebufferManager;
    protected List<Entity> entities;

    public RenderingEngine() throws Exception {
        this.lightManager = new LightManager();
        this.renderComponentManager = new RenderComponentManager();
        this.shaderProgramManager = new ShaderProgramManager();
        this.skyboxManager = new SkyboxManager();
        this.framebufferManager = new FramebufferManager();
        this.entities = new ArrayList<>();
    }

    public RenderingEngine(List<Entity> entities) throws Exception {
        this.lightManager = new LightManager();
        this.renderComponentManager = new RenderComponentManager();
        this.shaderProgramManager = new ShaderProgramManager();
        this.skyboxManager = new SkyboxManager();
        this.framebufferManager = new FramebufferManager();
        this.entities = new ArrayList<>(entities);
        this.renderComponentManager.addRenderComponents(entities);
    }

    public abstract void render();

    public abstract void update();

    public abstract void resize();

    public void updateEntities() {
        renderComponentManager.updateEntities();
    }

    public LightManager getLightManager() {
        return lightManager;
    }

    public RenderComponentManager getRenderableManager() {
        return renderComponentManager;
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
        renderComponentManager.destroyAllRenderComponents();
        shaderProgramManager.destroyAllShaderPrograms();
        skyboxManager.destroyAllSkyboxes();
        framebufferManager.destroy();
    }

}
