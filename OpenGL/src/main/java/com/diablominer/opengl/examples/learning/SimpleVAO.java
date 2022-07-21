package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class SimpleVAO extends VAO {

    private final int vertices;
    private final List<VertexBufferObject> attachedVertexBuffers = new ArrayList<>();

    public SimpleVAO(List<float[]> floatData, List<Integer> sizeData, Buffer.Usage usage) {
        super();
        this.vertices = floatData.get(0).length;

        bind();
        for (int i = 0; i < floatData.size(); i++) {
            createAttachedBuffer(floatData.get(i), usage, i, sizeData.get(i));
        }
        VertexBufferObject.unbind();
        enableVertexAttribPointers();
        unbind();
    }

    private void createAttachedBuffer(float[] floatData, Buffer.Usage usage, int index, int size) {
        FloatBuffer floatBuffer = BufferUtil.createBuffer(floatData);
        VertexBufferObject bufferObject = new VertexBufferObject();
        bufferObject.fill(floatBuffer, usage);

        GL33.glVertexAttribPointer(index, size, GL33.GL_FLOAT, false, size * Float.BYTES, 0);
        this.attachedVertexBuffers.add(bufferObject);

        BufferUtil.destroyBuffer(floatBuffer);
    }

    public void enableVertexAttribPointers() {
        for (int i = 0; i < attachedVertexBuffers.size(); i++) {
            GL33.glEnableVertexAttribArray(i);
        }
    }

    public void disableVertexAttribPointers() {
        for (int i = 0; i < attachedVertexBuffers.size(); i++) {
            GL33.glDisableVertexAttribArray(i);
        }
    }

    public void draw() {
        bind();

        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, vertices);

        SimpleVAO.unbind();
    }

    public void destroy() {
        bind();
        disableVertexAttribPointers();
        unbind();

        destroyVAO();
        for (VertexBufferObject bufferObject : attachedVertexBuffers) {
            bufferObject.destroy();
        }
    }

}
