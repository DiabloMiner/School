package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class MSAATexture2D extends Texture2D {

    public final int samples;

    public MSAATexture2D(int width, int height, int internalFormat, int samples) {
        super(internalFormat, GL33.GL_UNSIGNED_BYTE, width, height);
        this.target = GL33.GL_TEXTURE_2D_MULTISAMPLE;
        this.samples = samples;

        bind();
        GL33.glTexImage2DMultisample(target, samples, internalFormat, width, height, true);
        unbind();
    }

}
