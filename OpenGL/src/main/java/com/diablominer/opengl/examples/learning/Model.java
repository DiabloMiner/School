package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class Model extends Renderable {

    protected List<Mesh> meshes;
    protected List<Texture2D> loadedTexture2DS;

    public Model() {
        super();
        this.meshes = new ArrayList<>();
        this.loadedTexture2DS = new ArrayList<>();
    }

    public Model(Vector3f position) {
        super(position);
        this.meshes = new ArrayList<>();
        this.loadedTexture2DS = new ArrayList<>();
    }

    public Model(Matrix4f model) {
        super(model);
        this.meshes = new ArrayList<>();
        this.loadedTexture2DS = new ArrayList<>();
    }

    public Model(boolean throwsShadows) {
        super(throwsShadows);
        this.meshes = new ArrayList<>();
        this.loadedTexture2DS = new ArrayList<>();
    }

    public Model(Vector3f position, boolean throwsShadows) {
        super(position, throwsShadows);
        this.meshes = new ArrayList<>();
        this.loadedTexture2DS = new ArrayList<>();
    }

    public Model(Matrix4f model, boolean throwsShadows) {
        super(model, throwsShadows);
        this.meshes = new ArrayList<>();
        this.loadedTexture2DS = new ArrayList<>();
    }

    public Model(List<Mesh> meshes, List<Texture2D> loadedTexture2DS, boolean throwsShadows) {
        super(throwsShadows);
        this.meshes = meshes;
        this.loadedTexture2DS = loadedTexture2DS;
    }

    public void destroyAllMeshes() {
        for (Mesh mesh : meshes) {
            mesh.destroy();
        }
        meshes.clear();
    }

    public void destroyAllTextures() {
        for (Texture2D texture : loadedTexture2DS) {
            texture.destroy();
        }
        loadedTexture2DS.clear();
    }

}
