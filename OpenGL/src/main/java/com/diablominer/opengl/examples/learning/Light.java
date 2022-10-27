package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Light {

    public Vector3f color;
    protected Renderer shadowRenderer;
    protected Matrix4f[] lightSpaceMatrices;

    public Light(Vector3f color) {
        this.color = color;
    }

    abstract void updateShadowMatrices();

    abstract void setUniformData(ShaderProgram shaderProgram, int index);

    abstract void initializeShadowRenderer(RenderComponent[] renderComponents);

}
