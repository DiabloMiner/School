package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;

import java.util.ArrayList;
import java.util.List;

public class IntegerElement extends UniformBufferBlockElement {

    public static final int typeSize = Integer.BYTES;

    public int data;

    public IntegerElement(int data) {
        super(typeSize);
        this.data = data;
    }

    @Override
    void setUniformBlockData(int offset, UniformBufferBlock uniformBufferBlock) {
        uniformBufferBlock.setUniformBlockDataBindless(offset, BufferUtil.createBuffer(data));
    }

    public static List<UniformBufferBlockElement> createElementList(List<Integer> integers) {
        List<UniformBufferBlockElement> elements = new ArrayList<>();
        integers.forEach(num -> elements.add(new IntegerElement(num)));
        return elements;
    }

    public static IntegerElement getEmptyElement() {
        return new IntegerElement(0);
    }

}
