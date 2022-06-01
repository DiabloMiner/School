package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;

public class PingPongQuad extends Model implements PingPongIterationObserver {

    private final PingPongQuadMesh quadMesh;
    private boolean firstIteration, horizontal;

    public PingPongQuad(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex) {
        super(new ArrayList<>(), new ArrayList<>(), false);
        quadMesh = new PingPongQuadMesh(verticalTex, horizontalTex, inputTex);
        this.meshes.add(quadMesh);

        Learning6.engineInstance.getEventManager().addEventObserver(EventTypes.PingPongIterationEvent, this);
    }

    public PingPongQuad(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex, boolean throwsShadow) {
        super(new ArrayList<>(), new ArrayList<>(), throwsShadow);
        quadMesh = new PingPongQuadMesh(verticalTex, horizontalTex, inputTex);
        this.meshes.add(quadMesh);

        Learning6.engineInstance.getEventManager().addEventObserver(EventTypes.PingPongIterationEvent, this);
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        quadMesh.draw(shaderProgram, firstIteration, horizontal);
    }

    @Override
    public void update(Event event) {}

    @Override
    public void update(PingPongIterationEvent event) {
        this.firstIteration = event.firstIteration;
        this.horizontal = event.horizontal;
    }
}
