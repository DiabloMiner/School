package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class UniformBufferBlock extends Buffer {

    public static int bindingTarget = GL33.GL_UNIFORM_BUFFER;
    public static List<UniformBufferBlock> allUniformBufferBlocks = new ArrayList<>();

    public int bindingPoint, size;
    public String name;

    public UniformBufferBlock(int usage, int size, String name) {
        super();
        this.size = size;
        this.name = name;
        defineStorage(usage);
        bindingPoint = allUniformBufferBlocks.size();
        allUniformBufferBlocks.add(this);
    }

    @Override
    public void bind() {
        GL33.glBindBuffer(bindingTarget, id);
    }

    public void bindToUniformBlockBinding() {
        bind();
        GL33.glBindBufferBase(bindingTarget, bindingPoint, id);
    }

    public void bindToUniformBlockBinding(int offset) {
        bind();
        GL33.glBindBufferRange(bindingTarget, bindingPoint, id, offset, size);
    }

    public void defineStorage(int usage) {
        bind();
        GL33.glBufferData(bindingTarget, size, usage);
        UniformBufferBlock.unbind();
    }

    public void setUniformBlockBinding(ShaderProgram shaderProgram) {
        shaderProgram.setUniformBlockBinding(this);
        bindToUniformBlockBinding();
    }

    public void setUniformBlockData(int offset, float[] data) {
        bind();
        GL33.glBufferSubData(bindingTarget, offset, data);
        unbind();
    }

    public void setUniformBlockData(int offset, FloatBuffer data) {
        bind();
        GL33.glBufferSubData(bindingTarget, offset, data);
        unbind();
    }

    @Override
    void destroy() {
        destroyBuffer();
    }

    public static void unbind() {
        GL33.glBindBuffer(bindingTarget, 0);
    }
}
