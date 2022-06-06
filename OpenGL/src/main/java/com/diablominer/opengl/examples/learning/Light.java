package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface Light {

    Vector3f getColor();

    void setUniformData(ShaderProgram shaderProgram, int index);

    void unbindShadowTextures();

    void initializeShadowRenderer(Renderable[] renderables);

    Renderer getShadowRenderer();

    Matrix4f[] getLightSpaceMatrices();

}
