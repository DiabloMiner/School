package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.HashSet;
import java.util.Set;

public class Renderbuffer {

    public static Set<Renderbuffer> allRenderbuffers = new HashSet<>();

    public int width, height, format;
    protected int id;

    public Renderbuffer(int format) {
        id = GL33.glGenRenderbuffers();
        this.width = -1;
        this.height = -1;
        this.format = format;
    }

    public Renderbuffer(int width, int height, int format) {
        id = GL33.glGenRenderbuffers();
        this.width = width;
        this.height = height;
        this.format = format;
        defineStorage(width, height, format);
    }

    public void bind() {
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, id);
    }

    public void defineStorage(int width, int height, int format) {
        bind();
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, format, width, height);
    }

    public void destroy() {
        GL33.glDeleteRenderbuffers(id);
    }

    public int getId() {
        return id;
    }

    public static void unbind() {
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0);
    }

    public static void destroyAll() {
        Renderbuffer.unbind();
        for (Renderbuffer renderbuffer : allRenderbuffers) {
            renderbuffer.destroy();
        }
    }
}
