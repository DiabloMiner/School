package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.HashSet;
import java.util.Set;

public class Renderbuffer {

    public static Set<Renderbuffer> allRenderbuffers = new HashSet<>();

    private int id;
    public int width, height;

    public Renderbuffer() {
        id = GL33.glGenRenderbuffers();
        this.width = -1;
        this.height = -1;
    }

    public Renderbuffer(int format, int width, int height) {
        id = GL33.glGenRenderbuffers();
        this.width = width;
        this.height = height;
        defineStorage(format, width, height);
    }

    public Renderbuffer(int format, int width, int height, int samples) {
        id = GL33.glGenRenderbuffers();
        defineMultisampledStorage(format, width, height, samples);
    }

    public void bind() {
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, id);
    }

    public void defineStorage(int format, int width, int height) {
        bind();
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, format, width, height);
    }

    public void defineMultisampledStorage(int format, int width, int height, int samples) {
        bind();
        GL33.glRenderbufferStorageMultisample(GL33.GL_RENDERBUFFER, samples, format, width, height);
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
