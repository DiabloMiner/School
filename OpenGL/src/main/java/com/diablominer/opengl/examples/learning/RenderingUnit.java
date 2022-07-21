package com.diablominer.opengl.examples.learning;

import java.util.*;

public abstract class RenderingUnit {

    protected com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram;
    protected final List<com.diablominer.opengl.examples.learning.Renderable> renderables;

    public RenderingUnit(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
        renderables = new ArrayList<>();
    }

    public RenderingUnit(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, com.diablominer.opengl.examples.learning.Renderable[] renderables) {
        this.shaderProgram = shaderProgram;
        this.renderables = new ArrayList<>(Arrays.asList(renderables));
    }

    public abstract void update();

    public abstract void update(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram);

    public abstract void render(Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags);

    public abstract void render(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags);

    public abstract void destroy();

    public void renderRenderables(Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        for (com.diablominer.opengl.examples.learning.Renderable renderable : renderables) {
            renderable.draw(shaderProgram, flags);
        }
    }

    public void renderRenderables(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        com.diablominer.opengl.examples.learning.ShaderProgram temporaryShaderProgram = this.shaderProgram;
        this.shaderProgram = shaderProgram;
        renderRenderables(flags);
        this.shaderProgram = temporaryShaderProgram;
    }

    public Set<Renderable> containsRenderables(Set<com.diablominer.opengl.examples.learning.Renderable> renderables) {
        Set<com.diablominer.opengl.examples.learning.Renderable> result = new HashSet<>();
        for (Renderable renderable : renderables) {
            if (this.renderables.contains(renderable)) {
                result.add(renderable);
            }
        }
        return result;
    }

    public void addNewRenderable(com.diablominer.opengl.examples.learning.Renderable renderable) {
        renderables.add(renderable);
    }

    public void addNewRenderables(List<com.diablominer.opengl.examples.learning.Renderable> renderables) {
        this.renderables.addAll(renderables);
    }

    public List<com.diablominer.opengl.examples.learning.Renderable> getRenderables() {
        return renderables;
    }

    public void setNewShaderProgram(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    public com.diablominer.opengl.examples.learning.ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public void destroyRenderables() {
        for (com.diablominer.opengl.examples.learning.Renderable renderable : renderables) {
            renderable.destroy();
        }
    }

    public void destroyShaderProgram() {
        shaderProgram.destroy();
    }

}
