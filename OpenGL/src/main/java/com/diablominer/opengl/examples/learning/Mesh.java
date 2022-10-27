package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public abstract class Mesh implements Renderable {

    protected List<float[]> floatData;
    protected List<Integer> vertexAttributeSizes;
    protected List<Texture2D> texture2DS;
    protected VAO vao;

    public Mesh() {
        floatData = new ArrayList<>();
        texture2DS = new ArrayList<>();
        vertexAttributeSizes = new ArrayList<>();
    }

    public Mesh(List<float[]> floatData, List<Integer> vertexAttributeSizes, List<Texture2D> texture2DS) {
        this.floatData = floatData;
        this.texture2DS = texture2DS;
        this.vertexAttributeSizes = vertexAttributeSizes;
    }

    abstract void setUpMesh();

}
