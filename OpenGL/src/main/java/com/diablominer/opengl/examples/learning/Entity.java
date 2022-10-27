package com.diablominer.opengl.examples.learning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {

    public static Map<String, Entity> allEntities;

    protected Map<Component.Type, Component> components;

    public Entity(String id) {
        this.components = new HashMap<>();
        allEntities.putIfAbsent(id, this);
    }

    public Entity(String id, Component.Type[] types, Component[] components) {
        this.components = new HashMap<>();
        addComponents(types, components);
        allEntities.putIfAbsent(id, this);
    }

    public void addComponent(Component.Type type, Component component) {
        components.putIfAbsent(type, component);
    }

    public void addComponents(Component.Type[] types, Component[] components) {
        for (int i = 0; i < components.length; i++) {
            addComponent(types[i], components[i]);
        }
    }

    public boolean hasComponent(Component.Type type) {
        return components.containsKey(type);
    }

    public Component getComponent(Component.Type type) {
        return components.get(type);
    }

    public PhysicsComponent getPhysicsComponent() {
        return (PhysicsComponent) getComponent(Component.Type.Physics);
    }

    public RenderComponent getRenderComponent() {
        return (RenderComponent) getComponent(Component.Type.Render);
    }

    public TransformComponent getTransformComponent() {
        return (TransformComponent) getComponent(Component.Type.Transform);
    }

    public static RenderComponent[] getRenderComponents(List<Entity> entities) {
        return entities.stream().map(Entity::getRenderComponent).toArray(RenderComponent[]::new);
    }

}
