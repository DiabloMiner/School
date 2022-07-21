package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class FramebufferCubeMap extends FramebufferObject {

    public CubeMap storedTexture;

    public FramebufferCubeMap(int width, int height, Texture.InternalFormat internalFormat, Texture.Format format, Texture.Type type, int minFilter, int magFilter, FramebufferAttachment attachment) {
        super(attachment);
        storedTexture = new CubeMap(width, height, internalFormat, format, type, minFilter, magFilter);
    }

    public FramebufferCubeMap(int width, int height, Texture.InternalFormat internalFormat, Texture.Format format, Texture.Type type, FramebufferAttachment attachment) {
        super(attachment);
        storedTexture = new CubeMap(width, height, internalFormat, format, type);
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
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, storedTexture.internalFormat.value, width, height, 0, storedTexture.format.value, storedTexture.type.value, (ByteBuffer) null);
        }
        storedTexture.unbind();
    }

    public void destroy() {
        storedTexture.destroy();
    }

}
