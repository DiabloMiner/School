package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FramebufferRenderer extends Framebuffer {

    public static Vector3f clearColor = new Vector3f(1.0f, 0.0f, 0.0f);

    private List<RenderingEngineUnit> renderingEngineUnits;

    public FramebufferRenderer(RenderingEngineUnit[] renderingEngineUnits) {
        super();
        this.renderingEngineUnits = new ArrayList<>(Arrays.asList(renderingEngineUnits));
    }

    public FramebufferRenderer(FramebufferTexture2D[] textures, RenderingEngineUnit[] renderingEngineUnits) {
        super(textures);
        this.renderingEngineUnits = new ArrayList<>(Arrays.asList(renderingEngineUnits));
    }

    public FramebufferRenderer(FramebufferCubeMap[] textures, RenderingEngineUnit[] renderingEngineUnits) {
        super(textures);
        this.renderingEngineUnits = new ArrayList<>(Arrays.asList(renderingEngineUnits));
    }

    public FramebufferRenderer(FramebufferTexture2D[] textures, FramebufferRenderbuffer[] renderbuffers, RenderingEngineUnit[] renderingEngineUnits) {
        super(textures, renderbuffers);
        this.renderingEngineUnits = new ArrayList<>(Arrays.asList(renderingEngineUnits));
    }

    public FramebufferRenderer(FramebufferCubeMap[] textures, FramebufferRenderbuffer[] renderbuffers, RenderingEngineUnit[] renderingEngineUnits) {
        super(textures, renderbuffers);
        this.renderingEngineUnits = new ArrayList<>(Arrays.asList(renderingEngineUnits));
    }

    public void update() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.updateRenderState();
        }
    }

    public void update(ShaderProgram shaderProgram) {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.updateRenderState(shaderProgram);
        }
    }

    public void render() {
        bind();
        GL33.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render();
        }
        Framebuffer.unbind();
    }

    public void render(ShaderProgram shaderProgram) {
        bind();
        GL33.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render(shaderProgram);
        }
        Framebuffer.unbind();
    }

    public void destroy() {
        super.destroy();
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.destroy();
        }
    }

}
