package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SingleFramebufferRenderer extends Renderer {

    public static Vector3f clearColor = new Vector3f(1.0f, 0.0f, 0.0f);

    private final Framebuffer framebuffer;

    public SingleFramebufferRenderer(Framebuffer framebuffer, RenderingEngineUnit[] renderingEngineUnits) {
        super(new ArrayList<>(Collections.singletonList(framebuffer)), new ArrayList<>(Arrays.asList(renderingEngineUnits)));
        this.framebuffer = framebuffer;
    }

    public SingleFramebufferRenderer(FramebufferTexture2D[] textures, RenderingEngineUnit[] renderingEngineUnits) {
        super(new ArrayList<>(Collections.singletonList(new Framebuffer(textures))), new ArrayList<>(Arrays.asList(renderingEngineUnits)));
        framebuffer = this.framebuffers.get(0);
    }

    public SingleFramebufferRenderer(FramebufferCubeMap[] textures, RenderingEngineUnit[] renderingEngineUnits) {
        super(new ArrayList<>(Collections.singletonList(new Framebuffer(textures))), new ArrayList<>(Arrays.asList(renderingEngineUnits)));
        framebuffer = this.framebuffers.get(0);
    }

    public SingleFramebufferRenderer(FramebufferTexture2D[] textures, FramebufferRenderbuffer[] renderbuffers, RenderingEngineUnit[] renderingEngineUnits) {
        super(new ArrayList<>(Collections.singletonList(new Framebuffer(textures, renderbuffers))), new ArrayList<>(Arrays.asList(renderingEngineUnits)));
        framebuffer = this.framebuffers.get(0);
    }

    public SingleFramebufferRenderer(FramebufferCubeMap[] textures, FramebufferRenderbuffer[] renderbuffers, RenderingEngineUnit[] renderingEngineUnits) {
        super(new ArrayList<>(Collections.singletonList(new Framebuffer(textures, renderbuffers))), new ArrayList<>(Arrays.asList(renderingEngineUnits)));
        framebuffer = this.framebuffers.get(0);
    }

    public void update() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.update();
        }
    }

    public void update(ShaderProgram shaderProgram) {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.update(shaderProgram);
        }
    }

    public void render() {
        framebuffer.bind();
        GL33.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render();
        }
        Framebuffer.unbind();
    }

    public void render(ShaderProgram shaderProgram) {
        framebuffer.bind();
        GL33.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render(shaderProgram);
        }
        Framebuffer.unbind();
    }

    public void destroy() {
        destroyRenderingEngineUnits();
        destroyFramebuffers();
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }
}
