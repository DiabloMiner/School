package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;

import java.util.*;

public abstract class PhysicsEngine implements SubEngine {

    public static final int roundingDigit = 15;

    public List<PhysicsObject> physicsObjects;
    public LCPSolverConfiguration solverConfig;

    public PhysicsEngine(LCPSolverConfiguration solverConfig) {
        physicsObjects = new ArrayList<>();
        this.solverConfig = solverConfig;
    }

    abstract void update(double timeStep);

    public void performTimeStep(double timeStep) {
        for (PhysicsObject physicsObject : physicsObjects) {
            physicsObject.performTimeStep(timeStep);
        }
    }

    public void performTimeStepForOtherObjects(List<PhysicsObject> alreadyTimeStepped, double timeStep) {
        List<PhysicsObject> physicsObjects = new ArrayList<>(this.physicsObjects);
        physicsObjects.removeAll(alreadyTimeStepped);
        for (PhysicsObject physicsObject : physicsObjects) {
            physicsObject.performTimeStep(timeStep);
        }
    }

    private List<PhysicsObject> physicsObjectsFromCollisions(List<Collision> collisons) {
        List<PhysicsObject> physicsObjects = new ArrayList<>();
        collisons.forEach(collision -> {
            physicsObjects.add(collision.A);
            physicsObjects.add(collision.B);
        });
        return physicsObjects;
    }

    public void checkForCollisions(double timeStep) {
        Set<Collision> collisions = new HashSet<>();
        Map<PhysicsObject, PhysicsObject> detectedCollisions = new HashMap<>();
        for (PhysicsObject object1 : physicsObjects) {
            List<PhysicsObject> toBeSearched = new ArrayList<>(physicsObjects);
            toBeSearched.remove(object1);

            for (PhysicsObject object2 : toBeSearched) {
                if (!(detectedCollisions.get(object1) == object2) && object1.isColliding(object2)) {
                    collisions.addAll(Arrays.asList(object1.getCollisions(object2, timeStep)));
                    detectedCollisions.put(object1, object2);
                    detectedCollisions.put(object2, object1);
                }
            }
        }

        List<Collision> sortedCollisions = new ArrayList<>(collisions);
        sortedCollisions.sort((c1, c2) -> Double.compare(c2.timeStepTaken, c1.timeStepTaken));
        List<Collision> subList = new ArrayList<>();
        for (Collision collision : sortedCollisions) {
            if (collision.isColliding()) {
                if (!subList.isEmpty() && subList.get(subList.size() - 1).timeStepTaken != collision.timeStepTaken) {
                    // TODO: Why would objects later in time be updated to this timeStep?
                    performTimeStepForOtherObjects(physicsObjectsFromCollisions(subList), subList.get(subList.size() - 1).timeStepTaken);
                    processCollisionSubList(subList, 0.0);
                    subList.clear();
                }
                subList.add(collision);
            }
        }
        if (subList.size() > 0) {
            performTimeStepForOtherObjects(physicsObjectsFromCollisions(subList), subList.get(subList.size() - 1).timeStepTaken);
            processCollisionSubList(subList, 0.0);
            subList.clear();
        }
        if (sortedCollisions.size() > 0) {
            performTimeStep(timeStep - sortedCollisions.get(sortedCollisions.size() - 1).timeStepTaken);
        }
    }

    private void processCollisionSubList(List<Collision> subList, double timeStep) {
        DoubleMatrix[] matrices = createMatrices(subList, timeStep);
        int size = matrices[0].rows / subList.size();
        for (int i = 0; i < subList.size(); i++) {
            DoubleMatrix APrime = matrices[0].get(Transforms.createIndexArray(i * size, size), Transforms.createIndexArray(i * size, size));
            DoubleMatrix bPrime = matrices[1].get(Transforms.createIndexArray(i * size, size), new int[] {0});
            DoubleMatrix x = solveLCP(APrime, bPrime, subList.get(i)).x;
            subList.get(i).applyImpulse(x.get(0), new double[] {x.get(1), x.get(2)});
        }
    }

    private DoubleMatrix[] createMatrices(List<Collision> subList, double timeStep) {
        if (solverConfig.solverChoice.forBLCP) {
            return LCPSolver.constructBLCPMatrices(subList.toArray(new Collision[0]), timeStep);
        } else {
            subList.forEach(Collision::generateAdditionalTangentialDirections);
            return LCPSolver.constructLCPMatrices(subList.toArray(new Collision[0]), timeStep);
        }
    }

    private LCPSolverResult solveLCP(DoubleMatrix APrime, DoubleMatrix bPrime, Collision currentCollision) {
        if (solverConfig.solverChoice.equals(LCPSolverChoice.LCP_MIN_MAP_NEWTON)) {
            MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
            return LCPSolver.getInstance().solveLCPWithMinimumMapNewton(APrime, bPrime, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
        } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_MIN_MAP_NEWTON)) {
            MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
            return LCPSolver.getInstance().solveBLCPWithMinimumMapNewton(APrime, bPrime, currentCollision.getFrictionCoefficient(), config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
        } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_NNCP)) {
            NNCGConfiguration config = (NNCGConfiguration) solverConfig;
            return LCPSolver.getInstance().solveBLCPWithNNCG(APrime, bPrime, currentCollision.getFrictionCoefficient(), config.iterations, roundingDigit);
        } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_PSOR)) {
            PSORConfiguration config = (PSORConfiguration) solverConfig;
            return LCPSolver.getInstance().solveBLCPWithPSOR(APrime, bPrime, currentCollision.getFrictionCoefficient(), config.omega, config.epsilonRelative, config.iterations, roundingDigit);
        } else  {
            if (solverConfig.solverChoice.forBLCP) {
                PGSConfiguration config = (PGSConfiguration) solverConfig;
                return LCPSolver.getInstance().solveBLCPWithPGS(APrime, bPrime, currentCollision.getFrictionCoefficient(), config.epsilonRelative, config.iterations, roundingDigit);
            } else {
                MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
                return LCPSolver.getInstance().solveLCPWithMinimumMapNewton(APrime, bPrime, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
            }
        }
    }

    public void predictTimeStep(double timeStep) {
        for (PhysicsObject physicsObject : physicsObjects) {
            physicsObject.predictTimeStep(timeStep);
        }
    }

}
