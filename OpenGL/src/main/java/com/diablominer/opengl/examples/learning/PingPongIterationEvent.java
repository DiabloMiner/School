package com.diablominer.opengl.examples.learning;

public class PingPongIterationEvent implements Event {

    public boolean firstIteration, horizontal;

    public PingPongIterationEvent(boolean firstIteration, boolean horizontal) {
        this.firstIteration = firstIteration;
        this.horizontal = horizontal;
    }

}
