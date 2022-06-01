package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class SingleFramebufferRenderer extends Renderer {

    public static Vector3f clearColor = new Vector3f(1.0f, 0.0f, 0.0f);
    public static Vector3f otherClearColor = new Vector3f(0.0f, 0.0f, 0.0f);

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
        GL33.glViewport(0, 0, framebuffer.width, framebuffer.height);
        clear();
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render();
        }
        Framebuffer.unbind();
    }

    public void render(ShaderProgram shaderProgram) {
        framebuffer.bind();
        GL33.glViewport(0, 0, framebuffer.width, framebuffer.height);
        clear();
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render(shaderProgram);
        }
        Framebuffer.unbind();
    }

    public void clear() {
        for (int i = 0; i < framebuffer.getNumberOfDrawBuffers(); i++) {
            if (i == 0) {
                GL33.glClearBufferfv(GL33.GL_COLOR, i, BufferUtil.createBuffer(new Vector4f(clearColor, 1.0f)));
            } else {
                GL33.glClearBufferfv(GL33.GL_COLOR, i, BufferUtil.createBuffer(new Vector4f(otherClearColor, 1.0f)));
            }
        }
        GL33.glClear(GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
    }

    public void destroy() {
        destroyRenderingEngineUnits();
        destroyFramebuffers();
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }
}
