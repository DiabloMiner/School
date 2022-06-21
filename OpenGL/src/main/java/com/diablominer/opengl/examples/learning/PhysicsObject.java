package com.diablominer.opengl.examples.learning;

public interface PhysicsObject {

    void performTimeStep(double timeStep);

    boolean isColliding(PhysicsObject physicsObject);

    void handleCollisions(PhysicsObject physicsObject, double timeStep);

    void predictTimeStep(double timeStep);

}
