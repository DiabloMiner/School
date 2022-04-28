package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.HashSet;
import java.util.Set;

public abstract class Buffer {

    public static Set<Buffer> allBuffers = new HashSet<>();

    protected int id;

    public Buffer() {
        id = GL33.glGenBuffers();
        allBuffers.add(this);
    }

    abstract void bind();

    abstract void destroy();

    public void destroyBuffer() {
        GL33.glDeleteBuffers(id);
    }

    public static void destroyAll() {
        for (Buffer buffer : allBuffers) {
            buffer.destroy();
        }
    }

}
