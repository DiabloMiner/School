package com.diablominer.opengl.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL33;

import javax.imageio.ImageIO;

public class Texture {

    public int id;
    public String type;

    public static List<Integer> alreadyBound = new ArrayList<>();

    public Texture(String filename) throws IOException {
        // The image is loaded and read out into a ByteBuffer
        BufferedImage bi = ImageIO.read(new File("./src/main/resources/textures/" + filename));
        ByteBuffer buffer = BufferUtil.createImageBuffer(bi.getRGB(0, 0, bi.getWidth(), bi.getHeight(), null, 0, bi.getWidth()), bi.getWidth(), bi.getHeight());

        // The texture is generated and bound
        id = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, id);
        GL33.glPixelStorei(GL33.GL_UNPACK_ALIGNMENT, 1);

        // The imageData for the texture is given and a mipmap is generated with this data
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, bi.getWidth(), bi.getHeight(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);

        // A few parameters for texture wrapping/filtering are set
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);

        // The TEXTURE_2D constant is unbound again and the buffer is destroyed
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        BufferUtil.destroyBuffer(buffer);
    }

    public void bind() {
        if (!alreadyBound.contains(id)) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size());
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, id);
            alreadyBound.add(id);
        }
    }

    public static void unbindAll() {
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        alreadyBound.clear();
    }

    public static int getIndexForTexture(Texture texture) {
        // For this method to work the texture from which the index is requested has to be bound already,
        // if this is not the case then just -1 will be returned
        return alreadyBound.indexOf(texture.id);
    }

}
