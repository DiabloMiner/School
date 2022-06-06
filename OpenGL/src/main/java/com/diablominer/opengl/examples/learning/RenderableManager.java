package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RenderableManager {

    public List<Renderable> allRenderables;
    public Set<Renderable> allRenderablesThrowingShadows;

    public RenderableManager() {
        allRenderables = new ArrayList<>();
        allRenderablesThrowingShadows = new HashSet<>();
    }

    public void addRenderable(Renderable renderable) {
        allRenderables.add(renderable);
        if (renderable.hasShadow()) { allRenderablesThrowingShadows.add(renderable); }
    }

    public void addRenderables(List<Renderable> renderables) {
        for (Renderable renderable : renderables) {
            addRenderable(renderable);
        }
    }

    public void destroyAllRenderables() {
        for (Renderable renderable : allRenderables) {
            renderable.destroy();
        }
        RenderablePointLight.destroyModel();
    }

}
