package com.diablominer.opengl.main;

public abstract class Game {

    public abstract void init() throws Exception;

    public abstract void mainLoop();

    public abstract void cleanUp();

}
