package com.diablominer.opengl.examples.learning;

public class ShadowRenderingEngineUnit extends RenderingEngineUnit {

    private final Light light;

    public ShadowRenderingEngineUnit(ShaderProgram shaderProgram, Renderable[] renderables, Light light) {
        super(shaderProgram, renderables);
        this.light = light;
    }


    @Override
    public void update() {
        shaderProgram.setUniformMat4F("lightSpaceMat", light.getLightSpaceMatrix());
    }

    @Override
    public void update(ShaderProgram shaderProgram) {
        shaderProgram.setUniformMat4F("lightSpaceMat", light.getLightSpaceMatrix());
    }

    @Override
    public void render() {
        renderAllRenderables();
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        renderAllRenderables(shaderProgram);
    }

    @Override
    public void destroy() {
        destroyShaderProgram();
        destroyAllRenderables();
    }
}
