package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.Collection;
import java.util.Map;

public class QuadRenderingUnit extends RenderingUnit {

    private final Quad quad;

    public QuadRenderingUnit(ShaderProgram shaderProgram, Collection<Texture2D> textures) {
        super(shaderProgram);
        quad = new Quad(textures);
        renderComponents.add(quad);
    }

    @Override
    public void update() {
        update(this.shaderProgram);
    }

    @Override
    public void update(ShaderProgram shaderProgram) {}

    @Override
    public void render(Map.Entry<RenderInto, RenderParameters> flags) {
        this.render(shaderProgram, flags);
    }

    @Override
    public void render(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
        flags.setValue(RenderParameters.COLOR_ENABLED);

        renderRenderables(shaderProgram, flags);

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void destroy() {
        destroyRenderables();
        destroyShaderProgram();
    }

}
