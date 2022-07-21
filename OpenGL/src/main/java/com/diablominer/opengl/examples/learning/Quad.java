package com.diablominer.opengl.examples.learning;

import java.util.Collection;
import java.util.Map;

public class Quad extends Model {

    private final QuadMesh quadMesh;

    public Quad(Collection<Texture2D> textures) {
        super();
        quadMesh = new QuadMesh(textures);
        this.meshes.add(quadMesh);
    }

    @Override
    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        for (Mesh mesh : meshes) {
            mesh.draw(shaderProgram, flags);
        }
    }

    @Override
    public void destroy() {
        destroyAllMeshes();
    }

}
