package com.diablominer.opengl.examples.learning;

import java.util.*;

public abstract class RenderingEngine implements SubEngine {

    public static Set<RenderingEngine> allRenderingEngines = new HashSet<>();

    protected List<Renderer> renderers;
    protected LightManager lightManager;
    protected RenderableManager renderableManager;
    protected ShaderProgramManager shaderProgramManager;

    public RenderingEngine() throws Exception {
        allRenderingEngines.add(this);
        lightManager = new LightManager();
        renderableManager = new RenderableManager();
        shaderProgramManager = new ShaderProgramManager();
    }

    public RenderingEngine(Collection<Renderer> renderers) throws Exception {
        allRenderingEngines.add(this);
        this.renderers = new ArrayList<>(renderers);
        lightManager = new LightManager();
        renderableManager = new RenderableManager();
        shaderProgramManager = new ShaderProgramManager();
    }

    public abstract void render();

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
    }

    public static void destroyAllRenderingEngines() {
        for (RenderingEngine renderingEngine : allRenderingEngines) {
            renderingEngine.destroy();
        }
    }

}
