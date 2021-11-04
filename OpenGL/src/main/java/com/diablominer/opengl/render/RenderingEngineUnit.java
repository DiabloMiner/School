package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.renderables.Renderable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RenderingEngineUnit {

    protected ShaderProgram shaderProgram;
    protected ShaderProgram alternativeShaderProgram;
    private List<Renderable> renderables;

    public RenderingEngineUnit(ShaderProgram shaderProgram) {
        this.shaderProgram = shaderProgram;
        this.alternativeShaderProgram = null;
        renderables = new ArrayList<>();
    }

    public RenderingEngineUnit(ShaderProgram shaderProgram, ShaderProgram alternativeShaderProgram) {
        this.shaderProgram = shaderProgram;
        this.alternativeShaderProgram = alternativeShaderProgram;
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
        this.renderables.addAll(renderables);
    }

    public List<Renderable> getRenderables() {
        return renderables;
    }

    public void renderAllRenderables() {
        for (Renderable renderable : renderables) {
            renderable.draw(shaderProgram);
        }
    }

    public void renderAllRenderablesAlternative() {
        if (alternativeShaderProgram != null) {
            for (Renderable renderable : renderables) {
                renderable.draw(alternativeShaderProgram);
            }
        } else {
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

    public Set<Renderable> containsRenderables(Set<Renderable> renderables) {
        Set<Renderable> result = new HashSet<>();
        for (Renderable renderable : renderables) {
            if (this.renderables.contains(renderable)) {
                result.add(renderable);
            }
        }
        return result;
    }

    public void renderWithAnotherShaderProgram(ShaderProgram shaderProgram) {
        ShaderProgram temporaryShaderProgram = this.shaderProgram;
        this.shaderProgram = shaderProgram;
        render();
        this.shaderProgram = temporaryShaderProgram;
    }

    public abstract void updateRenderState(Camera camera, ShaderProgram shaderProgram);

    public abstract void render();

    public abstract void renderAlternative();

}
