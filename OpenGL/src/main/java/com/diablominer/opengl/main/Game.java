package com.diablominer.opengl.main;

public interface Game {

    void init() throws Exception;

    void mainLoop() throws Exception;

    void cleanUp();

}
