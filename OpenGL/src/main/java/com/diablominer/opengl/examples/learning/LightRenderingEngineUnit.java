package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class LightRenderingEngineUnit extends StandardRenderingEngineUnit {

    public static String shaderColorVariableName = "lightColor";

    public LightRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
        renderables.addAll(RenderableLight.allRenderableLights);
    }

    private void renderAllLights() {
        for (Renderable renderable : renderables) {
            RenderableLight renderableLight = (RenderableLight) renderable;
            shaderProgram.setUniformVec3F(shaderColorVariableName, renderableLight.light.getColor());
            renderable.draw(shaderProgram);
        }
    }

    private void renderAllLights(ShaderProgram shaderProgram) {
        for (Renderable renderable : renderables) {
            RenderableLight renderableLight = (RenderableLight) renderable;
            shaderProgram.setUniformVec3F(shaderColorVariableName, renderableLight.light.getColor());
            renderable.draw(shaderProgram);
        }
    }

    @Override
    public void render() {
        render(this.shaderProgram);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        renderAllLights(shaderProgram);

        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
    }
}
