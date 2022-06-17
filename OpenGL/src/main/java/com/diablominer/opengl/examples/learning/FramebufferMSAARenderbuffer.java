package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class FramebufferMSAARenderbuffer extends FramebufferRenderbuffer {

    public FramebufferMSAARenderbuffer(int width, int height, int format, int samples, FramebufferAttachment attachment) {
        super(attachment);
        storedRenderbuffer = new MSAARenderbuffer(width, height, format, samples);
    }

    @Override
    public void resize(int width, int height) {
        storedRenderbuffer.bind();
        GL33.glRenderbufferStorageMultisample(GL33.GL_RENDERBUFFER, ((MSAARenderbuffer) storedRenderbuffer).samples,storedRenderbuffer.format, width, height);
        Renderbuffer.unbind();
    }

}
