package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;
import org.joml.Math;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PhysicsEngine implements SubEngine {

    public static double epsilon = 1e-15;
    public static double collisionTimeEpsilon = 10e-50;
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
            entity.getPhysicsComponent().performTimeStep(timeStep, roundingDigit);
        }
    }

    public void performExplicitEulerTimeStep(double timeStep) {
        for (Entity entity : entities) {
            entity.getPhysicsComponent().performExplicitEulerTimeStep(timeStep, roundingDigit);
        }
    }

    public void performTimeStep(double timeStep, List<PhysicsComponent> physicsComponents) {
        for (PhysicsComponent physicsComponent : physicsComponents) {
            physicsComponent.performTimeStep(timeStep, roundingDigit);
        }
    }

    public void performExplicitEulerTimeStep(double timeStep, List<PhysicsComponent> physicsComponents) {
        for (PhysicsComponent physicsComponent : physicsComponents) {
            physicsComponent.performExplicitEulerTimeStep(timeStep, roundingDigit);
        }
    }

    public void checkForCollisions(double timeStep) {
        Set<Collision> collisions = findCollisions(new SolutionParameters(0.0, timeStep, -1.0), timeStep);
        if (collisions.size() > 0) { resolveCollisions(collisions, timeStep); }
    }

    protected Set<Collision> findCollisions(SolutionParameters parameters, double timeStep) {
        Set<Collision> collisions = new HashSet<>();
        Map<Integer, Boolean> alreadySearched = new HashMap<>();
        for (Entity object1 : entities) {
            List<Entity> toBeSearched = new ArrayList<>(entities);
            toBeSearched.remove(object1);

            for (Entity object2 : toBeSearched) {
                if (object1.getPhysicsComponent().objectType.performTimeStep || object2.getPhysicsComponent().objectType.performTimeStep) {
                    if (!alreadySearched.containsKey(indexKey(object1, object2, entities))) {
                        Optional<Collision> collisionOptional = object1.getPhysicsComponent().getInitialCollision(object2.getPhysicsComponent(), parameters, timeStep, roundingDigit);
                        collisionOptional.ifPresent(collisions::add);
                    }
                    alreadySearched.putIfAbsent(indexKey(object1, object2, entities), true);
                }
            }
        }
        // TODO: Remove
        if (collisions.size() == 5) {
            System.out.print("");
        }
        return collisions;
    }

    protected int indexKey(Entity object1, Entity object2, List<Entity> entities) {
        return entities.indexOf(object1) + entities.indexOf(object2);
    }

    protected void resolveCollisions(Set<Collision> collisions, double timeStep) {
        List<Collision> sortedCollisionList = new ArrayList<>(collisions);
        sortedCollisionList.sort(Comparator.comparingDouble(c -> c.collisionTime));
        HashMap<Integer, Collision> sortedCollisions = new LinkedHashMap<>();
        for (int i = 0; i < sortedCollisionList.size(); i++) {
            sortedCollisions.put(i, sortedCollisionList.get(i));
        }

        // TODO: Some time after the two balls collide, they go into an endless resolution loop with ground
        // TODO: Shouldnt have velocity at the ground --> amplification of vel --> resolution loop
        // TODO: Also maybe backtrack and solve again when needed and always check collision list before for collision time(which is now used as the current time of the collision)
        // TODO: Collisions happening right after drawshot collision dont work: Some strange angular velocities are added leading to problems; Maybe choose other tangential directions;
        // TODO: After drawshot, accumulation of vel looks buggy
        // Collision resolution loop
        double currentTime = 0.0;
        boolean multipleIterations = false;
        setUseRK2(physicsObjectsFromCollisions(sortedCollisionList), false);
        for (int i = 0; i < sortedCollisions.size(); i++) {
            if (sortedCollisions.get(i).isDistanceZero(epsilon))  {
                // Get sublist, solve and skip already solved collisions in next iteration
                // Solve with a timestep is executed with a timestep until the next collision should occur
                List<Collision> sublist = new ArrayList<>(Collections.singletonList(sortedCollisions.get(i)));
                int lastIndex = i;
                for (int k = (i + 1); k < sortedCollisions.size(); k++) {
                    if (sortedCollisions.get(k).isDistanceZero(epsilon) && sortedCollisions.get(k).containsActivePhysicsComponents(sortedCollisions.get(i).A, sortedCollisions.get(i).B)) {
                        sublist.add(sortedCollisions.get(k));
                        lastIndex = k;
                    }
                }
                i = lastIndex;

                // Do new collision resolution with the changed collisionTime for all already solved collisions if multipleIterations is true:
                // Backtrack to collision time for each collision; Then reverse the impulse using the saved solution from LCPSolver
                // Then recalculate the solution for the new time needed
                if (multipleIterations) {
                    // Find first index from i backwards that has a smaller collision time than i
                    // Find indices from i backwards that have a smaller collision time than i
                    List<Integer> indexList = new ArrayList<>();
                    for (int k = (i - 1); k >= 0; k--) {
                        if (sortedCollisions.get(k).collisionTime < sortedCollisions.get(i).collisionTime && sortedCollisions.get(k).containsActivePhysicsComponents(sortedCollisions.get(i).A, sortedCollisions.get(i).B)) {
                            indexList.add(k);
                        }
                    }
                    for (Integer k : indexList) {
                        sortedCollisions.get(k).A.performExplicitEulerTimeStep(sortedCollisions.get(k).collisionTime - currentTime, roundingDigit);
                        sortedCollisions.get(k).B.performExplicitEulerTimeStep(sortedCollisions.get(k).collisionTime - currentTime, roundingDigit);
                        DoubleMatrix x = LCPSolver.solvedCollisions.get(sortedCollisions.get(k).hashCode()).neg();
                        sortedCollisions.get(k).applyImpulse(x.get(0), new double[] {x.get(1), x.get(2)}, new double[] {x.get(4), x.get(5)}, roundingDigit);
                        processCollisionSubList(Collections.singletonList(sortedCollisions.get(k)), currentTime - sortedCollisions.get(k).collisionTime);
                        sortedCollisions.get(k).A.performSemiImplicitEulerTimeStep(currentTime - sortedCollisions.get(k).collisionTime, roundingDigit);
                        sortedCollisions.get(k).B.performSemiImplicitEulerTimeStep(currentTime - sortedCollisions.get(k).collisionTime, roundingDigit);
                    }
                }

                // Find next biggest timestep to solve to and set the collision time
                double maximumTime = timeStep;
                for (int k = (lastIndex + 1); k < sortedCollisions.size(); k++) {
                    if (sortedCollisions.get(k).collisionTime < maximumTime && sortedCollisions.get(k).collisionTime > currentTime && sortedCollisions.get(k).containsActivePhysicsComponents(sortedCollisions.get(i).A, sortedCollisions.get(i).B)) {
                        maximumTime = sortedCollisions.get(k).collisionTime;
                    }
                }
                sublist.forEach(Collision::finalizeCollisionData);
                processCollisionSubList(sublist, maximumTime - currentTime);
            } else {
                // Perform a timestep that at its greatest extent may only cover the difference between this collisions and the next collisions time
                double oldTime = sortedCollisions.get(i).collisionTime, maximumTime = timeStep;

                // Find next biggest timeStep estimate;
                for (int k = (i + 1); k < sortedCollisions.size(); k++) {
                    if (sortedCollisions.get(k).collisionTime < maximumTime && sortedCollisions.get(k).collisionTime > oldTime) {
                        maximumTime = sortedCollisions.get(k).collisionTime;
                    }
                }

                // Find next time to update to and perform a update
                double deltaTime = sortedCollisions.get(i).updateTimeStepEstimate(maximumTime, currentTime, collisionTimeEpsilon, roundingDigit);
                currentTime = Transforms.round(performResolutionTimeStep(new ArrayList<>(sortedCollisions.values()), currentTime, maximumTime, deltaTime, i), roundingDigit);

                // Re-sort according to the new estimates
                List<Collision> newSortedCollisions = sortedCollisions.values().stream().sorted(Comparator.comparingDouble(c -> c.collisionTime)).collect(Collectors.toList());
                for (int k = 0; k < newSortedCollisions.size(); k++) {
                    sortedCollisions.put(k, newSortedCollisions.get(k));
                }

                // Perform another iteration until the condition above is met or finish collision's iteration if collisions happens in next timestep
                if (deltaTime > epsilon || sortedCollisions.get(i).isDistanceZero(epsilon)) {
                    i--;
                    multipleIterations = true;
                }
            }
        }
        // Check if any further collisions arise in the remaining timestep
        checkForFurtherCollisions(physicsObjectsFromCollisions(sortedCollisionList), Transforms.round(timeStep - currentTime, roundingDigit));
        sortedCollisionList.forEach(collision -> {
            collision.A.alreadyTimeStepped = true;
            collision.B.alreadyTimeStepped = true;
        });
    }

    /**
     * Perform a timestep in the collision resolution loop, changing the currentTime variable, updating all collision data for non-solved collisions, and actually performing a timestep.
     * @return The updated value of currentTime
     */
    protected double performResolutionTimeStep(List<Collision> collisions, double currentTime, double maximumTime, double deltaTime, int currentIndex) {
        performTimeStep(deltaTime);
        for (int i = currentIndex; i < collisions.size(); i++) {
            collisions.get(i).updateCollisionNormal(epsilon, roundingDigit);
            collisions.get(i).updateCollisionTime(collisions.get(i).updateTimeStepEstimate(maximumTime, currentTime, collisionTimeEpsilon, roundingDigit));
        }
        return (currentTime + deltaTime);
    }

    protected void processCollisionSubList(List<Collision> subList, double timeStep) {
        DoubleMatrix[] matrices = createMatrices(subList, timeStep);
        int size = matrices[0].rows / subList.size();
        for (int i = 0; i < subList.size(); i++) {
            DoubleMatrix APrime = matrices[0].get(Transforms.createIndexArray(i * size, size), Transforms.createIndexArray(i * size, size));
            DoubleMatrix bPrime = matrices[1].get(Transforms.createIndexArray(i * size, size), new int[] {0});
            DoubleMatrix x = solveLCP(APrime, bPrime, subList.get(i)).x;
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
        Set<Collision> collisions = findCollisions(new SolutionParameters(0.0, timeStep, -1.0), timeStep);
        if (collisions.size() > 0) {
            leftOverTime = this.simulationTimeStep;
            double newTimeStep = collisions.toArray(new Collision[0])[collisions.size() - 1].collisionTime;
            newTimeStep = Transforms.round(newTimeStep, (int) Math.round(java.lang.Math.log10(1 / newTimeStep)));
            resolveCollisions(collisions, timeStep);
            setSimulationTimeStep(newTimeStep);

            // Update physics components not updated in resolve collisions
            List<PhysicsComponent> toBeUpdated = new ArrayList<>(physicsComponents);
            toBeUpdated.removeAll(physicsObjectsFromCollisions(new ArrayList<>(collisions)));
            performTimeStep(timeStep, toBeUpdated);
        } else {
            setUseRK2(physicsComponents, false);
            performTimeStep(timeStep, new ArrayList<>(physicsComponents));
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
        Set<PhysicsComponent> physicsComponents = new HashSet<>();
        collisions.forEach(collision -> {
            physicsComponents.add(collision.A);
            physicsComponents.add(collision.B);
        });
        return new ArrayList<>(physicsComponents);
    }

}
