package com.diablominer.opengl.utils;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.Buffer;
import java.nio.DoubleBuffer;
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

    public static FloatBuffer createBuffer(float data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(1);
        buffer.put(data).flip();
        return buffer;
    }

    public static IntBuffer createBuffer(int data) {
        IntBuffer buffer = MemoryUtil.memAllocInt(1);
        buffer.put(data).flip();
        return buffer;
    }

    public static FloatBuffer createBuffer(Vector4f data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(4);
        data.get(buffer);
        return buffer;
    }

    public static FloatBuffer createBuffer(Matrix4f data) {
        FloatBuffer buffer = MemoryUtil.memAllocFloat(4 * 4);
        data.get(buffer);
        return buffer;
    }

    public static float[] createArray(Vector4f data) {
        float[] result = new float[4];
        for (int i = 0; i < 4; i++) {
            result[i] = data.get(i);
        }
        return result;
    }

    public static float[] createArray(Matrix4f data) {
        float[] result = new float[4 * 4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result[i + 4 * j] = data.get(j, i);
            }
        }
        return result;
    }


    public static void destroyBuffer(Buffer buffer) {
        MemoryUtil.memFree(buffer);
    }
}
