package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpandedUniformBufferBlock extends UniformBufferBlock {

    public static int floatByteSize = 4;
    public static int floatsInAVec4 = 4;
    public static int columnsInAMat4 = 4;

    public List<Integer> vectorOffsets = new ArrayList<>();
    public List<Vector4f> vectorData = new ArrayList<>();
    public List<Integer> matrixOffsets = new ArrayList<>();
    public List<Matrix4f> matrixData = new ArrayList<>();

    public ExpandedUniformBufferBlock(int vectorSize, int matrixSize, int usage, String name) {
        super(usage, floatByteSize * floatsInAVec4 * vectorSize + floatByteSize * floatsInAVec4 * columnsInAMat4 * matrixSize, name);
        size = floatByteSize * floatsInAVec4 * vectorSize + floatByteSize * floatsInAVec4 * columnsInAMat4 * matrixSize;

        for (int i = 0; i < vectorSize; i++) {
            vectorOffsets.add(floatByteSize * floatsInAVec4 * i);
        }
        for (int i = 0; i < matrixSize; i++) {
            matrixOffsets.add(floatByteSize * floatsInAVec4 * columnsInAMat4 * i + floatByteSize * floatsInAVec4 * vectorSize);
        }
    }

    public ExpandedUniformBufferBlock(List<Vector4f> vectors, List<Matrix4f> matrices, int usage, String name) {
        super(usage, floatByteSize * floatsInAVec4 * vectors.size() + floatByteSize * floatsInAVec4 * columnsInAMat4 * matrices.size(), name);
        size = floatByteSize * floatsInAVec4 * vectors.size() + floatByteSize * floatsInAVec4 * columnsInAMat4 * matrices.size();

        vectorData = new ArrayList<>(vectors);
        matrixData = new ArrayList<>(matrices);
        for (int i = 0; i < vectors.size(); i++) {
            vectorOffsets.add(floatByteSize * floatsInAVec4 * i);
        }
        for (int i = 1; i <= matrices.size(); i++) {
            matrixOffsets.add(floatByteSize * floatsInAVec4 * columnsInAMat4 * i + floatByteSize * floatsInAVec4 * vectors.size());
        }
    }

    public void setData(Vector4f[] vectors, Matrix4f[] matrices) {
        List<Vector4f> vectorList = new ArrayList<>(Arrays.asList(vectors));
        List<Matrix4f> matrixList = new ArrayList<>(Arrays.asList(matrices));
        if (!vectorData.equals(vectorList)) {
            this.vectorData = new ArrayList<>(vectorList);
        }
        if (!matrixData.equals(matrixList)) {
            this.matrixData = new ArrayList<>(matrixList);
        }
    }

    public void setData(Vector4f[] vectors, int[] indices) {
        for (int i : indices) {
            if (i < matrixData.size()) {
                vectorData.set(i, vectors[i]);
            } else {
                vectorData.add(vectors[i]);
            }
        }
    }

    public void setData(Matrix4f[] matrices, int[] indices) {
        for (int i : indices) {
            if (i < matrixData.size()) {
                matrixData.set(i, matrices[i]);
            } else {
                matrixData.add(matrices[i]);
            }
        }
    }

    public void setUniformBlockData() {
        if (vectorData.size() != 0 || matrixData.size() != 0) {
            bind();
            for (int i = 0; i < vectorData.size(); i++) {
                setUniformBlockData(vectorOffsets.get(i), BufferUtil.createArray(vectorData.get(i)));
            }
            for (int i = 0; i < matrixData.size(); i++) {
                float[] value = new float[4 * 4];
                matrixData.get(i).get(value);
                setUniformBlockData(matrixOffsets.get(i), value);
            }
            unbind();
        }
    }

    public void setUniformBlockData(int[] vecIndices, int[] matIndices) {
        if (vectorData.size() != 0 || matrixData.size() != 0) {
            bind();
            for (int i : vecIndices) {
                setUniformBlockData(vectorOffsets.get(i), BufferUtil.createArray(vectorData.get(i)));
            }
            for (int i : matIndices) {
                float[] value = new float[4 * 4];
                matrixData.get(i).get(value);
                setUniformBlockData(matrixOffsets.get(i), value);
            }
            unbind();
        }
    }
}
