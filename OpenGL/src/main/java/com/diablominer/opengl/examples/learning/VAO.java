package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.HashMap;
import java.util.Map;

public abstract class VAO {

    protected final int id;

    public VAO() {
        id = GL33.glGenVertexArrays();
    }

    public void bind() {
        GL33.glBindVertexArray(id);
    }

    abstract void draw();

    public static void unbind() {
        GL33.glBindVertexArray(0);
    }

    abstract void destroy();

    public void destroyVAO() {
        GL33.glDeleteVertexArrays(id);
    }

}
