package com.diablominer.opengl.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class LogicalEngine {

    protected static List<GameObject> gameObjects;
    protected static List<PhysicsObject> allPhysicsObjects;
    // TODO: Remove static modifier and make it dynamic and make it list again
    protected static List<PhysicsObject> alreadyCollidedPhysicsObjects = new ArrayList<>();

    public LogicalEngine() {
        gameObjects = new ArrayList<>();
        allPhysicsObjects = new ArrayList<>();
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
            temporarySet.removeAll(alreadyCollidedPhysicsObjects);
            physicsObject.collide(temporarySet);
        }
    }

    public void updateAllGameObjects(double timeStep) {
        alreadyCollidedPhysicsObjects.clear();
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

    public static void addAlreadyCollidedPhysicsObject(PhysicsObject physicsObject) {
        alreadyCollidedPhysicsObjects.add(physicsObject);
    }

}
