package com.diablominer.opengl.render.textures;

import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class CubeMap {

    public static List<CubeMap> alreadyBound = new ArrayList<>();
    public static List<CubeMap> allTextures = new ArrayList<>();
    public static int activeTextureOffset = 0;

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
                GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL33.GL_SRGB_ALPHA, xBuffer.get(), yBuffer.get(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
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

        allTextures.add(this);
    }

    public CubeMap(int width, int height, int internalFormat, int format, int type) {
        this.id = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, id);
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);

        allTextures.add(this);
    }

    public void bind() {
        activeTextureOffset = Texture.alreadyBound.size();
        if (!alreadyBound.contains(this)) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size() + activeTextureOffset);
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, this.id);
            alreadyBound.add(this);
        }
    }

    public void destroy() {
        GL33.glDeleteTextures(id);
    }

    public static int getIndexForTexture(CubeMap texture) {
        return alreadyBound.indexOf(texture) + activeTextureOffset;
    }

    public static void unbindAll() {
        for (int i = 0; i < alreadyBound.size(); i++) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + i);
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
        }
        alreadyBound.clear();
    }

    public static void destroyAllCubeMaps() {
        for (CubeMap cubeMap : allTextures) {
            cubeMap.destroy();
        }
    }

}
