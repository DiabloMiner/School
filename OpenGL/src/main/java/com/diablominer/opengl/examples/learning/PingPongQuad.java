package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;

public class PingPongQuad extends Model {

    private final PingPongQuadMesh quadMesh;

    public PingPongQuad(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex, RenderableManager renderableManager) {
        super(false);
        quadMesh = new PingPongQuadMesh(verticalTex, horizontalTex, inputTex);
        this.meshes.add(quadMesh);
        renderableManager.addRenderable(this);
    }

    public PingPongQuad(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex, RenderableManager renderableManager, boolean throwsShadow) {
        super(throwsShadow);
        quadMesh = new PingPongQuadMesh(verticalTex, horizontalTex, inputTex);
        this.meshes.add(quadMesh);
        renderableManager.addRenderable(this);
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        quadMesh.draw(shaderProgram);
    }

    @Override
    public void destroy() {
        destroyAllMeshes();
        destroyAllTextures();
    }

}
