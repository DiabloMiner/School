package com.diablominer.opengl.render;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class CubeMap {

    public int id;

    public CubeMap(String directory, String fileType) {
        String[] files = {directory + File.separator + "right" + fileType, directory + File.separator + "left" + fileType, directory + File.separator + "top" + fileType,
                directory + File.separator + "bottom" + fileType, directory + File.separator + "front" + fileType, directory + File.separator + "back" + fileType};
        this.id = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, id);

        for (int i = 0; i < files.length; i++) {
            IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
            ByteBuffer buffer = STBImage.stbi_load(files[i], xBuffer, yBuffer, channelsBuffer, 4);

            if (buffer != null) {
                GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL33.GL_RGBA, xBuffer.get(), yBuffer.get(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
                GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);
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

        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
    }

    public void bind() {
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, this.id);
    }

    public static void unbindAll() {
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
    }

}
