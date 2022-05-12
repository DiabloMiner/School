package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;

public class PingPongQuad extends Model implements PingPongIterationObserver {

    private final PingPongQuadMesh quadMesh;
    private boolean firstIteration, horizontal;

    public PingPongQuad(Texture2D verticalTex, Texture2D horizontalTex, Texture2D inputTex) {
        super(new ArrayList<>(), new ArrayList<>());
        quadMesh = new PingPongQuadMesh(verticalTex, horizontalTex, inputTex);
        this.meshes.add(quadMesh);

        Learning6.getEventManager().addEventObserver(EventTypes.PingPongIterationEvent, this);
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        quadMesh.draw(shaderProgram, firstIteration, horizontal);
    }

    @Override
    public void update(Event event) {
        update((PingPongIterationEvent) event);
    }

    @Override
    public void update(PingPongIterationEvent event) {
        this.firstIteration = event.firstIteration;
        this.horizontal = event.horizontal;
    }
}
