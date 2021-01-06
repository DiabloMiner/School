package com.diablominer.opengl.main;

import java.util.ArrayList;
import java.util.List;

public abstract class LogicalEngine {

    private List<GameObject> gameObjects;

    public LogicalEngine() {
        gameObjects = new ArrayList<>();
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void updateAllGameObjects() {
        for (GameObject gameObject : gameObjects) {
            gameObject.updateObjectState();
        }
    }

}
