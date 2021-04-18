package com.diablominer.opengl.main;

import java.util.ArrayList;
import java.util.List;

public abstract class LogicalEngine {

    protected List<GameObject> gameObjects;

    public LogicalEngine() {
        gameObjects = new ArrayList<>();
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void updateAllGameObjects(double time) {
        for (GameObject gameObject : gameObjects) {
            gameObject.updateObjectState(time);
        }
    }

    public abstract void update(double time);

}
