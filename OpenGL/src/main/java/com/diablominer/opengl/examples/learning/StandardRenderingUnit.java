package com.diablominer.opengl.examples.learning;

import java.util.Map;

public class StandardRenderingUnit extends RenderingUnit {

    public StandardRenderingUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    public StandardRenderingUnit(ShaderProgram shaderProgram, RenderComponent[] renderComponents) {
        super(shaderProgram, renderComponents);
    }

    @Override
    public void update() {
        update(this.shaderProgram);
    }

    @Override
    public void update(ShaderProgram shaderProgram) {}

    @Override
    public void render(Map.Entry<RenderInto, RenderParameters> flags) {
        this.render(this.shaderProgram, flags);
    }

    @Override
    public void render(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        renderRenderables(shaderProgram, flags);
    }

    @Override
    public void destroy() {
        destroyRenderables();
        destroyShaderProgram();
    }
}
