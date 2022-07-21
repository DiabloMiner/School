package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Matrix4FElement extends UniformBufferBlockElement{

    public static final int typeSize = 4 * 4 * Float.BYTES;

    public Matrix4f data;

    public Matrix4FElement(Matrix4f data) {
        super(typeSize);
        this.data = data;
    }

    @Override
    void setUniformBlockData(int offset, UniformBufferBlock uniformBufferBlock) {
        uniformBufferBlock.setUniformBlockDataBindless(offset, BufferUtil.createBuffer(data));
    }

    public static List<UniformBufferBlockElement> createElementList(List<Matrix4f> matrices) {
        List<UniformBufferBlockElement> elements = new ArrayList<>();
        matrices.forEach(mat -> elements.add(new Matrix4FElement(mat)));
        return elements;
    }

    public static Matrix4FElement getEmptyElement() {
        return new Matrix4FElement(new Matrix4f().identity());
    }

}
