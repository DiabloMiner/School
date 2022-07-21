package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class FramebufferMSAATexture2D extends FramebufferTexture2D {

    public FramebufferMSAATexture2D(int width, int height, Texture.InternalFormat internalFormat, int samples, FramebufferAttachment attachment) {
        super(attachment);
        storedTexture = new MSAATexture2D(width, height, internalFormat, samples);
    }

    @Override
    public void bind() {
        storedTexture.bind();
    }

    @Override
    public void unbind() {
        storedTexture.unbind();
    }

    @Override
    public void resize(int width, int height) {
        bind();
        GL33.glTexImage2DMultisample(storedTexture.target.value, ((MSAATexture2D) storedTexture).samples, storedTexture.internalFormat.value, width, height, true);
        unbind();
    }

}
