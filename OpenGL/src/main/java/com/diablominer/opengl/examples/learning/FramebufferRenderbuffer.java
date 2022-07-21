package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class FramebufferRenderbuffer extends FramebufferObject {

    public Renderbuffer storedRenderbuffer;

    protected FramebufferRenderbuffer(FramebufferAttachment attachment) {
        super(attachment);
    }

    public FramebufferRenderbuffer(int width, int height, Renderbuffer.InternalFormat format, FramebufferAttachment attachment) {
        super(attachment);
        storedRenderbuffer = new Renderbuffer(width, height, format);
    }

    public void bind() {
        storedRenderbuffer.bind();
    }

    public void resize(int width, int height) {
        storedRenderbuffer.bind();
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, storedRenderbuffer.internalFormat.value, width, height);
        Renderbuffer.unbind();
    }

    public void destroy() {
        storedRenderbuffer.destroy();
    }

    public static void unbind() {
        Renderbuffer.unbind();
    }

}
