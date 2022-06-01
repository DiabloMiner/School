package com.diablominer.opengl.examples.learning;

public enum UniformBufferBlockType {

    Float(java.lang.Float.BYTES),
    Integer(java.lang.Integer.BYTES),
    Double(java.lang.Double.BYTES),
    Vector4F(Float.size * 4),
    Matrix4F(Vector4F.size * 4);

    public final int size;

    UniformBufferBlockType(int size) {
        this.size = size;
    }

}
