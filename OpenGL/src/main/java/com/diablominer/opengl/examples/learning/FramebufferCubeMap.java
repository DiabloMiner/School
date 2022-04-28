package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.ByteBuffer;

public class FramebufferCubeMap extends CubeMap implements FramebufferObject {

    public FramebufferAttachment attachment;
    int internalFormat, format, type;

    public FramebufferCubeMap(int width, int height, int internalFormat, int format, int type, int minFilter, FramebufferAttachment attachment) {
        super(width, height, internalFormat, format, type, minFilter);
        this.attachment = attachment;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
    }

    public void resize(int width, int height) {
        bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        }
        unbind();
    }

    @Override
    public FramebufferAttachment getFramebufferAttachment() {
        return attachment;
    }
}
