package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.*;

public class Framebuffer {

    private static Framebuffer standardFramebuffer = null;
    public static Set<Framebuffer> allFramebuffers = new HashSet<>();

    private final int id;
    public int width, height;
    private int drawBuffers;
    private List<FramebufferTexture2D> attached2DTextures;
    private List<FramebufferCubeMap> attachedCubeMaps;
    private List<FramebufferRenderbuffer> attachedRenderbuffers;

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
        this.drawBuffers = 0;

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
        this.drawBuffers = 0;

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public Framebuffer(FramebufferTexture2D[] textures) {
        id = GL33.glGenFramebuffers();
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;

        attachTexturesAndRenderbuffers(Arrays.asList(textures), new ArrayList<>(), new ArrayList<>());
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
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;

        attachTexturesAndRenderbuffers(new ArrayList<>(), Arrays.asList(textures), new ArrayList<>());
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
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;

        attachTexturesAndRenderbuffers(Arrays.asList(textures), new ArrayList<>(), Arrays.asList(renderbuffers));
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public Framebuffer(FramebufferCubeMap[] textures, FramebufferRenderbuffer[] renderbuffers) {
        id = GL33.glGenFramebuffers();
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;

        attachTexturesAndRenderbuffers(new ArrayList<>(), Arrays.asList(textures), Arrays.asList(renderbuffers));
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }

        allFramebuffers.add(this);
    }

    public void attachTexture2D(FramebufferTexture2D texture, int level) {
        texture.bind();
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, texture.getFramebufferAttachment().glType, texture.storedTexture.target, texture.storedTexture.getId(), level);
        texture.unbind();
    }

    public void attachCubeMap(FramebufferCubeMap texture, int level) {
        texture.bind();
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, texture.getFramebufferAttachment().glType, texture.storedTexture.getId(), level);
        texture.unbind();
    }

    public void attachTexture2D(FramebufferTexture2D texture) {
        texture.bind();
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, texture.getFramebufferAttachment().glType, texture.storedTexture.target, texture.storedTexture.getId(), 0);
        texture.unbind();
    }

    public void attachCubeMap(FramebufferCubeMap texture) {
        texture.bind();
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, texture.getFramebufferAttachment().glType, texture.storedTexture.getId(), 0);
        texture.unbind();
    }

    public void attachRenderBuffer(FramebufferRenderbuffer renderBuffer) {
        renderBuffer.bind();
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, renderBuffer.getFramebufferAttachment().glType, GL33.GL_RENDERBUFFER, renderBuffer.storedRenderbuffer.getId());
        Renderbuffer.unbind();
    }

    public void setDrawBuffers(List<FramebufferAttachment> colorAttachments) {
        int[] attachmentGlTypes = new int[colorAttachments.size()];
        for (int i = 0; i < colorAttachments.size(); i++) {
            attachmentGlTypes[i] = colorAttachments.get(i).glType;
        }
        GL33.glDrawBuffers(attachmentGlTypes);
    }

    public void attachTexturesAndRenderbuffers(List<FramebufferTexture2D> texture2Ds, List<FramebufferCubeMap> cubeMaps, List<FramebufferRenderbuffer> renderbuffers) {
        attached2DTextures = new ArrayList<>(texture2Ds);
        attachedCubeMaps = new ArrayList<>(cubeMaps);
        attachedRenderbuffers = new ArrayList<>(renderbuffers);

        List<FramebufferAttachment> colorAttachments = new ArrayList<>();
        bind();
        for (FramebufferTexture2D texture : texture2Ds) {
            attachTexture2D(texture);

            if (texture.attachment.isColorAttachment) {
                colorAttachments.add(texture.attachment);
            }
        }
        for (FramebufferCubeMap texture : cubeMaps) {
            attachCubeMap(texture);

            if (texture.attachment.isColorAttachment) {
                colorAttachments.add(texture.attachment);
            }
        }
        for (FramebufferRenderbuffer renderbuffer : renderbuffers) {
            attachRenderBuffer(renderbuffer);

            if (renderbuffer.attachment.isColorAttachment) {
                colorAttachments.add(renderbuffer.attachment);
            }
        }
        this.drawBuffers = colorAttachments.size();
        setDrawBuffers(colorAttachments);
        unbind();
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
        adjustSize(width, height);
    }

    public void adjustSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void destroy() {
        for (FramebufferTexture2D texture2D : attached2DTextures) {
            texture2D.destroy();
        }
        for (FramebufferCubeMap cubeMap : attachedCubeMaps) {
            cubeMap.destroy();
        }
        for (FramebufferRenderbuffer renderbuffer : attachedRenderbuffers) {
            renderbuffer.destroy();
        }
        destroySelf();
    }

    public void destroySelf() {
        GL33.glDeleteFramebuffers(id);
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
            width = texture.storedTexture.width;
            height = texture.storedTexture.height;
        }
    }

    public void addCubeMap(FramebufferCubeMap texture) {
        attachedCubeMaps.add(texture);
        if (width == -1 && height == -1) {
            width = texture.storedTexture.width;
            height = texture.storedTexture.height;
        }
    }

    public void addRenderbufffer(FramebufferRenderbuffer renderbuffer) {
        attachedRenderbuffers.add(renderbuffer);
        if (width == -1 && height == -1) {
            width = renderbuffer.storedRenderbuffer.width;
            height = renderbuffer.storedRenderbuffer.height;
        }
    }

    public int getNumberOfDrawBuffers() {
        return drawBuffers;
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
