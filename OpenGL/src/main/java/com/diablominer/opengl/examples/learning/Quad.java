package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.Collection;

public class Quad extends Model {

    private QuadMesh quadMesh;

    public Quad(Collection<Texture2D> textures) {
        super(new ArrayList<>(), new ArrayList<>());
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
