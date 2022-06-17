package com.diablominer.opengl.examples.learning;

public abstract class RenderableLight extends Renderable {

    public Light light;

    public RenderableLight(Light light) {
        super(false);
        this.light = light;
    }

}
