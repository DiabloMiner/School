package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class FramebufferTexture2D implements FramebufferObject {

    public FramebufferAttachment attachment;
    public Texture2D storedTexture;

    public FramebufferTexture2D(FramebufferAttachment attachment) {
        this.storedTexture = null;
        this.attachment = attachment;
    }

    public FramebufferTexture2D(int width, int height, int internalFormat, int format, int type, FramebufferAttachment attachment) {
        storedTexture = new Texture2D(width, height, internalFormat, format, type);
        this.attachment = attachment;
    }

    public FramebufferTexture2D(int width, int height, int internalFormat, int format, int type, FloatBuffer borderColor, FramebufferAttachment attachment) {
        storedTexture = new Texture2D(width, height, internalFormat, format, type, borderColor);
        this.attachment = attachment;
    }

    public void bind() {
        storedTexture.bind();
    }

    public void unbind() {
        storedTexture.unbind();
    }

    public void resize(int width, int height) {
        bind();
        GL33.glTexImage2D(storedTexture.target, 0, storedTexture.internalFormat, width, height, 0, storedTexture.format, storedTexture.type, (ByteBuffer) null);
        unbind();
    }

    @Override
    public FramebufferAttachment getFramebufferAttachment() {
        return attachment;
    }

    public void destroy() {
        storedTexture.destroy();
    }

}
