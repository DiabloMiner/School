package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.Arrays;

public class StandardRenderingEngineUnit extends RenderingEngineUnit {

    public StandardRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    public StandardRenderingEngineUnit(ShaderProgram shaderProgram, Renderable[] renderables) {
        super(shaderProgram);
        this.renderables.addAll(Arrays.asList(renderables));
    }

    @Override
    public void updateRenderState() {
        // TODO: Introduce method for getting a Engine reference and make RenderingEngine
        // TODO: Create new classes: Introduce new Postprocessing Shader for Bloom, IBL, Shadowcasting ; Review assimp code ; (Improve old texture code with new code)
    }

    @Override
    public void updateRenderState(ShaderProgram shaderProgram) {}

    @Override
    public void render() {
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        renderAllRenderables();

        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        renderWithAnotherShaderProgram(shaderProgram);

        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void destroy() {
        destroyAllRenderables();
        destroyShaderProgram();
    }
}
