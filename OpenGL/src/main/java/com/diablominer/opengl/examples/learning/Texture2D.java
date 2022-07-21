package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.AbstractMap;

public class Texture2D extends Texture {

    protected Texture2D(InternalFormat internalFormat, Type type, int width, int height) {
        super(Target.Texture2D, internalFormat, Format.NotDefined, type, width, height);
    }

    public Texture2D(String filename, boolean isInSRGBColorSpace, boolean flipImage) {
        super(Target.Texture2D);
        this.format = Format.RGBA;
        this.type = Type.UNSIGNED_BYTE;
        if (isInSRGBColorSpace) {
            this.internalFormat = InternalFormat.SRGB_ALPHA;
        } else {
            this.internalFormat = InternalFormat.RGBA;
        }

        // The image is loaded and read out into a ByteBuffer
        IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
        STBImage.stbi_set_flip_vertically_on_load(flipImage);
        ByteBuffer buffer = STBImage.stbi_load(filename, xBuffer, yBuffer, channelsBuffer, 4);

        if (buffer != null) {
            // The texture is bound
            bind();

            // The imageData for the texture is given and a mipmap is generated with this data
            width = xBuffer.get();
            height = yBuffer.get();
            GL33.glTexImage2D(target.value, 0, internalFormat.value, width, height, 0, format.value, type.value, buffer);
            GL33.glGenerateMipmap(target.value);

            // A few parameters for texture wrapping/filtering are set
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

            // The target constant is unbound again
            unbind();

            // Free the allocated memory for buffer
            STBImage.stbi_image_free(buffer);
        } else {
            System.err.println("TwoDimensionalTexture loading has failed, because the texture couldn't be loaded.");
        }
    }

    public Texture2D(String filename, boolean flipImage, InternalFormat internalFormat, Format format, Type type, boolean hasFloatData) {
        super(Target.Texture2D, internalFormat, format, type);

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
            // The texture is bound
            bind();

            // The imageData for the texture is given and a mipmap is generated with this data
            width = xBuffer.get();
            height = yBuffer.get();
            if (hasFloatData) {
                GL33.glTexImage2D(target.value, 0, internalFormat.value, width, height, 0, format.value, type.value, (FloatBuffer) buffer);
            } else {
                GL33.glTexImage2D(target.value, 0, internalFormat.value, width, height, 0, format.value, type.value, (ByteBuffer) buffer);
            }
            GL33.glGenerateMipmap(target.value);

            // A few parameters for texture wrapping/filtering are set
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

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
    }

    public Texture2D(int width, int height, InternalFormat internalFormat, Format format, Type type) {
        super(Target.Texture2D, internalFormat, format, type, width, height);

        bind();
        GL33.glTexImage2D(target.value, 0, internalFormat.value, width, height, 0, format.value, type.value, (ByteBuffer) null);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        unbind();
    }

    public Texture2D(int width, int height, InternalFormat internalFormat, Format format, Type type, FloatBuffer borderColor) {
        super(Target.Texture2D, internalFormat, format, type, width, height);

        bind();
        GL33.glTexImage2D(target.value, 0, internalFormat.value, width, height, 0, format.value, type.value, (ByteBuffer) null);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameterfv(target.value, GL33.GL_TEXTURE_BORDER_COLOR, borderColor);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        unbind();
    }

    public static FramebufferTexture2D createBRDFLookUpTexture(int size) throws Exception {
        Framebuffer framebuffer = new Framebuffer(new FramebufferTexture2D[]{new FramebufferTexture2D(size, size, InternalFormat.RGBA16F, Format.RG, Type.FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0),
                new FramebufferTexture2D(size, size, InternalFormat.DEPTH24, Format.DEPTH, Type.FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT)});
        ShaderProgram shaderProgram = new ShaderProgram("L6_BrdfLutVS", "L6_BrdfLutFS");
        QuadMesh quad = new QuadMesh(new float[] {-1.0f,1.0f,0.0f,-1.0f,-1.0f,0.0f,1.0f,1.0f,0.0f,1.0f,1.0f,0.0f,-1.0f,-1.0f,0.0f,1.0f,-1.0f,0.0f}, new float[] {0.0f,1.0f,0.0f,0.0f,1.0f,1.0f,1.0f,1.0f,0.0f,0.0f,1.0f,0.0f}, 3, 2);

        framebuffer.bind();
        GL33.glViewport(0, 0, size, size);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);
        GL33.glDepthFunc(GL33.GL_LEQUAL);

        quad.draw(shaderProgram, new AbstractMap.SimpleEntry<>(RenderingIntoFlag.COLOR_DEPTH, RenderingParametersFlag.COLOR_DEPTH_ENABLED));

        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        Framebuffer.unbind();

        quad.destroy();
        shaderProgram.destroy();
        framebuffer.getAttached2DTexture(FramebufferAttachment.DEPTH_ATTACHMENT).destroy();
        FramebufferTexture2D result = framebuffer.getAttached2DTexture(FramebufferAttachment.COLOR_ATTACHMENT0);
        framebuffer.destroyOnlyFramebuffer();

        return result;
    }

}
