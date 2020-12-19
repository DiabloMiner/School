package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderingEngineUnit {

    private ShaderProgram shaderProgram;
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

    public void cleanUpAllRenderables() {
        for (Renderable renderable : renderables) {
            renderable.cleanUp();
        }
    }

    public abstract void updateRenderState(Camera camera, Window window);

    public abstract void render();

}
