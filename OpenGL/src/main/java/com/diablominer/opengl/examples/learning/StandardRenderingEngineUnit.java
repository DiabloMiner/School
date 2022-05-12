package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class StandardRenderingEngineUnit extends RenderingEngineUnit {

    public StandardRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    public StandardRenderingEngineUnit(ShaderProgram shaderProgram, Renderable[] renderables) {
        super(shaderProgram, renderables);
    }

    @Override
    public void update() {
        update(this.shaderProgram);
        // TODO: Examine event system and why there was no light shader
        // TODO: Introduce method for getting a Engine reference and make RenderingEngine non static
        // TODO: Make IBL, Shadowcasting available ; Review assimp code ; (Improve old texture code with new code)
    }

    @Override
    public void update(ShaderProgram shaderProgram) {}

    @Override
    public void render() {
        render(this.shaderProgram);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        renderAllRenderables(shaderProgram);

        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void destroy() {
        destroyAllRenderables();
        destroyShaderProgram();
    }
}
