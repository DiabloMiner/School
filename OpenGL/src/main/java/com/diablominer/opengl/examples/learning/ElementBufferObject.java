package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.IntBuffer;

public class ElementBufferObject extends Buffer {

    public static int bindingTarget = GL33.GL_ELEMENT_ARRAY_BUFFER;
    private IntBuffer intBuffer;

    public ElementBufferObject() {
        super();
    }

    public ElementBufferObject(IntBuffer buffer, int usage) {
        super();
        fill(buffer, usage);
    }

    @Override
    public void bind() {
        GL33.glBindBuffer(bindingTarget, id);
    }

    public void fill(IntBuffer buffer, int usage) {
        intBuffer = buffer;

        bind();
        GL33.glBufferData(bindingTarget, buffer, usage);
    }

    public static void unbind() {
        GL33.glBindBuffer(bindingTarget, 0);
    }

    public IntBuffer getIntBuffer() {
        return intBuffer;
    }

    @Override
    void destroy() {
        destroyBuffer();
    }
}
