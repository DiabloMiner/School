package com.diablominer.opengl.examples.learning;

import java.util.Map;

public class ShadowRenderingUnit extends RenderingUnit {

    private final Light light;

    public ShadowRenderingUnit(ShaderProgram shaderProgram, Renderable[] renderables, Light light) {
        super(shaderProgram, renderables);
        this.light = light;
    }

    @Override
    public void update() {
        for (int i = 0; i < light.lightSpaceMatrices.length; i++) {
            shaderProgram.setUniformMat4F("lightSpaceMat[" + i + "]", light.lightSpaceMatrices[i]);
        }
    }

    @Override
    public void update(ShaderProgram shaderProgram) {
        for (int i = 0; i < light.lightSpaceMatrices.length; i++) {
            shaderProgram.setUniformMat4F("lightSpaceMat[" + i + "]", light.lightSpaceMatrices[i]);
        }
    }

    @Override
    public void render(Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        renderRenderables(flags);
    }

    @Override
    public void render(ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        renderRenderables(shaderProgram, flags);
    }

    @Override
    public void destroy() {
        destroyShaderProgram();
        destroyRenderables();
    }
}
