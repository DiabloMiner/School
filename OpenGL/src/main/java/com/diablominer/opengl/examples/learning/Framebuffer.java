package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.*;

public class Framebuffer {

    private static Framebuffer standardFramebuffer = null;
    public static Set<Framebuffer> allFramebuffers = new HashSet<>();

    private final int id;
    public int width, height;
    private final List<FramebufferTexture2D> attached2DTextures;
    private final List<FramebufferCubeMap> attachedCubeMaps;
    private final List<FramebufferRenderbuffer> attachedRenderbuffers;

    public enum FramebufferTarget {
        Framebuffer(GL33.GL_FRAMEBUFFER),
        Framebuffer_Draw(GL33.GL_DRAW_FRAMEBUFFER),
        Framebuffer_Read(GL33.GL_READ_FRAMEBUFFER);

        public final int glType;

        FramebufferTarget(int glType) {
            this.glType = glType;
        }
    }

    private Framebuffer(int id) {
        this.id = id;
        attached2DTextures = new ArrayList<>();
        attachedCubeMaps = new ArrayList<>();
        attachedRenderbuffers = new ArrayList<>();
        this.width = -1;
        this.height = -1;

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public Framebuffer() {
        id = GL33.glGenFramebuffers();
        attached2DTextures = new ArrayList<>();
        attachedCubeMaps = new ArrayList<>();
        attachedRenderbuffers = new ArrayList<>();
        this.width = -1;
        this.height = -1;

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public Framebuffer(FramebufferTexture2D[] textures) {
        id = GL33.glGenFramebuffers();
        attached2DTextures = new ArrayList<>(Arrays.asList(textures));
        attachedCubeMaps = new ArrayList<>();
        attachedRenderbuffers = new ArrayList<>();
        this.width = textures[0].width;
        this.height = textures[0].height;

        List<FramebufferAttachment> colorAttachments = new ArrayList<>();
        bind();
        for (FramebufferTexture2D texture : textures) {
            texture.bind();
            createAttached2DTexture(texture);
            texture.unbind();

            if (texture.attachment.isColorAttachment) {
                colorAttachments.add(texture.attachment);
            }
        }
        setDrawBuffers(colorAttachments);
        unbind();
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public Framebuffer(FramebufferTexture2D texture2D) {
        this(new FramebufferTexture2D[] {texture2D});
    }

    public Framebuffer(FramebufferCubeMap[] textures) {
        id = GL33.glGenFramebuffers();
        attached2DTextures = new ArrayList<>();
        attachedCubeMaps = new ArrayList<>(Arrays.asList(textures));
        attachedRenderbuffers = new ArrayList<>();
        this.width = textures[0].width;
        this.height = textures[0].height;

        List<FramebufferAttachment> colorAttachments = new ArrayList<>();
        bind();
        for (FramebufferCubeMap texture : textures) {
            texture.bind();
            createAttachedCubeMap(texture);
            texture.unbind();

            if (texture.attachment.isColorAttachment) {
                colorAttachments.add(texture.attachment);
            }
        }
        setDrawBuffers(colorAttachments);
        unbind();
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public Framebuffer(FramebufferCubeMap cubeMap) {
        this(new FramebufferCubeMap[] {cubeMap});
    }

    public Framebuffer(FramebufferTexture2D[] textures, FramebufferRenderbuffer[] renderbuffers) {
        id = GL33.glGenFramebuffers();
        attached2DTextures = new ArrayList<>(Arrays.asList(textures));
        attachedCubeMaps = new ArrayList<>();
        attachedRenderbuffers = new ArrayList<>(Arrays.asList(renderbuffers));
        this.width = textures[0].width;
        this.height = textures[0].height;

        List<FramebufferAttachment> colorAttachments = new ArrayList<>();
        bind();
        for (FramebufferTexture2D texture : textures) {
            texture.bind();
            createAttached2DTexture(texture);
            texture.unbind();

            if (texture.attachment.isColorAttachment) {
                colorAttachments.add(texture.attachment);
            }
        }
        for (FramebufferRenderbuffer renderbuffer : renderbuffers) {
            renderbuffer.bind();
            createAttachedRenderBuffer(renderbuffer);
            Renderbuffer.unbind();

            if (renderbuffer.attachment.isColorAttachment) {
                colorAttachments.add(renderbuffer.attachment);
            }
        }
        setDrawBuffers(colorAttachments);
        unbind();
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public Framebuffer(FramebufferCubeMap[] textures, FramebufferRenderbuffer[] renderbuffers) {
        id = GL33.glGenFramebuffers();
        attached2DTextures = new ArrayList<>();
        attachedCubeMaps = new ArrayList<>(Arrays.asList(textures));
        attachedRenderbuffers = new ArrayList<>(Arrays.asList(renderbuffers));
        this.width = textures[0].width;
        this.height = textures[0].height;

        List<FramebufferAttachment> colorAttachments = new ArrayList<>();
        bind();
        for (FramebufferCubeMap texture : textures) {
            texture.bind();
            createAttachedCubeMap(texture);
            texture.unbind();

            if (texture.attachment.isColorAttachment) {
                colorAttachments.add(texture.attachment);
            }
        }
        for (FramebufferRenderbuffer renderbuffer : renderbuffers) {
            renderbuffer.bind();
            createAttachedRenderBuffer(renderbuffer);
            Renderbuffer.unbind();

            if (renderbuffer.attachment.isColorAttachment) {
                colorAttachments.add(renderbuffer.attachment);
            }
        }
        setDrawBuffers(colorAttachments);
        unbind();
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    private void createAttached2DTexture(FramebufferTexture2D texture) {
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, texture.getFramebufferAttachment().glType, texture.target, texture.getId(), 0);
    }

    private void createAttachedCubeMap(FramebufferCubeMap texture) {
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, texture.getFramebufferAttachment().glType, texture.getId(), 0);
    }

    private void createAttachedRenderBuffer(FramebufferRenderbuffer renderBuffer) {
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, renderBuffer.getFramebufferAttachment().glType, GL33.GL_RENDERBUFFER, renderBuffer.getId());
    }

    private void setDrawBuffers(List<FramebufferAttachment> colorAttachments) {
        int[] attachmentGlTypes = new int[colorAttachments.size()];
        for (int i = 0; i < colorAttachments.size(); i++) {
            attachmentGlTypes[i] = colorAttachments.get(i).glType;
        }
        GL33.glDrawBuffers(attachmentGlTypes);
    }

    public void bind() {
        GL33.glBindFramebuffer(FramebufferTarget.Framebuffer.glType, id);
    }

    public void bind(FramebufferTarget target) {
        GL33.glBindFramebuffer(target.glType, id);
    }

    public void resize(int width, int height) {
        for (FramebufferTexture2D texture2D : attached2DTextures) {
            texture2D.resize(width, height);
        }
        for (FramebufferCubeMap cubeMap : attachedCubeMaps) {
            cubeMap.resize(width, height);
        }
        for (FramebufferRenderbuffer renderbuffer : attachedRenderbuffers) {
            renderbuffer.resize(width, height);
        }
        this.width = width;
        this.height = height;
    }

    public void destroy() {
        for (Texture2D texture2D : attached2DTextures) {
            texture2D.destroy();
        }
        for (CubeMap cubeMap : attachedCubeMaps) {
            cubeMap.destroy();
        }
        for (Renderbuffer renderbuffer : attachedRenderbuffers) {
            renderbuffer.destroy();
        }
    }

    public List<FramebufferTexture2D> getAttached2DTextures() {
        return attached2DTextures;
    }

    public List<FramebufferCubeMap> getAttachedCubeMaps() {
        return attachedCubeMaps;
    }

    public List<FramebufferRenderbuffer> getAttachedRenderbuffers() {
        return attachedRenderbuffers;
    }

    public void addTexture2D(FramebufferTexture2D texture) {
        attached2DTextures.add(texture);
        if (width == -1 && height == -1) {
            width = texture.width;
            height = texture.height;
        }
    }

    public void addCubeMap(FramebufferCubeMap texture) {
        attachedCubeMaps.add(texture);
        if (width == -1 && height == -1) {
            width = texture.width;
            height = texture.height;
        }
    }

    public void addRenderbufffer(FramebufferRenderbuffer renderbuffer) {
        attachedRenderbuffers.add(renderbuffer);
        if (width == -1 && height == -1) {
            width = renderbuffer.width;
            height = renderbuffer.height;
        }
    }

    public static Framebuffer getStandardFramebuffer() {
        if (standardFramebuffer == null) {
            standardFramebuffer = new Framebuffer(0);
            allFramebuffers.add(standardFramebuffer);
        }
        return standardFramebuffer;
    }

    public static void unbind() {
        GL33.glBindFramebuffer(FramebufferTarget.Framebuffer.glType, 0);
    }

    public static void unbind(FramebufferTarget target) {
        GL33.glBindFramebuffer(target.glType, 0);
    }

    public static void blitFrameBuffers(Framebuffer f1, Framebuffer f2, int srcX0, int srcY0, int srcX1, int srcY1, int destX0, int destY0, int destX1, int destY1) {
        for (FramebufferTexture2D texture : f1.attached2DTextures) {
            f1.bind();
            GL33.glReadBuffer(texture.getFramebufferAttachment().glType);
            f2.bind();
            GL33.glDrawBuffer(texture.getFramebufferAttachment().glType);
            f1.bind(FramebufferTarget.Framebuffer_Read);
            f2.bind(FramebufferTarget.Framebuffer_Draw);
            GL33.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, texture.getFramebufferAttachment().glBitType, GL33.GL_NEAREST);

            Framebuffer.unbind(FramebufferTarget.Framebuffer_Read);
            Framebuffer.unbind(FramebufferTarget.Framebuffer_Draw);
            Framebuffer.unbind();
        }

        for (FramebufferRenderbuffer renderbuffer : f1.attachedRenderbuffers) {
            f1.bind(FramebufferTarget.Framebuffer_Read);
            f2.bind(FramebufferTarget.Framebuffer_Draw);
            GL33.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, renderbuffer.getFramebufferAttachment().glBitType, GL33.GL_NEAREST);

            Framebuffer.unbind(FramebufferTarget.Framebuffer_Read);
            Framebuffer.unbind(FramebufferTarget.Framebuffer_Draw);
            Framebuffer.unbind();
        }
    }

    public static void blitFrameBuffers(Framebuffer f1, Framebuffer f2) {
        blitFrameBuffers(f1, f2, 0, 0, f1.width, f1.height, 0, 0, f1.width, f1.height);
    }

    public static void destroyAll() {
        for (Framebuffer framebuffer : allFramebuffers) {
            framebuffer.destroy();
        }
    }
}
