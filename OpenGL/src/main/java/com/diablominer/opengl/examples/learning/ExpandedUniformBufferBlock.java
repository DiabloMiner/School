package com.diablominer.opengl.examples.learning;
import java.util.*;

public class ExpandedUniformBufferBlock extends UniformBufferBlock {

    private final List<UniformBufferBlockElement> elements;
    private final List<Integer> offsets;

    public ExpandedUniformBufferBlock(int usage, String name, List<UniformBufferBlockElement> elements) {
        super(usage, elements.stream().mapToInt(e -> e.size).sum(), name);
        this.elements = new ArrayList<>(elements);
        offsets = new ArrayList<>(Collections.singletonList(0));

        for (int i = 1; i < elements.size(); i++) {
            offsets.add(elements.get(i).size + offsets.get(i - 1));
        }
    }

    public ExpandedUniformBufferBlock(int usage, String name, List<Integer> sizes, int sumOfSizes) {
        super(usage, sumOfSizes, name);
        this.elements = new ArrayList<>();
        offsets = new ArrayList<>(Collections.singletonList(0));

        for (int i = 1; i < sizes.size(); i++) {
            offsets.add(sizes.get(i - 1) + offsets.get(i - 1));
        }
    }

    public void setElements(List<UniformBufferBlockElement> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
    }

    public void setElements(List<UniformBufferBlockElement> elements, List<Integer> indices) {
        for (int i : indices) {
            elements.set(i, elements.get(i));
        }
    }

    public void setUniformBlockData() {
        for (int i = 0; i < elements.size(); i++) {
            elements.get(i).setUniformBlockData(offsets.get(i), this);
        }
    }

    public void setUniformBlockData(List<Integer> indices) {
        for (int i : indices) {
            elements.get(i).setUniformBlockData(offsets.get(i), this);
        }
    }

}
