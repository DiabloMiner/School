package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.AbstractMap;
import java.util.Arrays;

public class BlurRenderer extends Renderer {

    public static ShaderProgram blurShaderProgram;
    static { try { blurShaderProgram = new ShaderProgram("L6SVS", "L6_GaussianBlur"); } catch (Exception e) { e.printStackTrace(); } }

    public static Vector3f clearColor = new Vector3f(0.0f, 0.0f, 0.0f);

    private final boolean horizontalAtBeginning;
    private final int iterations;
    private final PingPongQuad quad;

    public BlurRenderer(int width, int height, Texture.InternalFormat internalFormat, Texture.Format format, Texture.Type type, int iterations, Texture2D inputTex) {
        super();
        Framebuffer framebuffer1 = new Framebuffer(new FramebufferTexture2D(width, height, internalFormat, format, type, FramebufferAttachment.COLOR_ATTACHMENT0));
        Framebuffer framebuffer2 = new Framebuffer(new FramebufferTexture2D(width, height, internalFormat, format, type, FramebufferAttachment.COLOR_ATTACHMENT0));
        framebuffers.addAll(Arrays.asList(framebuffer1, framebuffer2));

        this.iterations = iterations;
        this.horizontalAtBeginning = true;
        quad = new PingPongQuad(framebuffer1.getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT0).storedTexture, framebuffer2.getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT0).storedTexture, inputTex, horizontalAtBeginning);
    }

    @Override
    public void update() { }

    @Override
    public void update(ShaderProgram shaderProgram) { }

    @Override
    public void render(RenderInto flag) {
        render(BlurRenderer.blurShaderProgram, flag);
    }

    @Override
    public void render(ShaderProgram shaderProgram, RenderInto flag) {
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
        GL33.glViewport(0, 0, framebuffers.get(0).width, framebuffers.get(0).height);

        boolean horizontal = horizontalAtBeginning;
        quad.setFirstIteration(true);
        for (int i = 0; i < iterations; i++) {
            Framebuffer framebuffer = framebuffers.get(horizontal ? 1 : 0);

            framebuffer.bind();
            GL33.glClearColor(clearColor.x, clearColor.y, clearColor.z, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT);

            quad.draw(blurShaderProgram, new AbstractMap.SimpleEntry<>(flag, RenderParameters.COLOR_ENABLED));

            horizontal = !horizontal;
        }
        Framebuffer.unbind();

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void destroy() {
        destroyFramebuffers();
        destroyRenderingEngineUnits();
    }

    public Framebuffer getFinalFramebuffer() {
        int index =  (iterations - 1) % framebuffers.size();
        return framebuffers.get(index);
    }

    public static ShaderProgram getBlurShaderProgram() throws Exception {
        if (blurShaderProgram == null) {
            blurShaderProgram = new ShaderProgram("L6SVS", "L6_GaussianBlur");
        }
        return blurShaderProgram;
    }

}
