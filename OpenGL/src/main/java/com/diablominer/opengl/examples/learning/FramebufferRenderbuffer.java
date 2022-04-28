package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class FramebufferRenderbuffer extends Renderbuffer implements FramebufferObject {

    public FramebufferAttachment attachment;
    private int format, samples;

    public FramebufferRenderbuffer(FramebufferAttachment attachment) {
        this.attachment = attachment;
        this.samples = -1;
    }

    public FramebufferRenderbuffer(int format, int width, int height, FramebufferAttachment attachment) {
        super(format, width, height);
        this.attachment = attachment;
        this.format = format;
        this.samples = -1;
    }

    public FramebufferRenderbuffer(int format, int width, int height, int samples, FramebufferAttachment attachment) {
        super(format, width, height, samples);
        this.attachment = attachment;
        this.format = format;
        this.samples = samples;
    }

    public void resize(int width, int height) {
        bind();
        if (samples == -1) {
            GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, format, width, height);
        } else {
            GL33.glRenderbufferStorageMultisample(GL33.GL_RENDERBUFFER, samples, format, width, height);
        }
        Renderbuffer.unbind();
    }

    @Override
    public FramebufferAttachment getFramebufferAttachment() {
        return attachment;
    }
}
