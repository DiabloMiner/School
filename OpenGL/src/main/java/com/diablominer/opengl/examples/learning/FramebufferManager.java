package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.List;

public class FramebufferManager {

    public List<Framebuffer> framebuffers;

    public FramebufferManager() {
        framebuffers = new ArrayList<>();
    }

    public Framebuffer addFramebuffer(Framebuffer framebuffer) {
        framebuffers.add(framebuffer);
        return framebuffer;
    }

    public List<Framebuffer> addFramebuffers(List<Framebuffer> framebuffers) {
        this.framebuffers.addAll(framebuffers);
        return framebuffers;
    }


    public void resize(int width, int height) {
        for (Framebuffer framebuffer : framebuffers) {
            framebuffer.resize(width, height);
            framebuffer.bind();
            GL33.glViewport(0, 0, width, height);
        }
        Framebuffer.unbind();
    }

    public void destroy() {
        for (Framebuffer framebuffer : framebuffers) {
            framebuffer.destroy();
        }
    }

}
