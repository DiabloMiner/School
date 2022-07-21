package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class FramebufferTexture2D extends FramebufferObject {

    public Texture2D storedTexture;

    protected FramebufferTexture2D(FramebufferAttachment attachment) {
        super(attachment);
    }

    public FramebufferTexture2D(int width, int height, Texture.InternalFormat internalFormat, Texture.Format format, Texture.Type type, FramebufferAttachment attachment) {
        super(attachment);
        storedTexture = new Texture2D(width, height, internalFormat, format, type);
    }

    public FramebufferTexture2D(int width, int height, Texture.InternalFormat internalFormat, Texture.Format format, Texture.Type type, FloatBuffer borderColor, FramebufferAttachment attachment) {
        super(attachment);
        storedTexture = new Texture2D(width, height, internalFormat, format, type, borderColor);
    }

    public void bind() {
        storedTexture.bind();
    }

    public void unbind() {
        storedTexture.unbind();
    }

    public void resize(int width, int height) {
        bind();
        GL33.glTexImage2D(storedTexture.target.value, 0, storedTexture.internalFormat.value, width, height, 0, storedTexture.format.value, storedTexture.type.value, (ByteBuffer) null);
        unbind();
    }

    public void destroy() {
        storedTexture.destroy();
    }

}
