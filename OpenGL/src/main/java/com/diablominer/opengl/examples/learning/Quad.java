package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.Collection;

public class Quad extends Model {

    private final QuadMesh quadMesh;

    public Quad(Collection<Texture2D> textures) {
        super(new ArrayList<>(), new ArrayList<>(), false);
        quadMesh = new QuadMesh(textures);
        this.meshes.add(quadMesh);
    }

    public Quad(Collection<Texture2D> textures, boolean throwsShadow) {
        super(new ArrayList<>(), new ArrayList<>(), throwsShadow);
        quadMesh = new QuadMesh(textures);
        this.meshes.add(quadMesh);
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        for (Mesh mesh : meshes) {
            mesh.draw(shaderProgram);
        }
    }

}
