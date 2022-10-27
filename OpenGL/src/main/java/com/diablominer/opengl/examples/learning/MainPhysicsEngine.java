package com.diablominer.opengl.examples.learning;

import java.util.List;

public class MainPhysicsEngine extends PhysicsEngine {

    public MainPhysicsEngine(LCPSolverConfiguration solverConfig, double simulationTimeStep) {
        super(solverConfig, simulationTimeStep);
    }

    public MainPhysicsEngine(LCPSolverConfiguration solverConfig, List<Entity> entities, double simulationTimeStep) {
        super(solverConfig, entities, simulationTimeStep);
    }

    @Override
    public void update() {
        updateTimeStep();
        checkForCollisions(simulationTimeStep);
        performTimeStep(simulationTimeStep);
    }

    @Override
    public void destroy() {}

}
