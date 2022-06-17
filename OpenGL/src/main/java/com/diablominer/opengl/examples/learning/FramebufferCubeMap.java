package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class FramebufferCubeMap implements FramebufferObject {

    public FramebufferAttachment attachment;
    public CubeMap storedTexture;

    public FramebufferCubeMap(int width, int height, int internalFormat, int format, int type, int minFilter, int magFilter, FramebufferAttachment attachment) {
        storedTexture = new CubeMap(width, height, internalFormat, format, type, minFilter, magFilter);
        this.attachment = attachment;
    }

    public FramebufferCubeMap(int width, int height, int internalFormat, int format, int type, FramebufferAttachment attachment) {
        storedTexture = new CubeMap(width, height, internalFormat, format, type);
        this.attachment = attachment;
    }

    public void bind() {
        storedTexture.bind();
    }

    public void unbind() {
        storedTexture.unbind();
    }

    public void resize(int width, int height) {
        storedTexture.bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, storedTexture.internalFormat, width, height, 0, storedTexture.format, storedTexture.type, (ByteBuffer) null);
        }
        storedTexture.unbind();
    }

    @Override
    public FramebufferAttachment getFramebufferAttachment() {
        return attachment;
    }

    public void destroy() {
        storedTexture.destroy();
    }

}
