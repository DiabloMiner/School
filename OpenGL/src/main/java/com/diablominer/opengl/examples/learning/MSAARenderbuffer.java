package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class MSAARenderbuffer extends Renderbuffer {

    public final int samples;

    public MSAARenderbuffer(int width, int height, Renderbuffer.InternalFormat format, int samples) {
        super(width, height, format);
        this.samples = samples;
        defineMultisampledStorage(width, height, format, samples);
    }

    public void defineMultisampledStorage(int width, int height, Renderbuffer.InternalFormat format, int samples) {
        bind();
        GL33.glRenderbufferStorageMultisample(GL33.GL_RENDERBUFFER, samples, format.value, width, height);
    }

}
