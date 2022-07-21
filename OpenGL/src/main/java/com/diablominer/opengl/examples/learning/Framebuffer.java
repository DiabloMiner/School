package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.*;

public class Framebuffer {

    public enum FramebufferTarget {
        Framebuffer(GL33.GL_FRAMEBUFFER),
        Framebuffer_Draw(GL33.GL_DRAW_FRAMEBUFFER),
        Framebuffer_Read(GL33.GL_READ_FRAMEBUFFER);

        public final int glType;

        FramebufferTarget(int glType) {
            this.glType = glType;
        }
    }

    public enum FramebufferList {
        Texture2DList,
        CubeMapList,
        RenderBufferList;
    }

    private static Framebuffer standardFramebuffer = null;

    private final int id;
    public int numOfDrawBuffers;
    public int width, height;
    private final Map<FramebufferAttachment, Map.Entry<FramebufferList, Integer>> attachedObjects;
    private List<FramebufferTexture2D> attached2DTextures;
    private List<FramebufferCubeMap> attachedCubeMaps;
    private List<FramebufferRenderbuffer> attachedRenderbuffers;

    private Framebuffer(int id) {
        this.id = id;
        attached2DTextures = new ArrayList<>();
        attachedCubeMaps = new ArrayList<>();
        attachedRenderbuffers = new ArrayList<>();
        attachedObjects = new HashMap<>();
        this.width = -1;
        this.height = -1;
        this.numOfDrawBuffers = 0;

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
    }

    public Framebuffer() {
        id = GL33.glGenFramebuffers();
        attached2DTextures = new ArrayList<>();
        attachedCubeMaps = new ArrayList<>();
        attachedRenderbuffers = new ArrayList<>();
        attachedObjects = new HashMap<>();
        this.width = -1;
        this.height = -1;
        this.numOfDrawBuffers = 0;

        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
    }

    public Framebuffer(FramebufferTexture2D[] textures) {
        id = GL33.glGenFramebuffers();
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;
        attachedObjects = new HashMap<>();

        attachTexturesAndRenderbuffers(Arrays.asList(textures), new ArrayList<>(), new ArrayList<>());
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
    }

    public Framebuffer(FramebufferTexture2D texture2D) {
        this(new FramebufferTexture2D[] {texture2D});
    }

    public Framebuffer(FramebufferCubeMap[] textures) {
        id = GL33.glGenFramebuffers();
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;
        attachedObjects = new HashMap<>();

        attachTexturesAndRenderbuffers(new ArrayList<>(), Arrays.asList(textures), new ArrayList<>());
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
    }

    public Framebuffer(FramebufferCubeMap cubeMap) {
        this(new FramebufferCubeMap[] {cubeMap});
    }

    public Framebuffer(FramebufferTexture2D[] textures, FramebufferRenderbuffer[] renderbuffers) {
        id = GL33.glGenFramebuffers();
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;
        attachedObjects = new HashMap<>();

        attachTexturesAndRenderbuffers(Arrays.asList(textures), new ArrayList<>(), Arrays.asList(renderbuffers));
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
    }

    public Framebuffer(FramebufferCubeMap[] textures, FramebufferRenderbuffer[] renderbuffers) {
        id = GL33.glGenFramebuffers();
        this.width = textures[0].storedTexture.width;
        this.height = textures[0].storedTexture.height;
        attachedObjects = new HashMap<>();

        attachTexturesAndRenderbuffers(new ArrayList<>(), Arrays.asList(textures), Arrays.asList(renderbuffers));
        if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer has not been completed. Framebuffer status: " + GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER));
        }
    }

    public void attachTexture2D(FramebufferTexture2D texture, int level) {
        texture.bind();
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, texture.framebufferAttachment.value, texture.storedTexture.target.value, texture.storedTexture.getId(), level);
        texture.unbind();
    }

    public void attachCubeMap(FramebufferCubeMap texture, int level) {
        texture.bind();
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, texture.framebufferAttachment.value, texture.storedTexture.getId(), level);
        texture.unbind();
    }

    public void attachTexture2D(FramebufferTexture2D texture) {
        texture.bind();
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, texture.framebufferAttachment.value, texture.storedTexture.target.value, texture.storedTexture.getId(), 0);
        texture.unbind();
    }

    public void attachCubeMap(FramebufferCubeMap texture) {
        texture.bind();
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, texture.framebufferAttachment.value, texture.storedTexture.getId(), 0);
        texture.unbind();
    }

    public void attachRenderBuffer(FramebufferRenderbuffer renderBuffer) {
        renderBuffer.bind();
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, renderBuffer.framebufferAttachment.value, GL33.GL_RENDERBUFFER, renderBuffer.storedRenderbuffer.getId());
        Renderbuffer.unbind();
    }

    public void setDrawBuffers(List<FramebufferAttachment> colorAttachments) {
        int[] attachmentGlTypes = new int[colorAttachments.size()];
        for (int i = 0; i < colorAttachments.size(); i++) {
            attachmentGlTypes[i] = colorAttachments.get(i).value;
        }
        GL33.glDrawBuffers(attachmentGlTypes);
    }

    public void attachTexturesAndRenderbuffers(List<FramebufferTexture2D> texture2Ds, List<FramebufferCubeMap> cubeMaps, List<FramebufferRenderbuffer> renderbuffers) {
        attached2DTextures = new ArrayList<>(texture2Ds);
        attachedCubeMaps = new ArrayList<>(cubeMaps);
        attachedRenderbuffers = new ArrayList<>(renderbuffers);

        List<FramebufferAttachment> colorAttachments = new ArrayList<>();
        bind();
        for (int i = 0; i < attached2DTextures.size(); i++) {
            FramebufferTexture2D texture = attached2DTextures.get(i);
            attachTexture2D(texture);

            attachedObjects.put(texture.framebufferAttachment, new AbstractMap.SimpleEntry<>(FramebufferList.Texture2DList, i));
            if (texture.framebufferAttachment.isColorAttachment) {
                colorAttachments.add(texture.framebufferAttachment);
            }
        }
        for (int i = 0; i < attachedCubeMaps.size(); i++) {
            FramebufferCubeMap texture = attachedCubeMaps.get(i);
            attachCubeMap(texture);

            attachedObjects.put(texture.framebufferAttachment, new AbstractMap.SimpleEntry<>(FramebufferList.CubeMapList, i));
            if (texture.framebufferAttachment.isColorAttachment) {
                colorAttachments.add(texture.framebufferAttachment);
            }
        }
        for (int i = 0; i < attachedRenderbuffers.size(); i++) {
            FramebufferRenderbuffer renderbuffer = attachedRenderbuffers.get(i);
            attachRenderBuffer(renderbuffer);

            attachedObjects.put(renderbuffer.framebufferAttachment, new AbstractMap.SimpleEntry<>(FramebufferList.RenderBufferList, i));
            if (renderbuffer.framebufferAttachment.isColorAttachment) {
                colorAttachments.add(renderbuffer.framebufferAttachment);
            }
        }
        this.numOfDrawBuffers = colorAttachments.size();
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

    public FramebufferObject getAttachedFramebufferObject(FramebufferAttachment attachment) {
        Map.Entry<FramebufferList, Integer> entry = attachedObjects.get(attachment);
        if (entry.getKey().equals(FramebufferList.Texture2DList)) {
            return attached2DTextures.get(entry.getValue());
        } else if (entry.getKey().equals(FramebufferList.CubeMapList)) {
            return attachedCubeMaps.get(entry.getValue());
        } else {
            return attachedRenderbuffers.get(entry.getValue());
        }
    }

    public FramebufferTexture2D getAttached2DTexture(FramebufferAttachment attachment) {
        Map.Entry<FramebufferList, Integer> entry = attachedObjects.get(attachment);
        if (entry.getKey().equals(FramebufferList.Texture2DList)) {
            return attached2DTextures.get(entry.getValue());
        } else {
            throw new IllegalArgumentException("The framebuffer does not have a 2d-texture attached at " + attachment.toString() + ".");
        }
    }

    public FramebufferCubeMap getAttachedCubeMap(FramebufferAttachment attachment) {
        Map.Entry<FramebufferList, Integer> entry = attachedObjects.get(attachment);
        if (entry.getKey().equals(FramebufferList.CubeMapList)) {
            return attachedCubeMaps.get(entry.getValue());
        } else {
            throw new IllegalArgumentException("The framebuffer does not have a 2d-texture attached at " + attachment.toString() + ".");
        }
    }

    public FramebufferRenderbuffer getAttachedRenderBuffer(FramebufferAttachment attachment) {
        Map.Entry<FramebufferList, Integer> entry = attachedObjects.get(attachment);
        if (entry.getKey().equals(FramebufferList.RenderBufferList)) {
            return attachedRenderbuffers.get(entry.getValue());
        } else {
            throw new IllegalArgumentException("The framebuffer does not have a 2d-texture attached at " + attachment.toString() + ".");
        }
    }

    public void addTexture2D(FramebufferTexture2D texture) {
        attached2DTextures.add(texture);
        attachedObjects.put(texture.framebufferAttachment, new AbstractMap.SimpleEntry<>(FramebufferList.Texture2DList, attached2DTextures.size() - 1));
        if (width == -1 && height == -1) {
            width = texture.storedTexture.width;
            height = texture.storedTexture.height;
        }
    }

    public void addCubeMap(FramebufferCubeMap texture) {
        attachedCubeMaps.add(texture);
        attachedObjects.put(texture.framebufferAttachment, new AbstractMap.SimpleEntry<>(FramebufferList.CubeMapList, attached2DTextures.size() - 1));
        if (width == -1 && height == -1) {
            width = texture.storedTexture.width;
            height = texture.storedTexture.height;
        }
    }

    public void addRenderbufffer(FramebufferRenderbuffer renderbuffer) {
        attachedRenderbuffers.add(renderbuffer);
        attachedObjects.put(renderbuffer.framebufferAttachment, new AbstractMap.SimpleEntry<>(FramebufferList.RenderBufferList, attached2DTextures.size() - 1));
        if (width == -1 && height == -1) {
            width = renderbuffer.storedRenderbuffer.width;
            height = renderbuffer.storedRenderbuffer.height;
        }
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
        destroyOnlyFramebuffer();
    }

    public void destroyOnlyFramebuffer() {
        GL33.glDeleteFramebuffers(id);
    }

    public static Framebuffer getStandardFramebuffer() {
        if (standardFramebuffer == null) {
            standardFramebuffer = new Framebuffer(0);
        }
        return standardFramebuffer;
    }

    public static void unbind() {
        GL33.glBindFramebuffer(FramebufferTarget.Framebuffer.glType, 0);
    }

    public static void unbind(FramebufferTarget target) {
        GL33.glBindFramebuffer(target.glType, 0);
    }

    public static void blitFrameBuffers(Framebuffer f1, Framebuffer f2, int srcX0, int srcY0, int srcX1, int srcY1, int destX0, int destY0, int destX1, int destY1, FramebufferAttachment[] attachments) {
        for (FramebufferAttachment attachment : attachments) {
            FramebufferObject object = f1.getAttachedFramebufferObject(attachment);
            if (!(f1.attachedObjects.get(attachment).getKey().equals(FramebufferList.RenderBufferList))) {
                f1.bind();
                GL33.glReadBuffer(object.framebufferAttachment.value);
                f2.bind();
                GL33.glDrawBuffer(object.framebufferAttachment.value);
            }
            f1.bind(FramebufferTarget.Framebuffer_Read);
            f2.bind(FramebufferTarget.Framebuffer_Draw);
            GL33.glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, destX0, destY0, destX1, destY1, object.framebufferAttachment.glBitType, GL33.GL_NEAREST);
        }

        Framebuffer.unbind(FramebufferTarget.Framebuffer_Read);
        Framebuffer.unbind(FramebufferTarget.Framebuffer_Draw);
        Framebuffer.unbind();
    }

    public static void blitFrameBuffers(Framebuffer f1, Framebuffer f2, FramebufferAttachment[] attachments) {
        blitFrameBuffers(f1, f2, 0, 0, f1.width, f1.height, 0, 0, f1.width, f1.height, attachments);
    }

    public static void blitFrameBuffers(Framebuffer f1, Framebuffer f2) {
        blitFrameBuffers(f1, f2, 0, 0, f1.width, f1.height, 0, 0, f1.width, f1.height, f1.attachedObjects.keySet().toArray(new FramebufferAttachment[0]));
    }

}
