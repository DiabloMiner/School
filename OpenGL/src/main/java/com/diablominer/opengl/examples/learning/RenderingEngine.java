package com.diablominer.opengl.examples.learning;

import java.util.*;

public abstract class RenderingEngine {

    public static Set<RenderingEngine> allRenderingEngines = new HashSet<>();

    protected List<Renderer> renderers;

    public RenderingEngine() {
        allRenderingEngines.add(this);
    }

    public RenderingEngine(Collection<Renderer> renderers) {
        allRenderingEngines.add(this);
        this.renderers = new ArrayList<>(renderers);
    }

    public abstract void render();

    public abstract void update();

    public abstract void destroy();

    public static void destroyAllRenderingEngines() {
        for (RenderingEngine renderingEngine : allRenderingEngines) {
            renderingEngine.destroy();
        }
    }

}
