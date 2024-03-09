package com.diablominer.opengl.examples.learning;

import java.util.List;

public class MainPhysicsEngine extends PhysicsEngine {

    /**
     *
     * @param simulationTimeStep - Simulation timestep
     * @param cfm - Constraint force mixing term (0 < cfm < 1): determines how much constraint force is mixed into the kinematic constraint in each timestep
     * @param erp - Error reduction parameter (0 < erp < 1): determines how much constraint error is reduced in each timestep
     * @param tol - Tolerance parameter: all contacts with a relative velocity below this threshold are automatically treated as perfectly inelastic
     */
    public MainPhysicsEngine(double simulationTimeStep, double cfm, double erp, double tol) {
        super(simulationTimeStep, cfm, erp, tol);
    }

    /**
     *
     * @param entities - All entities simulated by this physics-engine
     * @param simulationTimeStep - Simulation timestep
     * @param cfm - Constraint force mixing term (0 < cfm < 1): determines how much constraint force is mixed into the kinematic constraint in each timestep
     * @param erp - Error reduction parameter (0 < erp < 1): determines how much constraint error is reduced in each timestep
     * @param tol - Tolerance parameter: all contacts with a relative velocity below this threshold are automatically treated as perfectly inelastic
     */
    public MainPhysicsEngine(List<Entity> entities, double simulationTimeStep, double cfm, double erp, double tol) {
        super(entities, simulationTimeStep, cfm , erp, tol);
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
