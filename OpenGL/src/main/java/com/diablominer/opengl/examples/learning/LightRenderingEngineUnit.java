package com.diablominer.opengl.examples.learning;

import java.util.Set;

public class LightRenderingEngineUnit extends StandardRenderingEngineUnit {

    public static String shaderColorVariableName = "lightColor";

    public LightRenderingEngineUnit(ShaderProgram shaderProgram, Set<RenderableLight> allRenderableLights) {
        super(shaderProgram);
        renderables.addAll(allRenderableLights);
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
        renderAllLights(shaderProgram);
    }
}
