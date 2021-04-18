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

    public void updateAllGameObjects(double timeStep) {
        for (GameObject gameObject : gameObjects) {
            gameObject.updateObjectState(timeStep);
        }
    }

    public void predictAllGameObjects(double timeStep) {
        for (GameObject gameObject : gameObjects) {
            gameObject.predictGameObjectState(timeStep);
        }
    }

    public abstract void update(double timeStep);

    public abstract void predict(double timeStep);

}
