package com.diablominer.opengl.render.textures;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.utils.BufferUtil;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

public class TwoDimensionalTexture implements Texture {

    public int id;
    public int index;
    public String path;
    public String type;

    public TwoDimensionalTexture(String filename, String type, boolean isInSRGBColorSpace, boolean flipImage) {
        index = -1;

        // The image is loaded and read out into a ByteBuffer
        IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
        STBImage.stbi_set_flip_vertically_on_load(flipImage);
        ByteBuffer buffer = STBImage.stbi_load(filename, xBuffer, yBuffer, channelsBuffer, 4);

        if (buffer != null) {
            // The texture is generated and bound
            id = GL33.glGenTextures();
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, id);

            // The imageData for the texture is given and a mipmap is generated with this data
            if (isInSRGBColorSpace) {
                GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_SRGB_ALPHA, xBuffer.get(), yBuffer.get(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
                GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);
            } else {
                GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, xBuffer.get(), yBuffer.get(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
                GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);
            }

            // A few parameters for texture wrapping/filtering are set
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);

            // The TEXTURE_2D constant is unbound again
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

            // Set the path and free the allocated memory for buffer
            this.path = filename;
            this.type = type;
            STBImage.stbi_image_free(buffer);
        } else {
            System.err.println("TwoDimensionalTexture loading has failed, because the texture couldn't be loaded.");
        }
    }

    private TwoDimensionalTexture(int width, int height, int internalFormat, int format, int type) {
        this.id = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, id);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);

        this.type = "";
        this.path = "";
        this.index = -1;
    }

    public void bind() {
        if (!alreadyBound.contains(this)) {
            index = alreadyBound.size();
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size());
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.id);
            alreadyBound.add(this);
        }
    }

    public void unbind() {
        if (index != -1) {
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
            alreadyBound.remove(this);
        }
    }

    public void nonModifyingUnbind() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter alreadyBound
        if (index != -1) {
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
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

    public static TwoDimensionalTexture createShadowTexture(int width, int height, int internalFormat, int format, int type) {
        TwoDimensionalTexture twoDimensionalTexture = new TwoDimensionalTexture( width, height, internalFormat, format, type);

        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_BORDER);
        GL33.glTexParameterfv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_BORDER_COLOR, new float[] {1.0f, 1.0f, 1.0f, 1.0f});

        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        return twoDimensionalTexture;
    }

    public static TwoDimensionalTexture createBrdfConvolutionTexture(int size) throws Exception {
        // Create framebuffer
        int frameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);

        TwoDimensionalTexture texture = new TwoDimensionalTexture(size, size, GL33.GL_RG16F, GL33.GL_RG, GL33.GL_FLOAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture.id);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, texture.id, 0);

        TwoDimensionalTexture renderBuffer = new TwoDimensionalTexture(size, size, GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, renderBuffer.id);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_ATTACHMENT, GL33.GL_TEXTURE_2D, renderBuffer.id, 0);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);

        // Create shaderprogram
        ShaderProgram shaderProgram = new ShaderProgram("./brdfConvolution/VertexShader", "./brdfConvolution/FragmentShader");

        // Define vertices
        float[] rectangleVertices = {
            -1.0f,  1.0f, 0.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
            1.0f,  1.0f, 0.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
        };
        FloatBuffer vertices = BufferUtil.createBuffer(rectangleVertices);

        // Prepare for rendering
        int VAO = GL33.glGenVertexArrays();
        int VBO = GL33.glGenBuffers();

        GL33.glBindVertexArray(VAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL33.glVertexAttribPointer(1, 2, GL33.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        // Render the rectangle
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glViewport(0, 0, size, size);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);
        GL33.glDepthFunc(GL33.GL_LEQUAL);

        shaderProgram.bind();
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glEnableVertexAttribArray(1);
        GL33.glDrawArrays(GL33.GL_TRIANGLE_STRIP, 0, 4);
        GL33.glDisableVertexAttribArray(0);
        GL33.glDisableVertexAttribArray(1);
        GL33.glBindVertexArray(0);
        shaderProgram.unbind();

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);

        // Delete unneeded components
        GL33.glDeleteVertexArrays(VAO);
        GL33.glDeleteBuffers(VBO);
        GL33.glDeleteFramebuffers(frameBuffer);
        renderBuffer.destroy();
        shaderProgram.destroy();

        return texture;
    }
}
