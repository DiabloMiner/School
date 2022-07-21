package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class Renderbuffer {

    public enum InternalFormat {
        RG(GL33.GL_RG),
        RGB(GL33.GL_RGB),
        RGB16F(GL33.GL_RGB16F),
        RGB32F(GL33.GL_RGB32F),
        RGBA(GL33.GL_RGBA),
        RGBA16F(GL33.GL_RGBA16F),
        RGBA32F(GL33.GL_RGBA32F),
        DEPTH(GL33.GL_DEPTH_COMPONENT),
        DEPTH24(GL33.GL_DEPTH_COMPONENT24),
        STENCIL(GL33.GL_STENCIL_INDEX),
        DEPTH_STENCIL(GL33.GL_DEPTH_STENCIL),
        DEPTH24_STENCIL8(GL33.GL_DEPTH24_STENCIL8),
        SRGB(GL33.GL_SRGB),
        SRGB8(GL33.GL_SRGB8),
        SRGB_ALPHA(GL33.GL_SRGB_ALPHA),
        SRGB8_ALPHA8(GL33.GL_SRGB8_ALPHA8);

        public int value;

        InternalFormat(int value) {
            this.value = value;
        }
    }

    public int width, height;
    protected InternalFormat internalFormat;
    protected int id;

    public Renderbuffer(InternalFormat internalFormat) {
        id = GL33.glGenRenderbuffers();
        this.width = -1;
        this.height = -1;
        this.internalFormat = internalFormat;
    }

    public Renderbuffer(int width, int height, InternalFormat internalFormat) {
        id = GL33.glGenRenderbuffers();
        this.width = width;
        this.height = height;
        this.internalFormat = internalFormat;
        defineStorage(width, height, internalFormat);
    }

    public void bind() {
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, id);
    }

    public void defineStorage(int width, int height, InternalFormat internalFormat) {
        bind();
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, internalFormat.value, width, height);
    }

    public void destroy() {
        GL33.glDeleteRenderbuffers(id);
    }

    public int getId() {
        return id;
    }

    public static void unbind() {
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0);
    }

}
