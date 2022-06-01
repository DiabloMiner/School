package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class FramebufferTexture2D extends Texture2D implements FramebufferObject {

    public FramebufferAttachment attachment;
    private final int internalFormat, format, type, samples;

    public FramebufferTexture2D(int width, int height, int internalFormat, int format, int type, FramebufferAttachment attachment) {
        super(width, height, internalFormat, format, type);
        this.attachment = attachment;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
        this.samples = -1;
    }

    public FramebufferTexture2D(int width, int height, int internalFormat, int format, int type, FloatBuffer borderColor, FramebufferAttachment attachment) {
        super(width, height, internalFormat, format, type, borderColor);
        this.attachment = attachment;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
        this.samples = -1;
    }

    public FramebufferTexture2D(int width, int height, int internalFormat, int samples, FramebufferAttachment attachment) {
        super(width, height, internalFormat, samples);
        this.attachment = attachment;
        this.internalFormat = internalFormat;
        this.format = internalFormat;
        this.type = GL33.GL_UNSIGNED_BYTE;
        this.samples = samples;
    }

    public void resize(int width, int height) {
        bind();
        if (samples == -1) {
            GL33.glTexImage2D(target, 0, format, width, height, 0, format, type, (ByteBuffer) null);
        } else {
            GL33.glTexImage2DMultisample(target, samples, internalFormat, width, height, true);
        }
        unbind();
    }

    @Override
    public FramebufferAttachment getFramebufferAttachment() {
        return attachment;
    }
}
