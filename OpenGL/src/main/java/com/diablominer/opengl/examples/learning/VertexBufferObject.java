package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;

public class VertexBufferObject extends Buffer {

    public static int bindingTarget = GL33.GL_ARRAY_BUFFER;

    public VertexBufferObject() {
        super();
    }

    public VertexBufferObject(FloatBuffer buffer, Usage usage) {
        super();
        fill(buffer, usage);
    }

    @Override
    public void bind() {
        GL33.glBindBuffer(bindingTarget, id);
    }

    public void fill(FloatBuffer buffer, Usage usage) {
        bind();
        GL33.glBufferData(bindingTarget, buffer, usage.value);
    }

    public static void unbind() {
        GL33.glBindBuffer(bindingTarget, 0);
    }

    @Override
    void destroy() {
        destroyBuffer();
    }
}
