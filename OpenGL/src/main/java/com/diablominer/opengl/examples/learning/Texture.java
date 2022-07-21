package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.*;

public abstract class Texture {

    public enum Target {
        Texture2D(GL33.GL_TEXTURE_2D),
        MultisampledTexture2D(GL33.GL_TEXTURE_2D_MULTISAMPLE),
        CubeMapTexture(GL33.GL_TEXTURE_CUBE_MAP);

        public int value;

        Target(int value) {
            this.value = value;
        }
    }

    public enum InternalFormat {
        RG(GL33.GL_RG),
        RGB(GL33.GL_RGB),
        RGB16F(GL33.GL_RGB16F),
        RGB32F(GL33.GL_RGB32F),
        RGBA(GL33.GL_RGBA),
        RGBA16F(GL33.GL_RGBA16F),
        RGBA32F(GL33.GL_RGBA32F),
        DEPTH(GL33.GL_DEPTH_COMPONENT),
        DEPTH24(GL33.GL_DEPTH_COMPONENT24),
        DEPTH_STENCIL(GL33.GL_DEPTH_STENCIL),
        DEPTH24_STENCIL8(GL33.GL_DEPTH24_STENCIL8),
        SRGB(GL33.GL_SRGB),
        SRGB8(GL33.GL_SRGB8),
        SRGB_ALPHA(GL33.GL_SRGB_ALPHA),
        SRGB8_ALPHA8(GL33.GL_SRGB8_ALPHA8);

        public int value;

        InternalFormat(int value) {
            this.value = value;
        }
    }

    public enum Format {
        RG(GL33.GL_RG),
        RGB(GL33.GL_RGB),
        RGBA(GL33.GL_RGBA),
        DEPTH(GL33.GL_DEPTH_COMPONENT),
        DEPTH_STENCIL(GL33.GL_DEPTH_STENCIL),
        NotDefined(-1);

        public int value;

        Format(int value) {
            this.value = value;
        }
    }

    public enum Type {
        FLOAT(GL33.GL_FLOAT),
        BYTE(GL33.GL_BYTE),
        UNSIGNED_BYTE(GL33.GL_UNSIGNED_BYTE),
        INT(GL33.GL_INT),
        UNSIGNED_INT(GL33.GL_UNSIGNED_INT);

        public int value;

        Type(int value) {
            this.value = value;
        }
    }

    public static final Set<Texture> allTextures = new HashSet<>();
    public static final List<Texture> alreadyBound = new ArrayList<>();
    public static final List<Integer> openTextureUnits = new ArrayList<>();

    public int width, height;
    public Target target;
    protected int id, index;
    protected InternalFormat internalFormat;
    protected Format format;
    protected Type type;

    public Texture(Target target, InternalFormat internalFormat, Format format, Type type, int width, int height) {
        this.id = GL33.glGenTextures();
        this.target = target;
        this.index = -1;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
        this.width = width;
        this.height = height;
        allTextures.add(this);
    }

    public Texture(Target target, InternalFormat internalFormat, Format format, Type type) {
        this.id = GL33.glGenTextures();
        this.target = target;
        this.index = -1;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
        allTextures.add(this);
    }

    public Texture(Target target) {
        this.id = GL33.glGenTextures();
        this.target = target;
        this.index = -1;
        allTextures.add(this);
    }

    public Texture(int id, Target target) {
        this.id = id;
        this.target = target;
        this.index = -1;
        allTextures.add(this);
    }

    public int getId() {
        return id;
    }

    public boolean isBound() {
        return index != -1;
    }

    public int getIndex() {
        return index;
    }

    public void bind() {
        if (!alreadyBound.contains(this)) {
            if (openTextureUnits.size() == 0) { index = alreadyBound.size(); } else { index = openTextureUnits.get(0); openTextureUnits.remove(0); }
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + index);
            GL33.glBindTexture(target.value, this.id);
            alreadyBound.add(this);
        }
    }

    public void unbind() {
        if (index != -1) {
            if (index != (alreadyBound.size() - 1)) { openTextureUnits.add(index); }
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
            GL33.glBindTexture(target.value, 0);
            alreadyBound.remove(this);
            this.index = -1;
        }
    }

    public void nonModifyingUnbind() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter alreadyBound
        if (index != -1) {
            GL33.glBindTexture(target.value, 0);
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
        }
    }

    public void destroy() {
        GL33.glDeleteTextures(id);
        allTextures.remove(this);
        alreadyBound.remove(this);
    }

    public void nonModifyingDestroy() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter allTextures
        GL33.glDeleteTextures(id);
        alreadyBound.remove(this);
    }

    public static void unbindAllTextures() {
        for (Texture texture : Texture.alreadyBound) {
            texture.nonModifyingUnbind();
        }
        Texture.alreadyBound.clear();
    }

    public static void destroyAllTextures() {
        for (Texture texture : Texture.allTextures) {
            texture.nonModifyingDestroy();
        }
        Texture.allTextures.clear();
    }

}
