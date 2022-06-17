package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class FramebufferRenderbuffer implements FramebufferObject {

    public FramebufferAttachment attachment;
    public Renderbuffer storedRenderbuffer;

    public FramebufferRenderbuffer(FramebufferAttachment attachment) {
        this.attachment = attachment;
    }

    public FramebufferRenderbuffer(int width, int height, int format, FramebufferAttachment attachment) {
        storedRenderbuffer = new Renderbuffer(width, height, format);
        this.attachment = attachment;
    }

    public void bind() {
        storedRenderbuffer.bind();
    }

    public void resize(int width, int height) {
        storedRenderbuffer.bind();
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, storedRenderbuffer.format, width, height);
        Renderbuffer.unbind();
    }

    @Override
    public FramebufferAttachment getFramebufferAttachment() {
        return attachment;
    }

    public void destroy() {
        storedRenderbuffer.destroy();
    }

    public static void unbind() {
        Renderbuffer.unbind();
    }

}
