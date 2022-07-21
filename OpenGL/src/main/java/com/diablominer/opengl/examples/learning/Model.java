package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class Model extends Renderable {

    protected List<Mesh> meshes;

    public Model() {
        super();
        this.meshes = new ArrayList<>();
    }

    public Model(Vector3f position) {
        super(position);
        this.meshes = new ArrayList<>();
    }

    public Model(Matrix4f model) {
        super(model);
        this.meshes = new ArrayList<>();
    }

    public Model(List<Mesh> meshes, List<Texture2D> texture2Ds) {
        super();
        this.meshes = meshes;
    }

    public void destroyAllMeshes() {
        for (Mesh mesh : meshes) {
            mesh.destroy();
        }
        meshes.clear();
    }

}
