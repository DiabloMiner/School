package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class CubeMap implements Texture {

    private int id;
    public int width, height;
    public int index;

    public CubeMap(String directory, String fileType, boolean flipImage) {
        String[] files = {directory + File.separator + "right" + fileType, directory + File.separator + "left" + fileType, directory + File.separator + "top" + fileType,
                directory + File.separator + "bottom" + fileType, directory + File.separator + "front" + fileType, directory + File.separator + "back" + fileType};
        this.id = GL33.glGenTextures();
        this.index = -1;
        bind();

        for (int i = 0; i < files.length; i++) {
            IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(flipImage);
            ByteBuffer buffer = STBImage.stbi_load(files[i], xBuffer, yBuffer, channelsBuffer, 4);

            width = xBuffer.get();
            height = yBuffer.get();
            if (buffer != null) {
                GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL33.GL_SRGB_ALPHA, width, height, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
                STBImage.stbi_image_free(buffer);
            } else {
                System.err.println("CubeMap loading has failed, because the files couldn't be loaded from the given directory.");
            }
        }

        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

        unbind();
        allTextures.add(this);
    }

    private CubeMap(int id) {
        // WARNING: This constructor can only be used if a cubemap has already been generated for OpenGL and needs to be registered in this texture handling system
        this.id = id;
        this.index = -1;
        bind();
        IntBuffer x = MemoryUtil.memAllocInt(1),y = MemoryUtil.memAllocInt(1);
        GL33.glGetTexLevelParameteriv(GL33.GL_TEXTURE_2D, 0, GL33.GL_TEXTURE_WIDTH, x);
        GL33.glGetTexLevelParameteriv(GL33.GL_TEXTURE_2D, 0, GL33.GL_TEXTURE_HEIGHT, y);
        this.width = x.get();
        this.height = y.get();
        unbind();
        allTextures.add(this);
    }

    public CubeMap(int width, int height, int internalFormat, int format, int type, int minFilter) {
        this.id = GL33.glGenTextures();
        this.index = -1;
        this.width = width;
        this.height = height;
        bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, minFilter);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);
        unbind();

        allTextures.add(this);
    }

    public CubeMap(int width, int height, int internalFormat, int format, int type) {
        this.id = GL33.glGenTextures();
        this.index = -1;
        this.width = width;
        this.height = height;
        bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);
        unbind();

        allTextures.add(this);
    }

    public void bind() {
        if (!alreadyBound.contains(this)) {
            this.index = alreadyBound.size();
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size());
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, this.id);
            alreadyBound.add(this);
        }
    }

    public void unbind() {
        if (index != -1) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + index);
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
            alreadyBound.remove(this);
            this.index = -1;
        }
    }

    public void nonModifyingUnbind() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter alreadyBound
        if (index != -1) {
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
            this.index = 0;
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
