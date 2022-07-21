package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RenderableManager implements Manager {

    public static List<Renderable> staticRenderables = new ArrayList<>();

    public List<Renderable> allRenderables;
    public Set<Renderable> allRenderablesThrowingShadows;

    public RenderableManager() {
        allRenderables = new ArrayList<>();
        allRenderablesThrowingShadows = new HashSet<>();

        addStaticRenderables(RenderablePointLight.model, this, false);
        addStaticRenderables(Skybox.model, this, false);
    }

    /**
     * Only Renderables in the scene itself should be added to this manager.
     */
    public Renderable addRenderable(Renderable renderable, boolean hasShadow) {
        allRenderables.add(renderable);
        if (hasShadow) { allRenderablesThrowingShadows.add(renderable); }
        return renderable;
    }

    /**
     * Only Renderables in the scene itself should be added to this manager.
     */
    public List<Renderable> addRenderables(List<Renderable> renderables, List<Boolean> haveShadows) {
        for (int i = 0; i < renderables.size(); i++) {
            addRenderable(renderables.get(i), haveShadows.get(i));
        }
        return renderables;
    }

    public void destroyAllRenderables() {
        for (Renderable renderable : allRenderables) {
            renderable.destroy();
        }
    }

    public static void addStaticRenderables(Renderable renderable, RenderableManager manager, boolean hasShadow) {
        if (!staticRenderables.contains(renderable)) {  staticRenderables.add(renderable);}
        if (hasShadow) {  manager.allRenderablesThrowingShadows.add(renderable);}
    }

    public static void destroyAllStaticRenderables() {
        for (Renderable renderable : staticRenderables) {
            renderable.destroy();
        }
    }

}
