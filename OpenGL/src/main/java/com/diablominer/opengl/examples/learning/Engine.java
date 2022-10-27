package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public abstract class Engine {

    protected List<Entity> entities;

    public Engine() {
        entities = new ArrayList<>();
    }

    public abstract void init() throws Exception;

    public abstract void mainLoop();

    public abstract void close();

}
