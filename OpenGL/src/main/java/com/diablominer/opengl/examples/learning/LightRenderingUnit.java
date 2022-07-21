package com.diablominer.opengl.examples.learning;

import java.util.Map;
import java.util.Set;

public class LightRenderingUnit extends StandardRenderingUnit {

    public static String shaderColorVariable = "lightColor";

    public LightRenderingUnit(ShaderProgram shaderProgram, Set<RenderableLight> allRenderableLights) {
        super(shaderProgram, allRenderableLights.toArray(new RenderableLight[0]));
    }

    private void renderAllLights(Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        for (Renderable renderable : renderables) {
            RenderableLight renderableLight = (RenderableLight) renderable;
            shaderProgram.setUniformVec3F(shaderColorVariable, renderableLight.light.color);
            renderable.draw(shaderProgram, flags);
        }
    }

    private void renderAllLights(ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        for (Renderable renderable : renderables) {
            RenderableLight renderableLight = (RenderableLight) renderable;
            shaderProgram.setUniformVec3F(shaderColorVariable, renderableLight.light.color);
            renderable.draw(shaderProgram, flags);
        }
    }

    @Override
    public void render(Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        this.render(this.shaderProgram, flags);
    }

    @Override
    public void render(ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        renderAllLights(shaderProgram, flags);
    }
}
