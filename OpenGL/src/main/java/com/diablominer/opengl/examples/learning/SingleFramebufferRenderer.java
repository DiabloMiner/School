package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.*;

public class SingleFramebufferRenderer extends Renderer {

    public static Vector3f clearColor = new Vector3f(0.0f, 0.0f, 0.0f);

    private final Framebuffer framebuffer;
    private final RenderingParametersFlag parametersFlag;

    public SingleFramebufferRenderer(Framebuffer framebuffer, RenderingUnit[] renderingUnits) {
        super(new ArrayList<>(Collections.singletonList(framebuffer)), new ArrayList<>(Arrays.asList(renderingUnits)));
        this.framebuffer = framebuffer;
        this.parametersFlag = RenderingParametersFlag.COLOR_DEPTH_STENCIL_ENABLED;
    }

    public SingleFramebufferRenderer(Framebuffer framebuffer, RenderingUnit[] renderingUnits, RenderingParametersFlag parametersFlag) {
        super(new ArrayList<>(Collections.singletonList(framebuffer)), new ArrayList<>(Arrays.asList(renderingUnits)));
        this.framebuffer = framebuffer;
        this.parametersFlag = parametersFlag;
    }

    public SingleFramebufferRenderer(FramebufferTexture2D[] textures, RenderingUnit[] renderingUnits) {
        super(new ArrayList<>(Collections.singletonList(new Framebuffer(textures))), new ArrayList<>(Arrays.asList(renderingUnits)));
        framebuffer = this.framebuffers.get(0);
        this.parametersFlag = RenderingParametersFlag.COLOR_DEPTH_STENCIL_ENABLED;
    }

    public SingleFramebufferRenderer(FramebufferCubeMap[] textures, RenderingUnit[] renderingUnits) {
        super(new ArrayList<>(Collections.singletonList(new Framebuffer(textures))), new ArrayList<>(Arrays.asList(renderingUnits)));
        framebuffer = this.framebuffers.get(0);
        this.parametersFlag = RenderingParametersFlag.COLOR_DEPTH_STENCIL_ENABLED;
    }

    public SingleFramebufferRenderer(FramebufferTexture2D[] textures, FramebufferRenderbuffer[] renderbuffers, RenderingUnit[] renderingUnits) {
        super(Collections.singletonList(new Framebuffer(textures, renderbuffers)), Arrays.asList(renderingUnits));
        framebuffer = this.framebuffers.get(0);
        this.parametersFlag = RenderingParametersFlag.COLOR_DEPTH_STENCIL_ENABLED;
    }

    public SingleFramebufferRenderer(FramebufferCubeMap[] textures, FramebufferRenderbuffer[] renderbuffers, RenderingUnit[] renderingUnits) {
        super(new ArrayList<>(Collections.singletonList(new Framebuffer(textures, renderbuffers))), new ArrayList<>(Arrays.asList(renderingUnits)));
        framebuffer = this.framebuffers.get(0);
        this.parametersFlag = RenderingParametersFlag.COLOR_DEPTH_STENCIL_ENABLED;
    }

    public void update() {
        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.update();
        }
    }

    public void update(ShaderProgram shaderProgram) {
        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.update(shaderProgram);
        }
    }

    public void render(RenderingIntoFlag flag) {
        framebuffer.bind();
        if (parametersFlag.depthEnabled) { GL33.glEnable(GL33.GL_DEPTH_TEST); }
        if (parametersFlag.stencilEnabled) { GL33.glEnable(GL33.GL_STENCIL_TEST); }
        setViewport();
        clear();

        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.render(new AbstractMap.SimpleEntry<>(flag, parametersFlag));
        }

        if (!parametersFlag.depthEnabled) { GL33.glEnable(GL33.GL_DEPTH_TEST); }
        if (!parametersFlag.stencilEnabled) { GL33.glEnable(GL33.GL_STENCIL_TEST); }
        Framebuffer.unbind();
    }

    public void render(ShaderProgram shaderProgram, RenderingIntoFlag flag) {
        framebuffer.bind();
        if (parametersFlag.depthEnabled) { GL33.glEnable(GL33.GL_DEPTH_TEST); }
        if (parametersFlag.stencilEnabled) { GL33.glEnable(GL33.GL_STENCIL_TEST); }
        setViewport();
        clear();

        for (RenderingUnit renderingUnit : renderingUnits) {
            renderingUnit.render(shaderProgram, new AbstractMap.SimpleEntry<>(flag, parametersFlag));
        }

        if (!parametersFlag.depthEnabled) { GL33.glEnable(GL33.GL_DEPTH_TEST); }
        if (!parametersFlag.stencilEnabled) { GL33.glEnable(GL33.GL_STENCIL_TEST); }
        Framebuffer.unbind();
    }

    public void setViewport() {
        GL33.glViewport(0, 0, framebuffer.width, framebuffer.height);
    }

    public void clear() {
        GL33.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
        int mask = 0;
        if (parametersFlag.colorEnabled) {mask |= GL33.GL_COLOR_BUFFER_BIT; }
        if (parametersFlag.depthEnabled) {mask |= GL33.GL_DEPTH_BUFFER_BIT; }
        if (parametersFlag.stencilEnabled) {mask |= GL33.GL_STENCIL_BUFFER_BIT; }
        GL33.glClear(mask);
    }

    public void destroy() {
        destroyRenderingEngineUnits();
        destroyFramebuffers();
    }

    public Framebuffer getFramebuffer() {
        return framebuffer;
    }
}
