package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;

public class PingPongQuad extends Model {

    private final PingPongQuadMesh quadMesh;
    private boolean firstIteration, horizontal;

    public PingPongQuad(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex) {
        super(new ArrayList<>(), new ArrayList<>());
        quadMesh = new PingPongQuadMesh(verticalTex, horizontalTex, inputTex);
        this.meshes.add(quadMesh);
    }

    public void update(boolean firstIteration, boolean horizontal) {
        this.firstIteration = firstIteration;
        this.horizontal = horizontal;
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        quadMesh.draw(shaderProgram, firstIteration, horizontal);
    }

}
