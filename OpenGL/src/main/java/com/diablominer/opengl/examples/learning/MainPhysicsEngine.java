package com.diablominer.opengl.examples.learning;

public class MainPhysicsEngine extends PhysicsEngine {

    public MainPhysicsEngine(LCPSolverConfiguration solverConfig, double simulationTimeStep) {
        super(solverConfig, simulationTimeStep);
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
