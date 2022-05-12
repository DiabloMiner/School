package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class PingPongRenderingEngineUnit extends RenderingEngineUnit {

    private final PingPongQuad quad;
    public boolean firstIteration, horizontal;

    public PingPongRenderingEngineUnit(ShaderProgram shaderProgram, Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex) {
        super(shaderProgram);
        quad = new PingPongQuad(verticalTex, horizontalTex, inputTex);
        renderables.add(quad);
    }

    @Override
    public void update() {
        update(this.shaderProgram);
    }

    @Override
    public void update(ShaderProgram shaderProgram) {
        quad.update(firstIteration, horizontal);
    }

    public void updateIterationValues(boolean firstIteration, boolean horizontal) {
        this.firstIteration = firstIteration;
        this.horizontal = horizontal;
        update();
    }

    @Override
    public void render() {
        render(this.shaderProgram);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);

        renderAllRenderables(shaderProgram);

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void destroy() {
        destroyAllRenderables();
        destroyShaderProgram();
    }

}
