package com.diablominer.opengl.examples.learning;

import java.util.*;

public abstract class RenderingUnit {

    protected com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram;
    protected final List<RenderComponent> renderComponents;

    public RenderingUnit(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
        renderComponents = new ArrayList<>();
    }

    public RenderingUnit(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, RenderComponent[] renderComponents) {
        this.shaderProgram = shaderProgram;
        this.renderComponents = new ArrayList<>(Arrays.asList(renderComponents));
    }

    public abstract void update();

    public abstract void update(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram);

    public abstract void render(Map.Entry<RenderInto, RenderParameters> flags);

    public abstract void render(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags);

    public abstract void destroy();

    public void renderRenderables(Map.Entry<RenderInto, RenderParameters> flags) {
        for (RenderComponent renderComponent : renderComponents) {
            renderComponent.draw(shaderProgram, flags);
        }
    }

    public void renderRenderables(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        com.diablominer.opengl.examples.learning.ShaderProgram temporaryShaderProgram = this.shaderProgram;
        this.shaderProgram = shaderProgram;
        renderRenderables(flags);
        this.shaderProgram = temporaryShaderProgram;
    }

    public Set<RenderComponent> containsRenderables(Set<RenderComponent> renderComponents) {
        Set<RenderComponent> result = new HashSet<>();
        for (RenderComponent renderComponent : renderComponents) {
            if (this.renderComponents.contains(renderComponent)) {
                result.add(renderComponent);
            }
        }
        return result;
    }

    public void addNewRenderable(RenderComponent renderComponent) {
        renderComponents.add(renderComponent);
    }

    public void addNewRenderables(List<RenderComponent> renderComponents) {
        this.renderComponents.addAll(renderComponents);
    }

    public List<RenderComponent> getRenderables() {
        return renderComponents;
    }

    public void setNewShaderProgram(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    public com.diablominer.opengl.examples.learning.ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public void destroyRenderables() {
        for (RenderComponent renderComponent : renderComponents) {
            renderComponent.destroy();
        }
    }

    public void destroyShaderProgram() {
        shaderProgram.destroy();
    }

}
