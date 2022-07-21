package com.diablominer.opengl.examples.learning;

public abstract class IOEngine implements SubEngine {

    public abstract void processInputs(double deltaTime);

    public abstract void resize(Window window);

}
