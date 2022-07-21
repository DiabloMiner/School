package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class Vector4FElement extends UniformBufferBlockElement {

    public static final int typeSize = 4 * Float.BYTES;

    public Vector4f data;

    public Vector4FElement(Vector3f data) {
        super(typeSize);
        this.data = new Vector4f(data, 0.0f);
    }

    public Vector4FElement(Vector4f data) {
        super(typeSize);
        this.data = data;
    }

    @Override
    void setUniformBlockData(int offset, UniformBufferBlock uniformBufferBlock) {
        uniformBufferBlock.setUniformBlockDataBindless(offset, BufferUtil.createBuffer(data));
    }

    public static List<UniformBufferBlockElement> createElementList(List<Vector4f> vectors) {
        List<UniformBufferBlockElement> elements = new ArrayList<>();
        vectors.forEach(vec -> elements.add(new Vector4FElement(vec)));
        return elements;
    }

    public static Vector4FElement getEmptyElement() {
        return new Vector4FElement(new Vector4f(0.0f));
    }

}
