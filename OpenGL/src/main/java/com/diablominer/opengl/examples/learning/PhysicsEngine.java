package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public abstract class PhysicsEngine implements SubEngine {

    public List<PhysicsObject> physicsObjects;

    public PhysicsEngine() {
        physicsObjects = new ArrayList<>();
    }

    abstract void update(double timeStep);

    public void performTimeStep(double timeStep) {
        for (PhysicsObject physicsObject : physicsObjects) {
            physicsObject.performTimeStep(timeStep);
        }
    }

    public void checkForCollisions(double timeStep) {
        for (PhysicsObject object1 : physicsObjects) {
            List<PhysicsObject> toBeSearched = new ArrayList<>(physicsObjects);
            toBeSearched.remove(object1);

            for (PhysicsObject object2 : toBeSearched) {
                if (object1.isColliding(object2)) {
                    object1.handleCollisions(object2, timeStep);
                }
            }
        }
    }

    public void predictTimeStep(double timeStep) {
        for (PhysicsObject physicsObject : physicsObjects) {
            physicsObject.predictTimeStep(timeStep);
        }
    }

}
