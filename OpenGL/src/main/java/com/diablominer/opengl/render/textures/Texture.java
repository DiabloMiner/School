package com.diablominer.opengl.render.textures;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

public class Texture {

    public int id;
    public String path;
    public String type;

    public static List<Texture> alreadyBound = new ArrayList<>();
    public static List<Texture> allTextures = new ArrayList<>();

    public Texture(String filename, String type) {
        // The image is loaded and read out into a ByteBuffer
        IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
        ByteBuffer buffer = STBImage.stbi_load(filename, xBuffer, yBuffer, channelsBuffer, 4);

        if (buffer != null) {
            // The texture is generated and bound
            id = GL33.glGenTextures();
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, id);

            // The imageData for the texture is given and a mipmap is generated with this data
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, xBuffer.get(), yBuffer.get(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
            GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);

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
            System.err.println("Texture loading has failed, because the texture couldn't be loaded.");
        }
    }

    public void bind() {
        if (!alreadyBound.contains(this)) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size());
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.id);
            alreadyBound.add(this);
        }
    }

    public void destroy() {
        GL33.glDeleteTextures(id);
    }

    public static void unbindAll() {
        for (int i = 0; i < alreadyBound.size(); i++) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + i);
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        }
        alreadyBound.clear();
    }

    public static int getIndexForTexture(Texture texture) {
        // For this method to work the texture from which the index is requested has to be bound already,
        // if this is not the case -1 will be returned
        return alreadyBound.indexOf(texture);
    }

    public static void destroyAllTextures() {
        for (Texture texture : allTextures) {
            texture.destroy();
        }
    }

}
