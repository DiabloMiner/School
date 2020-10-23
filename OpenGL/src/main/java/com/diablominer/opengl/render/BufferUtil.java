package com.diablominer.opengl.render;

import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.FloatBuffer;

public class BufferUtil {

    public static FloatBuffer createBuffer(float[] data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(data.length);
        buffer.put(data).flip();
        return buffer;
    }

    public static void destroyBuffer(Buffer buffer) {
        MemoryUtil.memFree(buffer);
    }
}
