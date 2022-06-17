package com.diablominer.opengl.examples.learning;

import java.util.*;

public abstract class RenderingEngine implements SubEngine {

    public static Set<RenderingEngine> allRenderingEngines = new HashSet<>();

    protected List<Renderer> renderers;
    protected LightManager lightManager;
    protected RenderableManager renderableManager;
    protected ShaderProgramManager shaderProgramManager;
    protected SkyboxManager skyboxManager;
    protected FramebufferManager framebufferManager;

    public RenderingEngine() throws Exception {
        allRenderingEngines.add(this);
        lightManager = new LightManager();
        renderableManager = new RenderableManager();
        shaderProgramManager = new ShaderProgramManager();
        skyboxManager = new SkyboxManager();
        framebufferManager = new FramebufferManager();
    }

    public RenderingEngine(Collection<Renderer> renderers) throws Exception {
        allRenderingEngines.add(this);
        this.renderers = new ArrayList<>(renderers);
        lightManager = new LightManager();
        renderableManager = new RenderableManager();
        shaderProgramManager = new ShaderProgramManager();
        skyboxManager = new SkyboxManager();
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

    public void destroyAllManagers() {
        renderableManager.destroyAllRenderables();
        shaderProgramManager.destroyAllShaderPrograms();
        skyboxManager.destroyAllSkyboxes();
    }

    public static void destroyAllRenderingEngines() {
        for (RenderingEngine renderingEngine : allRenderingEngines) {
            renderingEngine.destroy();
        }
    }

}
