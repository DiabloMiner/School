package com.diablominer.opengl.examples.learning;

import java.util.Collection;

public class Quad extends Model {

    private final QuadMesh quadMesh;

    public Quad(Collection<Texture2D> textures, RenderableManager renderableManager) {
        super(false);
        quadMesh = new QuadMesh(textures);
        this.meshes.add(quadMesh);
        renderableManager.addRenderable(this);
    }

    public Quad(Collection<Texture2D> textures, RenderableManager renderableManager, boolean throwsShadow) {
        super(throwsShadow);
        quadMesh = new QuadMesh(textures);
        this.meshes.add(quadMesh);
        renderableManager.addRenderable(this);
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        for (Mesh mesh : meshes) {
            mesh.draw(shaderProgram);
        }
    }

    @Override
    public void destroy() {
        destroyAllMeshes();
        destroyAllTextures();
    }

}
