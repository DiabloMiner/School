package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.renderables.Renderable;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderingEngineUnit {

    protected ShaderProgram shaderProgram;
    private List<Renderable> renderables;

    public RenderingEngineUnit(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
        renderables = new ArrayList<>();
    }

    public void setNewShaderProgram(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
    }

    public ShaderProgram getShaderProgram() {
        return shaderProgram;
    }

    public void addNewRenderable(Renderable renderable) {
        renderables.add(renderable);
    }

    public void addNewRenderables(List<Renderable> renderables) {
        renderables.addAll(renderables);
    }

    public List<Renderable> getRenderables() {
        return renderables;
    }

    public void renderAllRenderables() {
        for (Renderable renderable : renderables) {
            renderable.draw(shaderProgram);
        }
    }

    public void destroyAllRenderables() {
        for (Renderable renderable : renderables) {
            renderable.destroy();
        }
    }

    public void destroyShaderProgram() {
        shaderProgram.destroy();
    }

    public boolean containsRenderable(Renderable renderable) {
        for (Renderable containedRenderable : renderables) {
            if (renderables.contains(renderable)) {
                return true;
            }
        }
        return false;
    }

    public void renderWithAnotherShaderProgram(ShaderProgram shaderProgram) {
        for (Renderable renderable : renderables) {
            renderable.draw(shaderProgram);
        }
    }

    public abstract void updateRenderState(Camera camera, ShaderProgram shaderProgram);

    public abstract void render();

}
