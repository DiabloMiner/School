package com.diablominer.opengl.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class LogicalEngine {

    protected static List<GameObject> gameObjects;
    protected static Set<PhysicsObject> allPhysicsObjects;

    public LogicalEngine() {
        gameObjects = new ArrayList<>();
        allPhysicsObjects = new HashSet<>();
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    public List<GameObject> getGameObjects() {
        return gameObjects;
    }

    public void collisionTestAllPhysicsObjects() {
        for (PhysicsObject physicsObject : allPhysicsObjects) {
            Set<PhysicsObject> temporarySet = new HashSet<>(allPhysicsObjects);
            temporarySet.remove(physicsObject);
            physicsObject.collisionDetectionAndResponse(temporarySet);
        }
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
