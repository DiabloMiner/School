package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class Texture2D implements Texture {

    public int target, width, height;
    protected int id, index;
    protected final int internalFormat, format, type;

    protected Texture2D(int internalFormat, int type, int width, int height) {
        id = GL33.glGenTextures();
        this.internalFormat = internalFormat;
        this.format = internalFormat;
        this.type = type;
        this.index = -1;
        this.width = width;
        this.height = height;
    }

    public Texture2D(String filename, boolean isInSRGBColorSpace, boolean flipImage) {
        this.target = GL33.GL_TEXTURE_2D;
        this.format = GL33.GL_RGBA;
        this.type = GL33.GL_UNSIGNED_BYTE;
        if (isInSRGBColorSpace) {
            this.internalFormat = GL33.GL_SRGB_ALPHA;
        } else {
            this.internalFormat = GL33.GL_RGBA;
        }

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
            GL33.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, buffer);
            GL33.glGenerateMipmap(target);

            // A few parameters for texture wrapping/filtering are set
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

            // The target constant is unbound again
            unbind();

            // Free the allocated memory for buffer
            STBImage.stbi_image_free(buffer);
        } else {
            System.err.println("TwoDimensionalTexture loading has failed, because the texture couldn't be loaded.");
        }
        // Set the initial index
        this.index = -1;
    }

    public Texture2D(String filename, boolean flipImage, int internalFormat, int format, int type, boolean hasFloatData) {
        this.target = GL33.GL_TEXTURE_2D;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        // The image is loaded and read out into a ByteBuffer
        IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
        STBImage.stbi_set_flip_vertically_on_load(flipImage);
        java.nio.Buffer buffer;
        if (hasFloatData) {
            buffer = STBImage.stbi_loadf(filename, xBuffer, yBuffer, channelsBuffer, 4);
        } else {
            buffer = STBImage.stbi_load(filename, xBuffer, yBuffer, channelsBuffer, 4);
        }

        if (buffer != null) {
            // The texture is generated and bound
            id = GL33.glGenTextures();
            bind();

            // The imageData for the texture is given and a mipmap is generated with this data
            width = xBuffer.get();
            height = yBuffer.get();
            if (hasFloatData) {
                GL33.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, (FloatBuffer) buffer);
            } else {
                GL33.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) buffer);
            }
            GL33.glGenerateMipmap(target);

            // A few parameters for texture wrapping/filtering are set
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(target, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

            // The target constant is unbound again
            unbind();

            // Free the allocated memory for buffer
            if (hasFloatData) {
                STBImage.stbi_image_free((FloatBuffer) buffer);
            } else {
                STBImage.stbi_image_free((ByteBuffer) buffer);
            }
        } else {
            System.err.println("TwoDimensionalTexture loading has failed, because the texture couldn't be loaded.");
        }
        // Set the initial index
        this.index = -1;
    }

    public Texture2D(int width, int height, int internalFormat, int format, int type) {
        this.id = GL33.glGenTextures();
        this.target = GL33.GL_TEXTURE_2D;
        this.width = width;
        this.height = height;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        bind();
        GL33.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        unbind();

        this.index = -1;
    }

    public Texture2D(int width, int height, int internalFormat, int format, int type, FloatBuffer borderColor) {
        this.id = GL33.glGenTextures();
        this.target = GL33.GL_TEXTURE_2D;
        this.width = width;
        this.height = height;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        bind();
        GL33.glTexImage2D(target, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameterfv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_BORDER_COLOR, borderColor);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(target, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        unbind();

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
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
            GL33.glBindTexture(target, 0);
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

    public boolean isBound() {
        return index != -1;
    }

    public int getIndex() {
        return index;
    }

    public static FramebufferTexture2D createBRDFLookUpTexture(int size) throws Exception {
        Framebuffer framebuffer = new Framebuffer(new FramebufferTexture2D[]{new FramebufferTexture2D(size, size, GL33.GL_RG16F, GL33.GL_RG, GL33.GL_FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0),
                new FramebufferTexture2D(size, size, GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT)});
        ShaderProgram shaderProgram = new ShaderProgram("L6_BrdfLutVS", "L6_BrdfLutFS");
        QuadMesh quad = new QuadMesh(new float[] {-1.0f,1.0f,0.0f,-1.0f,-1.0f,0.0f,1.0f,1.0f,0.0f,1.0f,1.0f,0.0f,-1.0f,-1.0f,0.0f,1.0f,-1.0f,0.0f}, new float[] {0.0f,1.0f,0.0f,0.0f,1.0f,1.0f,1.0f,1.0f,0.0f,0.0f,1.0f,0.0f}, 3, 2);

        framebuffer.bind();
        GL33.glViewport(0, 0, size, size);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);
        GL33.glDepthFunc(GL33.GL_LEQUAL);

        quad.draw(shaderProgram);

        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        Framebuffer.unbind();

        quad.destroy();
        shaderProgram.destroy();
        framebuffer.getAttached2DTextures().get(1).destroy();
        FramebufferTexture2D result = framebuffer.getAttached2DTextures().get(0);
        framebuffer.getAttached2DTextures().clear();
        Framebuffer.allFramebuffers.remove(framebuffer);
        framebuffer.destroySelf();

        return result;
    }

}
