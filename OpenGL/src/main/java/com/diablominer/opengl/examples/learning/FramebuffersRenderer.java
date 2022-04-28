package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class FramebuffersRenderer {

    public static Vector3f clearColor = new Vector3f(1.0f, 0.0f, 0.0f);

    protected List<Framebuffer> framebuffers;
    protected List<RenderingEngineUnit> renderingEngineUnits;

    public FramebuffersRenderer(Framebuffer[] framebuffers, RenderingEngineUnit[] renderingEngineUnits) {
        this.framebuffers = new ArrayList<>(Arrays.asList(framebuffers));
        this.renderingEngineUnits = new ArrayList<>(Arrays.asList(renderingEngineUnits));
    }

    public abstract void update();

    public abstract void update(ShaderProgram shaderProgram);

    public abstract void render();

    public abstract void render(ShaderProgram shaderProgram);

    protected void updateAllRenderingEngineUnits() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.updateRenderState();
        }
    }

    protected void updateAllRenderingEngineUnits(ShaderProgram shaderProgram) {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.updateRenderState(shaderProgram);
        }
    }

    protected void renderAllRenderingEngineUnits() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render();
        }
    }

    protected void renderAllRenderingEngineUnits(ShaderProgram shaderProgram) {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render(shaderProgram);
        }
    }

    public void destroy() {
        for (Framebuffer framebuffer : framebuffers) {
            framebuffer.destroy();
        }
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.destroy();
        }
    }

}
