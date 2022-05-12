package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.Collection;

public class QuadRenderingEngineUnit extends RenderingEngineUnit {

    private final Quad quad;

    public QuadRenderingEngineUnit(ShaderProgram shaderProgram, Collection<Texture2D> textures) {
        super(shaderProgram);
        quad = new Quad(textures);
        renderables.add(quad);
    }

    @Override
    public void update() {
        update(this.shaderProgram);
    }

    @Override
    public void update(ShaderProgram shaderProgram) {}

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
