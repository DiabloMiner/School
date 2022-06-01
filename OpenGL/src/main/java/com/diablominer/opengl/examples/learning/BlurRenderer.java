package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;

public class BlurRenderer extends Renderer {

    public static ShaderProgram blurShaderProgram;
    static { try { blurShaderProgram = new ShaderProgram("L6SVS", "L6_GaussianBlur", false); } catch (Exception e) { e.printStackTrace(); } }

    public static Vector3f clearColor = new Vector3f(0.0f, 0.0f, 0.0f);

    private final int iterations;
    private final PingPongRenderingEngineUnit renderingEngineUnit;

    public BlurRenderer(int width, int height, int internalFormat, int format, int type, int iterations, Texture2D inputTex) {
        super();
        framebuffers.addAll(new ArrayList<>(Arrays.asList(new Framebuffer(new FramebufferTexture2D(width, height, internalFormat, format, type, FramebufferAttachment.COLOR_ATTACHMENT0)), new Framebuffer(new FramebufferTexture2D(width, height, internalFormat, format, type, FramebufferAttachment.COLOR_ATTACHMENT0)))));
        renderingEngineUnit = new PingPongRenderingEngineUnit(blurShaderProgram, this.framebuffers.get(0).getAttached2DTextures().get(0), this.framebuffers.get(1).getAttached2DTextures().get(0), inputTex);
        this.renderingEngineUnits.add(renderingEngineUnit);
        this.iterations = iterations;
    }

    public BlurRenderer(int width, int height, int internalFormat, int format, int type, ShaderProgram shaderProgram, int iterations, Texture2D inputTex) {
        super();
        framebuffers.addAll(new ArrayList<>(Arrays.asList(new Framebuffer(new FramebufferTexture2D(width, height, internalFormat, format, type, FramebufferAttachment.COLOR_ATTACHMENT0)), new Framebuffer(new FramebufferTexture2D(width, height, internalFormat, format, type, FramebufferAttachment.COLOR_ATTACHMENT0)))));
        renderingEngineUnit = new PingPongRenderingEngineUnit(shaderProgram, this.framebuffers.get(0).getAttached2DTextures().get(0), this.framebuffers.get(1).getAttached2DTextures().get(0), inputTex);
        this.renderingEngineUnits.add(renderingEngineUnit);
        this.iterations = iterations;
    }

    @Override
    public void update() {
        update(this.renderingEngineUnit.shaderProgram);
    }

    @Override
    public void update(ShaderProgram shaderProgram) {
        updateAllRenderingEngineUnits(shaderProgram);
    }

    @Override
    public void render() {
        render(this.renderingEngineUnit.shaderProgram);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        boolean horizontal = true;
        for (int i = 0; i < iterations; i++) {
            Framebuffer framebuffer = framebuffers.get(horizontal ? 1 : 0);

            framebuffer.bind();
            GL33.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);
            Learning6.engineInstance.getEventManager().executeEvent(new PingPongIterationEvent(i == 0, horizontal));
            renderingEngineUnit.render(shaderProgram);

            horizontal = !horizontal;
        }
        Framebuffer.unbind();
    }

    @Override
    public void destroy() {
        destroyFramebuffers();
        destroyRenderingEngineUnits();
    }

    public Framebuffer getFinalFramebuffer() {
        int n = (int) Math.round((((double) iterations - 1) / ((double) framebuffers.size())) - ( 1.0 / 2.0));
        int index = (iterations - 1) - n * framebuffers.size();
        return framebuffers.get(index);
    }
}
