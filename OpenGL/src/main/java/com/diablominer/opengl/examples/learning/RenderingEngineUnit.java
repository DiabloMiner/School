package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.render.renderables.Renderable;

import java.util.*;

public abstract class RenderingEngineUnit {

    protected com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram;
    protected final List<com.diablominer.opengl.examples.learning.Renderable> renderables;

    public RenderingEngineUnit(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
        renderables = new ArrayList<>();
    }

    public RenderingEngineUnit(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram, com.diablominer.opengl.examples.learning.Renderable[] renderables) {
        this.shaderProgram = shaderProgram;
        this.renderables = new ArrayList<>(Arrays.asList(renderables));
    }

    public abstract void update();

    public abstract void update(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram);

    public abstract void render();

    public abstract void render(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram);

    public abstract void destroy();

    public void renderAllRenderables() {
        for (com.diablominer.opengl.examples.learning.Renderable renderable : renderables) {
            renderable.draw(shaderProgram);
        }
    }

    public void renderAllRenderables(com.diablominer.opengl.examples.learning.ShaderProgram shaderProgram) {
        com.diablominer.opengl.examples.learning.ShaderProgram temporaryShaderProgram = this.shaderProgram;
        this.shaderProgram = shaderProgram;
        renderAllRenderables();
        this.shaderProgram = temporaryShaderProgram;
    }

    public void destroyAllRenderables() {
        for (com.diablominer.opengl.examples.learning.Renderable renderable : renderables) {
            renderable.destroy();
        }
    }

    public void destroyShaderProgram() {
        shaderProgram.destroy();
    }

    public Set<Renderable> containsRenderables(Set<com.diablominer.opengl.render.renderables.Renderable> renderables) {
        Set<com.diablominer.opengl.render.renderables.Renderable> result = new HashSet<>();
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

}
