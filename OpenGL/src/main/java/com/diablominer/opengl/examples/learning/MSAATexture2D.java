package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class MSAATexture2D extends Texture2D {

    public final int samples;

    public MSAATexture2D(int width, int height, InternalFormat internalFormat, int samples) {
        super(internalFormat, Type.UNSIGNED_BYTE, width, height);
        this.target = Target.MultisampledTexture2D;
        this.samples = samples;

        bind();
        GL33.glTexImage2DMultisample(target.value, samples, internalFormat.value, width, height, true);
        unbind();
    }

}
