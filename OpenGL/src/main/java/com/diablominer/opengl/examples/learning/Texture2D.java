package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Texture2D implements Texture {

    private int id;
    public int width, height;
    public int index;
    public String path;
    public String type;
    public int target;

    public Texture2D(String filename, String type, boolean isInSRGBColorSpace, boolean flipImage) {
        index = -1;
        this.target = GL33.GL_TEXTURE_2D;

        // The image is loaded and read out into a ByteBuffer
        IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
        STBImage.stbi_set_flip_vertically_on_load(flipImage);
        ByteBuffer buffer = STBImage.stbi_load(filename, xBuffer, yBuffer, channelsBuffer, 4);

        if (buffer != null) {
            // The texture is generated and bound
            id = GL33.glGenTextures();
            bind();

            // The imageData for the texture is given and a mipmap is generated with this data
            width = xBuffer.get();
            height = yBuffer.get();
            if (isInSRGBColorSpace) {
                GL33.glTexImage2D(target, 0, GL33.GL_SRGB_ALPHA, width, height, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
                GL33.glGenerateMipmap(target);
            } else {
                GL33.glTexImage2D(target, 0, GL33.GL_RGBA, width, height, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
                GL33.glGenerateMipmap(target);
            }

            // A few parameters for texture wrapping/filtering are set
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

            // The target constant is unbound again
            unbind();

            // Set the path and free the allocated memory for buffer
            this.path = filename;
            this.type = type;
            STBImage.stbi_image_free(buffer);
        } else {
            System.err.println("TwoDimensionalTexture loading has failed, because the texture couldn't be loaded.");
        }
    }

    public Texture2D(int width, int height, int internalFormat, int format, int type) {
        this.id = GL33.glGenTextures();
        this.target = GL33.GL_TEXTURE_2D;
        this.width = width;
        this.height = height;
        bind();
        GL33.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        unbind();

        this.type = "";
        this.path = "";
        this.index = -1;
    }

    public Texture2D(int width, int height, int internalFormat, int samples) {
        this.id = GL33.glGenTextures();
        this.target = GL33.GL_TEXTURE_2D_MULTISAMPLE;
        this.width = width;
        this.height = height;
        bind();
        GL33.glTexImage2DMultisample(target, samples, internalFormat, width, height, true);
        unbind();

        this.type = "";
        this.path = "";
        this.index = -1;
    }

    public void bind() {
        if (!alreadyBound.contains(this)) {
            index = alreadyBound.size();
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size());
            GL33.glBindTexture(target, this.id);
            alreadyBound.add(this);
        }
    }

    public void unbind() {
        if (index != -1) {
            GL33.glBindTexture(target, 0);
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
            alreadyBound.remove(this);
        }
    }

    public void nonModifyingUnbind() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter alreadyBound
        if (index != -1) {
            GL33.glBindTexture(target, 0);
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

    public int getId() {
        return id;
    }
}
