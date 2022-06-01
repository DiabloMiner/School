package com.diablominer.opengl.examples.learning;

public abstract class UniformBufferBlockElement {

    public int size;

    public UniformBufferBlockElement(int size) {
        this.size = size;
    }

    abstract void setUniformBlockData(int offset, UniformBufferBlock uniformBufferBlock);

    public static UniformBufferBlockElement getEmptyElement(int size) {
        return new UniformBufferBlockElement(size) {
            @Override
            void setUniformBlockData(int offset, UniformBufferBlock uniformBufferBlock) { }
        };
    }

}
