package com.diablominer.opengl.utils;

import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class BufferUtil {

    public static FloatBuffer createBuffer(float[] data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static IntBuffer createBuffer(int[] data) {
        IntBuffer buffer = MemoryUtil.memAllocInt(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static ByteBuffer createImageBuffer(int[] data, int width, int height) {
        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = data[i * width + j];
                buffer.put((byte) ((pixel >> 16) & 0xff)); // RED
                buffer.put((byte) ((pixel >> 8) & 0xff));  // GREEN
                buffer.put((byte) (pixel & 0xff));		   // BLUE
                buffer.put((byte) ((pixel >> 24) & 0xff)); // ALPHA
            }
        }
        buffer.flip();
        return buffer;
    }

    public static void destroyBuffer(Buffer buffer) {
        MemoryUtil.memFree(buffer);
    }
}
