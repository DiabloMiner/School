package com.diablominer.opengl.examples.learning;

public class MainPhysicsEngine extends PhysicsEngine {

    @Override
    public void update(double timeStep) {
        performTimeStep(timeStep);
        checkForCollisions(timeStep);
    }

    @Override
    public void destroy() {}

}
