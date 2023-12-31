package com.diablominer.opengl.examples.learning;

import java.util.List;

public class MainPhysicsEngine extends PhysicsEngine {

    public MainPhysicsEngine(double simulationTimeStep) {
        super(simulationTimeStep);
    }

    public MainPhysicsEngine(List<Entity> entities, double simulationTimeStep) {
        super(entities, simulationTimeStep);
    }

    @Override
    public void update() {
        updateTimeStep();
        timeStep(simulationTimeStep);
        /*checkForCollisions(simulationTimeStep);
        performTimeStep(simulationTimeStep);*/
    }

    @Override
    public void destroy() {}

}
