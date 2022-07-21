package com.diablominer.opengl.examples.learning;


import java.util.Map;

public class PingPongQuad extends Model {

    private final PingPongQuadMesh quadMesh;

    public PingPongQuad(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex, boolean horizontalAtStart) {
        super();
        quadMesh = new PingPongQuadMesh(verticalTex, horizontalTex, inputTex, horizontalAtStart);
        this.meshes.add(quadMesh);
    }

    @Override
    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        quadMesh.draw(shaderProgram, flags);
    }

    public void setFirstIteration(boolean firstIteration) {
        quadMesh.setFirstIteration(firstIteration);
    }

    @Override
    public void destroy() {
        destroyAllMeshes();
    }

}
