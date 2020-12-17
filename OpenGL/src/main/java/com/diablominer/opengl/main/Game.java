package com.diablominer.opengl.main;

import java.util.ArrayList;
import java.util.List;

public abstract class Game {

    protected List<GameObject> objects = new ArrayList<>();

    public void addGameObject(GameObject object) {
        objects.add(object);
    }

    public abstract void init() throws Exception;

    public abstract void mainLoop();

    public abstract void cleanUp();

}
