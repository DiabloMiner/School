package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VecMatUniformBufferBlock extends ExpandedUniformBufferBlock {

    public static final int floatByteSize = 4;
    public static final int floatsInAVec4 = 4;
    public static final int columnsInAMat4 = 4;

    public VecMatUniformBufferBlock(int numberOfVectors, int numberOfMatrices, Buffer.Usage usage, String name) {
        super(usage, name, Stream.concat(Transforms.createPrefilledList(numberOfVectors, floatByteSize * floatsInAVec4).stream(), Transforms.createPrefilledList(numberOfMatrices, floatByteSize * floatsInAVec4 * columnsInAMat4).stream()).collect(Collectors.toList()),floatByteSize * floatsInAVec4 * numberOfVectors + floatByteSize * floatsInAVec4 * columnsInAMat4 * numberOfMatrices);
    }

    public VecMatUniformBufferBlock(List<Vector4f> vectors, List<Matrix4f> matrices, Buffer.Usage usage, String name) {
        super(usage, name, Stream.concat(Transforms.createPrefilledList(vectors.size(), floatByteSize * floatsInAVec4).stream(), Transforms.createPrefilledList(matrices.size(), floatByteSize * floatsInAVec4 * columnsInAMat4).stream()).collect(Collectors.toList()),floatByteSize * floatsInAVec4 * vectors.size() + floatByteSize * floatsInAVec4 * columnsInAMat4 * matrices.size());

        List<UniformBufferBlockElement> elements = new ArrayList<>();
        vectors.forEach(vec -> elements.add(new Vector4FElement(vec)));
        matrices.forEach(mat -> elements.add(new Matrix4FElement(mat)));
        super.setElements(elements);
    }

    public void setData(List<Vector4f> vectors, List<Matrix4f> matrices) {
        List<UniformBufferBlockElement> elements = new ArrayList<>();
        vectors.forEach(vec -> elements.add(new Vector4FElement(vec)));
        matrices.forEach(mat -> elements.add(new Matrix4FElement(mat)));
        super.setElements(elements);
    }

    public void setData(List<Vector4f> vectors, List<Matrix4f> matrices, List<Integer> indices) {
        List<UniformBufferBlockElement> elements = new ArrayList<>();
        vectors.forEach(vec -> elements.add(new Vector4FElement(vec)));
        matrices.forEach(mat -> elements.add(new Matrix4FElement(mat)));
        super.setElements(elements, indices);
    }

}
