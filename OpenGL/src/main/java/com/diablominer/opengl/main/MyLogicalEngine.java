package com.diablominer.opengl.main;

public class MyLogicalEngine extends LogicalEngine {

    public void update(double timeStep) {
        collisionTestAllPhysicsObjects();
        updateAllGameObjects(timeStep);
    }

    @Override
    public void predict(double timeStep) { predictAllGameObjects(timeStep); }

}
