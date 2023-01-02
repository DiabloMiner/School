package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;
import org.joml.Math;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PhysicsEngine implements SubEngine {

    public static final int roundingDigit = 15;

    public LCPSolverConfiguration solverConfig;
    public double simulationTimeStep;
    protected List<Entity> entities;
    protected double leftOverTime;

    public PhysicsEngine(LCPSolverConfiguration solverConfig, double simulationTimeStep) {
        this.entities = new ArrayList<>();
        this.solverConfig = solverConfig;
        this.simulationTimeStep = Math.min(Math.max(simulationTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
        this.leftOverTime = 0.0;
    }

    public PhysicsEngine(LCPSolverConfiguration solverConfig, List<Entity> entities, double simulationTimeStep) {
        this.entities = new ArrayList<>(entities);
        this.solverConfig = solverConfig;
        this.simulationTimeStep = Math.min(Math.max(simulationTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
        this.leftOverTime = 0.0;
    }

    public void performTimeStep(double timeStep) {
        for (Entity entity : entities) {
            entity.getPhysicsComponent().performTimeStep(timeStep);
        }
    }

    public void checkForCollisions(double timeStep) {
        Set<Collision> collisions = findCollisions(timeStep);
        if (collisions.size() > 0) { resolveCollisions(collisions, timeStep); }
    }

    protected Set<Collision> findCollisions(double timeStep) {
        Set<Collision> collisions = new HashSet<>();
        Map<Integer, Boolean> alreadySearched = new HashMap<>();
        for (Entity object1 : entities) {
            List<Entity> toBeSearched = new ArrayList<>(entities);
            toBeSearched.remove(object1);

            for (Entity object2 : toBeSearched) {
                if (object1.getPhysicsComponent().objectType.performTimeStep || object2.getPhysicsComponent().objectType.performTimeStep) {
                    if (!alreadySearched.containsKey(indexKey(object1, object2, entities)) && object1.getPhysicsComponent().willCollide(object2.getPhysicsComponent(), timeStep)) {
                        collisions.addAll(Arrays.asList(object1.getPhysicsComponent().getCollisions(object2.getPhysicsComponent(), timeStep)));
                    }
                    // TODO: Remove
                    object1.getPhysicsComponent().willCollide(object2.getPhysicsComponent(), timeStep);
                    alreadySearched.putIfAbsent(indexKey(object1, object2, entities), true);
                }
            }
        }
        // TODO: Remove
        if (collisions.size() == 1) {
            System.out.print("");
            entities.get(1).getPhysicsComponent().willCollide(entities.get(2).getPhysicsComponent(), 0.01);
        }
        return collisions;
    }

    protected int indexKey(Entity object1, Entity object2, List<Entity> entities) {
        return entities.indexOf(object1) + entities.indexOf(object2);
    }

    protected void updateEarlierObjects(List<Collision> sortedCollisions, double timeStep, int endingIndex) {
        for (int i = 0; i < endingIndex; i++) {
            double h = timeStep - sortedCollisions.get(i).timeStepTaken;
            sortedCollisions.get(i).A.performTimeStep(h);
            sortedCollisions.get(i).B.performTimeStep(h);
        }
        double h = (endingIndex != 0) ? (timeStep - sortedCollisions.get(endingIndex - 1).timeStepTaken) : (timeStep);
        List<PhysicsComponent> nonCollidingObjects = this.entities.stream().map(Entity::getPhysicsComponent).collect(Collectors.toList());
        nonCollidingObjects.removeAll(physicsObjectsFromCollisions(sortedCollisions));
        nonCollidingObjects.forEach(object -> object.performTimeStep(h));
    }

    protected void resolveCollisions(Set<Collision> collisions, double timeStep) {
        List<Collision> sortedCollisions = new ArrayList<>(collisions);
        sortedCollisions.sort(Comparator.comparingDouble(c -> c.timeStepTaken));

        List<Collision> subList = new ArrayList<>();
        for (int i = 0; i < sortedCollisions.size(); i++) {
            Collision collision = sortedCollisions.get(i);
            if (collision.isColliding(timeStep - collision.timeStepTaken)) {
                if (!subList.isEmpty() && subList.get(subList.size() - 1).timeStepTaken != collision.timeStepTaken) {
                    updateEarlierObjects(sortedCollisions, subList.get(0).timeStepTaken, i - subList.size());
                    processCollisionSubList(subList, timeStep - subList.get(subList.size() - 1).timeStepTaken);
                    subList.clear();
                }
                subList.add(collision);
            }
        }

        if (subList.size() > 0) {
            updateEarlierObjects(sortedCollisions, subList.get(0).timeStepTaken, sortedCollisions.size() - subList.size());
            processCollisionSubList(subList, timeStep - subList.get(subList.size() - 1).timeStepTaken);
            subList.clear();
        }
        checkForFurtherCollisions(physicsObjectsFromCollisions(sortedCollisions), timeStep - sortedCollisions.get(sortedCollisions.size() - 1).timeStepTaken);
        sortedCollisions.forEach(collision -> {
            collision.A.alreadyTimeStepped = true;
            collision.B.alreadyTimeStepped = true;
        });
    }

    protected void processCollisionSubList(List<Collision> subList, double timeStep) {
        DoubleMatrix[] matrices = createMatrices(subList, timeStep);
        int size = matrices[0].rows / subList.size();
        for (int i = 0; i < subList.size(); i++) {
            DoubleMatrix APrime = matrices[0].get(Transforms.createIndexArray(i * size, size), Transforms.createIndexArray(i * size, size));
            DoubleMatrix bPrime = matrices[1].get(Transforms.createIndexArray(i * size, size), new int[] {0});
            DoubleMatrix x = solveLCP(APrime, bPrime, subList.get(i)).x;
            // Add rolling friction coefficients to collision solving
            subList.get(i).applyImpulse(x.get(0), new double[] {x.get(1), x.get(2)}, new double[] {x.get(4), x.get(5)}, roundingDigit);
            LCPSolver.addSolvedCollision(subList.get(i), x);
        }
    }

    protected DoubleMatrix[] createMatrices(List<Collision> subList, double timeStep) {
        if (solverConfig.solverChoice.forBLCP) {
            return LCPSolver.constructBLCPMatrices(subList.toArray(new Collision[0]), timeStep);
        } else {
            subList.forEach(Collision::generateAdditionalTangentialDirections);
            return LCPSolver.constructLCPMatrices(subList.toArray(new Collision[0]), timeStep);
        }
    }

    protected LCPSolverResult solveLCP(DoubleMatrix APrime, DoubleMatrix bPrime, Collision currentCollision) {
        if (LCPSolver.solvedCollisions.get(currentCollision.hashCode()) != null) {
            DoubleMatrix x0 = LCPSolver.solvedCollisions.get(currentCollision.hashCode());
            if (solverConfig.solverChoice.equals(LCPSolverChoice.LCP_MIN_MAP_NEWTON)) {
                MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
                return LCPSolver.getInstance().solveLCPWithMinimumMapNewton(x0, APrime, bPrime, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
            } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_MIN_MAP_NEWTON)) {
                MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
                return LCPSolver.getInstance().solveBLCPWithMinimumMapNewton(x0, APrime, bPrime, currentCollision.getFrictionCoefficient(), currentCollision.coefficientOfRollingFriction, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
            } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_NNCP)) {
                NNCGConfiguration config = (NNCGConfiguration) solverConfig;
                return LCPSolver.getInstance().solveBLCPWithNNCG(x0, APrime, bPrime, currentCollision.getFrictionCoefficient(), currentCollision.coefficientOfRollingFriction, config.iterations, roundingDigit);
            } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_PSOR)) {
                PSORConfiguration config = (PSORConfiguration) solverConfig;
                return LCPSolver.getInstance().solveBLCPWithPSOR(x0, APrime, bPrime, currentCollision.getFrictionCoefficient(), currentCollision.coefficientOfRollingFriction, config.omega, config.epsilonRelative, config.iterations, roundingDigit);
            } else  {
                if (solverConfig.solverChoice.forBLCP) {
                    PGSConfiguration config = (PGSConfiguration) solverConfig;
                    return LCPSolver.getInstance().solveBLCPWithPGS(x0, APrime, bPrime, currentCollision.getFrictionCoefficient(), config.epsilonRelative, config.iterations, roundingDigit);
                } else {
                    MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
                    return LCPSolver.getInstance().solveLCPWithMinimumMapNewton(x0, APrime, bPrime, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
                }
            }
        } else {
            if (solverConfig.solverChoice.equals(LCPSolverChoice.LCP_MIN_MAP_NEWTON)) {
                MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
                return LCPSolver.getInstance().solveLCPWithMinimumMapNewton(APrime, bPrime, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
            } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_MIN_MAP_NEWTON)) {
                MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
                return LCPSolver.getInstance().solveBLCPWithMinimumMapNewton(APrime, bPrime, currentCollision.getFrictionCoefficient(), currentCollision.coefficientOfRollingFriction, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
            } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_NNCP)) {
                NNCGConfiguration config = (NNCGConfiguration) solverConfig;
                return LCPSolver.getInstance().solveBLCPWithNNCG(APrime, bPrime, currentCollision.getFrictionCoefficient(), currentCollision.coefficientOfRollingFriction, config.iterations, roundingDigit);
            } else if (solverConfig.solverChoice.equals(LCPSolverChoice.BLCP_PSOR)) {
                PSORConfiguration config = (PSORConfiguration) solverConfig;
                return LCPSolver.getInstance().solveBLCPWithPSOR(APrime, bPrime, currentCollision.getFrictionCoefficient(), currentCollision.coefficientOfRollingFriction, config.omega, config.epsilonRelative, config.iterations, roundingDigit);
            } else  {
                if (solverConfig.solverChoice.forBLCP) {
                    PGSConfiguration config = (PGSConfiguration) solverConfig;
                    return LCPSolver.getInstance().solveBLCPWithPGS(APrime, bPrime, currentCollision.getFrictionCoefficient(), currentCollision.coefficientOfRollingFriction, config.epsilonRelative, config.iterations, roundingDigit);
                } else {
                    MinMapNewtonConfiguration config = (MinMapNewtonConfiguration) solverConfig;
                    return LCPSolver.getInstance().solveLCPWithMinimumMapNewton(APrime, bPrime, config.alpha, config.beta, config.delta, config.epsilonAbsolute, config.epsilonRelative, config.iterations, config.lineSearchIterations, roundingDigit);
                }
            }
        }
    }

    protected void checkForFurtherCollisions(Collection<PhysicsComponent> physicsComponents, double timeStep) {
        Set<Collision> collisions = findCollisions(timeStep);
        if (collisions.size() > 0) {
            leftOverTime = this.simulationTimeStep;
            double newTimeStep = collisions.toArray(new Collision[0])[collisions.size() - 1].timeStepTaken;
            newTimeStep = Transforms.round(newTimeStep, (int) Math.round(java.lang.Math.log10(1 / newTimeStep)));
            resolveCollisions(collisions, timeStep);
            setSimulationTimeStep(newTimeStep);
        } else {
            setUseRK2(physicsComponents, false);
            performTimeStep(timeStep);
        }
    }

    protected void setUseRK2(Collection<PhysicsComponent> physicsComponents, boolean useRK2) {
        for (PhysicsComponent physicsComponent : physicsComponents) {
            if (physicsComponent instanceof StandardPhysicsComponent) {
                ((StandardPhysicsComponent) physicsComponent).useRK2 = useRK2;
            }
        }
    }

    protected void setSimulationTimeStep(double newTimeStep) {
        simulationTimeStep = Math.min(Math.max(newTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
    }

    protected void updateTimeStep() {
        if (simulationTimeStep < Learning6.maxTimeStep || simulationTimeStep == Learning6.minTimeStep) {
            simulationTimeStep = Learning6.maxTimeStep;
        }
    }

    public void predictTimeStep(double timeStep) {
        for (Entity entity : entities) {
            entity.getTransformComponent().update(entity.getPhysicsComponent().predictTimeStep(timeStep));
        }
    }

    public double getLeftOverTime() {
        double temp = leftOverTime;
        leftOverTime = 0.0;
        return temp;
    }

    abstract void update();

    public void updateEntities() {
        for (Entity entity : entities) {
            entity.getTransformComponent().update(entity.getPhysicsComponent().worldMatrix);
        }
    }

    public static List<PhysicsComponent> physicsObjectsFromCollisions(List<Collision> collisions) {
        List<PhysicsComponent> physicsComponents = new ArrayList<>();
        collisions.forEach(collision -> {
            physicsComponents.add(collision.A);
            physicsComponents.add(collision.B);
        });
        return physicsComponents;
    }

}
