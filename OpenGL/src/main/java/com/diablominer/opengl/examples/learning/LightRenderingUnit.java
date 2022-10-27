package com.diablominer.opengl.examples.learning;

import java.util.Map;
import java.util.Set;

public class LightRenderingUnit extends StandardRenderingUnit {

    public static String shaderColorVariable = "lightColor";

    public LightRenderingUnit(ShaderProgram shaderProgram, Set<RenderableLight> allRenderableLights) {
        super(shaderProgram, allRenderableLights.toArray(new RenderableLight[0]));
    }

    private void renderAllLights(Map.Entry<RenderInto, RenderParameters> flags) {
        for (RenderComponent renderComponent : renderComponents) {
            RenderableLight renderableLight = (RenderableLight) renderComponent;
            shaderProgram.setUniformVec3F(shaderColorVariable, renderableLight.light.color);
            renderComponent.draw(shaderProgram, flags);
        }
    }

    private void renderAllLights(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        for (RenderComponent renderComponent : renderComponents) {
            RenderableLight renderableLight = (RenderableLight) renderComponent;
            shaderProgram.setUniformVec3F(shaderColorVariable, renderableLight.light.color);
            renderComponent.draw(shaderProgram, flags);
        }
    }

    @Override
    public void render(Map.Entry<RenderInto, RenderParameters> flags) {
        this.render(this.shaderProgram, flags);
    }

    @Override
    public void render(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        renderAllLights(shaderProgram, flags);
    }
}
