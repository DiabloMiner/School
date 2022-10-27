package com.diablominer.opengl.examples.learning;

import java.util.*;

public abstract class Renderer {

    protected final List<Framebuffer> framebuffers;
    protected final List<RenderingUnit> renderingUnits;

    public Renderer() {
        this.framebuffers = new ArrayList<>();
        this.renderingUnits = new ArrayList<>();
    }

    public Renderer(Collection<Framebuffer> framebuffers) {
        this.framebuffers = new ArrayList<>(framebuffers);
        this.renderingUnits = new ArrayList<>();
    }

    public Renderer(Collection<Framebuffer> framebuffers, Collection<RenderingUnit> renderingUnits) {
        this.framebuffers = new ArrayList<>(framebuffers);
        this.renderingUnits = new ArrayList<>(renderingUnits);
    }

    protected void updateAllRenderingEngineUnits() {
        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.update();
        }
    }

    protected void updateAllRenderingEngineUnits(ShaderProgram shaderProgram) {
        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.update(shaderProgram);
        }
    }

    protected void renderAllRenderingEngineUnits(Map.Entry<RenderInto, RenderParameters> flags) {
        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.render(flags);
        }
    }

    protected void renderAllRenderingEngineUnits(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.render(shaderProgram, flags);
        }
    }

    protected void destroyFramebuffers() {
        for (Framebuffer framebuffer : framebuffers) {
            framebuffer.destroy();
        }
    }

    protected void destroyRenderingEngineUnits() {
        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.destroy();
        }
    }

    public abstract void update();

    public abstract void update(ShaderProgram shaderProgram);

    public abstract void render(RenderInto flag);

    public abstract void render(ShaderProgram shaderProgram, RenderInto flag);

    public abstract void destroy();

    public List<Framebuffer> getFramebuffers() {
        return framebuffers;
    }

    public List<RenderingUnit> getRenderingEngineUnits() {
        return renderingUnits;
    }

}
