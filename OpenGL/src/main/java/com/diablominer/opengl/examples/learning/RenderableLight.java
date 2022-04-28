package com.diablominer.opengl.examples.learning;

import java.util.HashSet;
import java.util.Set;

public abstract class RenderableLight extends Renderable {

    public static Set<RenderableLight> allRenderableLights = new HashSet<>();

    public Light light;

    public RenderableLight(Light light) {
        this.light = light;
        allRenderableLights.add(this);
    }

}
