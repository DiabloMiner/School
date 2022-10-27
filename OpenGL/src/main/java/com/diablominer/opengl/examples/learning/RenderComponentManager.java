package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RenderComponentManager implements Manager {

    public static List<Renderable> staticRenderables = new ArrayList<>();

    public List<Entity> allEntities;
    public Set<RenderComponent> allRenderComponentsThrowingShadows;

    public RenderComponentManager() {
        allEntities = new ArrayList<>();
        allRenderComponentsThrowingShadows = new HashSet<>();

        addStaticRenderables(RenderablePointLight.model, this, false);
        addStaticRenderables(Skybox.model, this, false);
    }

    public void addRenderComponent(Entity entity) {
        allEntities.add(entity);
        if (entity.getRenderComponent().hasShadow) { allRenderComponentsThrowingShadows.add(entity.getRenderComponent()); }
    }

    public void addRenderComponents(List<Entity> entities) {
        for (Entity entity : entities) {
            addRenderComponent(entity);
        }
    }

    public void updateEntities() {
        for (Entity entity : allEntities) {
            entity.getRenderComponent().updateModelMatrix(entity.getTransformComponent().modelMatrix);
        }
    }

    public void destroyAllRenderComponents() {
        for (Entity entity : allEntities) {
            entity.getRenderComponent().destroy();
        }
    }

    public static void addStaticRenderables(RenderComponent renderComponent, RenderComponentManager manager, boolean hasShadow) {
        if (!staticRenderables.contains(renderComponent)) {  staticRenderables.add(renderComponent);}
        if (hasShadow) {  manager.allRenderComponentsThrowingShadows.add(renderComponent);}
    }

    public static void destroyAllStaticRenderables() {
        for (Renderable renderable : staticRenderables) {
            renderable.destroy();
        }
    }

}
