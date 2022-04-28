package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class SimpleRenderingEngineUnit extends RenderingEngineUnit {

    public SimpleRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    public SimpleRenderingEngineUnit(ShaderProgram shaderProgram, Renderable[] renderables) {
        super(shaderProgram, renderables);
    }

    @Override
    public void updateRenderState() {}

    @Override
    public void updateRenderState(ShaderProgram shaderProgram) {}

    @Override
    public void render() {
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);

        renderAllRenderables();

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);

        renderAllRenderables();

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void destroy() {
        destroyAllRenderables();
    }
}
