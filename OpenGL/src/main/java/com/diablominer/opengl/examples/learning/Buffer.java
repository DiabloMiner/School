package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public abstract class Buffer {

    public enum Usage {
        STREAM_DRAW(GL33.GL_STREAM_DRAW),
        STREAM_READ(GL33.GL_STREAM_READ),
        STREAM_COPY(GL33.GL_STREAM_COPY),
        STATIC_DRAW(GL33.GL_STATIC_DRAW),
        STATIC_READ(GL33.GL_STATIC_READ),
        STATIC_COPY(GL33.GL_STATIC_COPY),
        DYNAMIC_DRAW(GL33.GL_DYNAMIC_DRAW),
        DYNAMIC_READ(GL33.GL_DYNAMIC_READ),
        DYNAMIC_COPY(GL33.GL_DYNAMIC_COPY);

        public int value;

        Usage(int value) {
            this.value = value;
        }
    }

    protected int id;

    public Buffer() {
        id = GL33.glGenBuffers();
    }

    abstract void bind();

    abstract void destroy();

    public void destroyBuffer() {
        GL33.glDeleteBuffers(id);
    }

}
