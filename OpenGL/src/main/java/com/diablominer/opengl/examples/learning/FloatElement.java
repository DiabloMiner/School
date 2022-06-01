package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class FloatElement extends UniformBufferBlockElement {

    public static final int typeSize = Float.BYTES;

    public float data;

    public FloatElement(float data) {
        super(typeSize);
        this.data = data;
    }

    @Override
    void setUniformBlockData(int offset, UniformBufferBlock uniformBufferBlock) {
        uniformBufferBlock.setUniformBlockData(offset, BufferUtil.createBuffer(data));
    }

    public static List<UniformBufferBlockElement> createElementList(List<Float> floats) {
        List<UniformBufferBlockElement> elements = new ArrayList<>();
        floats.forEach(num -> elements.add(new FloatElement(num)));
        return elements;
    }

    public static FloatElement getEmptyElement() {
        return new FloatElement(0.0f);
    }

}
