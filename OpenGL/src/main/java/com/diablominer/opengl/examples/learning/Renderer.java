package com.diablominer.opengl.examples.learning;

import java.util.*;

public abstract class Renderer {

    public static Set<Renderer> allRenderers = new HashSet<>();

    protected final List<Framebuffer> framebuffers;
    protected final List<RenderingEngineUnit> renderingEngineUnits;

    public Renderer() {
        this.framebuffers = new ArrayList<>();
        this.renderingEngineUnits = new ArrayList<>();
        allRenderers.add(this);
    }

    public Renderer(Collection<Framebuffer> framebuffers) {
        this.framebuffers = new ArrayList<>(framebuffers);
        this.renderingEngineUnits = new ArrayList<>();
        allRenderers.add(this);
    }

    public Renderer(Collection<Framebuffer> framebuffers, Collection<RenderingEngineUnit> renderingEngineUnits) {
        this.framebuffers = new ArrayList<>(framebuffers);
        this.renderingEngineUnits = new ArrayList<>(renderingEngineUnits);
        allRenderers.add(this);
    }

    protected void updateAllRenderingEngineUnits() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.update();
        }
    }

    protected void updateAllRenderingEngineUnits(ShaderProgram shaderProgram) {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.update(shaderProgram);
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

    protected void destroyFramebuffers() {
        for (Framebuffer framebuffer : framebuffers) {
            framebuffer.destroy();
        }
    }

    protected void destroyRenderingEngineUnits() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.destroy();
        }
    }

    public abstract void update();

    public abstract void update(ShaderProgram shaderProgram);

    public abstract void render();

    public abstract void render(ShaderProgram shaderProgram);

    public abstract void destroy();

    public static void destroyAllRenderers() {
        for (Renderer renderer : allRenderers) {
            renderer.destroy();
        }
    }

}
