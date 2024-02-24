package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;
import org.joml.Math;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PhysicsEngine implements SubEngine {

    public static double epsilon = 1e-15;
    // public static double collisionTimeEpsilon = 10e-50;
    // public static final int roundingDigit = 20;

    // Should be moved into a solver config
    public static int maxIter = 20;

    // public LCPSolverConfiguration solverConfig;
    public double simulationTimeStep;
    protected double erp, cfm;
    protected List<Entity> entities;
    protected double leftOverTime;

    /**
     * @param simulationTimeStep - Simulation timestep
     */
    public PhysicsEngine(double simulationTimeStep) {
        this.entities = new ArrayList<>();
        // this.solverConfig = solverConfig;
        this.simulationTimeStep = Math.min(Math.max(simulationTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
        this.erp = 0.1;
        this.cfm = 0.0001;
        this.leftOverTime = 0.0;
    }
    /**
     * @param entities - All entities simulated by this physics-engine
     * @param simulationTimeStep - Simulation timestep
     */
    public PhysicsEngine(List<Entity> entities, double simulationTimeStep) {
        this.entities = new ArrayList<>(entities);
        // this.solverConfig = solverConfig;
        this.simulationTimeStep = Math.min(Math.max(simulationTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
        this.erp = 0.1;
        this.cfm = 0.0001;
        this.leftOverTime = 0.0;
    }

    /**
     * @param simulationTimeStep - Simulation timestep
     * @param cfm - Constraint force mixing term (0 < cfm < 1): determines how much constraint force is mixed into the kinematic constraint in each timestep
     * @param erp - Error reduction parameter (0 < erp < 1): determines how much constraint error is reduced in each timestep
     */
    public PhysicsEngine(double simulationTimeStep, double cfm, double erp) {
        this.entities = new ArrayList<>();
        // this.solverConfig = solverConfig;
        this.simulationTimeStep = Math.min(Math.max(simulationTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
        this.erp = erp;
        this.cfm = cfm;
        this.leftOverTime = 0.0;
    }

    /**
     * @param entities - All entities simulated by this physics-engine
     * @param simulationTimeStep - Simulation timestep
     * @param cfm - Constraint force mixing term (0 < cfm < 1): determines how much constraint force is mixed into the kinematic constraint in each timestep
     * @param erp - Error reduction parameter (0 < erp < 1): determines how much constraint error is reduced in each timestep
     */
    public PhysicsEngine(List<Entity> entities, double simulationTimeStep, double cfm, double erp) {
        this.entities = new ArrayList<>(entities);
        // this.solverConfig = solverConfig;
        this.simulationTimeStep = Math.min(Math.max(simulationTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
        this.erp = erp;
        this.cfm = cfm;
        this.leftOverTime = 0.0;
    }

    public void timeStep(double dt) {
        // TODO: Currently assuming all constraints are contact constraints, adjust row sizes for J and e accordingly
        List<Entity> dynamicEntities = entities.stream().filter(entity -> !entity.getPhysicsComponent().isStatic()).collect(Collectors.toList());
        List<Contact> contacts = getContacts();
        int nBodies = dynamicEntities.size();
        int nContacts = contacts.size();

        // -------------------------------------------------------------------------------------------------------------
        // Step 1: Construct the matrices that describe the system
        // -------------------------------------------------------------------------------------------------------------

        DoubleMatrix MInv = new DoubleMatrix(nBodies * 6, nBodies * 6), q = new DoubleMatrix(nBodies * 7, 1), u = new DoubleMatrix(nBodies * 6, 1),
                qNext, uNext, fExt = new DoubleMatrix(nBodies * 6, 1), H = new DoubleMatrix(nBodies * 7, nBodies * 6), J = new DoubleMatrix(3 * nContacts, 6 * nBodies),
                e = new DoubleMatrix(3 * nContacts, 1), epsilon = Transforms.identity(3 * nContacts, 3 * nContacts);
        if (nContacts == 0) {
            J = new DoubleMatrix(3, 6 * nBodies).fill(0.0);
            e = new DoubleMatrix(3, 1).fill(0.0);
            epsilon = Transforms.identity(3, 3);
        }

        readEntityData(dynamicEntities, u, q, fExt, MInv, H);
        computeConstraints(dynamicEntities, contacts, J, e);

        // -------------------------------------------------------------------------------------------------------------
        // Step 2: Solve system
        // -------------------------------------------------------------------------------------------------------------
        // Note that integration is performed per semi implicit euler integration and has an error proportional to the size of the timestep
        // Also note that computing the change of orientation via the H-matrix is an approximation of the real change
        // whose error is also proportional to the size of the timestep (See 'Foundations of Physically Based Modeling and Animation' p. 200)

        DoubleMatrix Jt = J.transpose();
        DoubleMatrix A = J.mmul(MInv).mmul(Jt).add(epsilon.mul(cfm));
        DoubleMatrix b = J.mmul(u.add(MInv.mmul(fExt).mul(dt))).add(e.mul(erp / dt));
        DoubleMatrix x = new DoubleMatrix(A.getRows(), b.getColumns()).fill(0.0);
        DoubleMatrix lo = new DoubleMatrix(0, 0), hi = new DoubleMatrix(0, 0);

        // Solve for x
        LCPSolver.gaussSeidel(A, b, x, lo, hi, maxIter);

        uNext = u.subi(MInv.mmul(Jt).mmul(x)).addi(MInv.mmul(fExt).mul(dt), new DoubleMatrix(nBodies * 6, 1));
        qNext = q.addi(H.mmul(uNext).mul(dt), new DoubleMatrix(nBodies * 7, 1));

        // -------------------------------------------------------------------------------------------------------------
        // Step 3: Reinject values into entities
        // -------------------------------------------------------------------------------------------------------------

        writeEntityData(dynamicEntities, uNext, qNext);

        // TODO: Test more complicated collision scenarios & test multi body collision after that & implement friction after that
        // TODO: Test why collision with two billiard balls fails (error correction)

        /*Entity e0 = entities.get(0);
        Entity e1 = entities.get(1);

        System.out.println(i + ", " + e0.getPhysicsComponent().position.y + ", " + e1.getPhysicsComponent().position.y);
        i++;*/
    }

    protected List<Contact> getContacts() {
        Set<Contact> collisions = new HashSet<>();
        Map<Integer, Boolean> alreadySearched = new HashMap<>();
        for (Entity e1 : entities) {
            List<Entity> toBeSearched = new ArrayList<>(entities);
            toBeSearched.remove(e1);

            for (Entity e2 : toBeSearched) {
                if (!e1.getPhysicsComponent().isStatic() || !e2.getPhysicsComponent().isStatic()) {
                    if (!alreadySearched.containsKey(key(e1, e2))) {
                        e1.getPhysicsComponent().getContact(e2.getPhysicsComponent()).ifPresent(collisions::add);
                    }
                    alreadySearched.putIfAbsent(key(e1, e2), true);
                    alreadySearched.putIfAbsent(key(e2, e1), true);
                }
            }
        }
        return new ArrayList<>(collisions);
    }

    public void readEntityData(List<Entity> entities, DoubleMatrix u, DoubleMatrix q, DoubleMatrix fExt, DoubleMatrix MInv, DoubleMatrix H) {
        int n = entities.size();

        u.assertSameSize(new DoubleMatrix(n * 6, 1));
        q.assertSameSize(new DoubleMatrix(n * 7, 1));
        fExt.assertSameSize(new DoubleMatrix(n * 6, 1));
        MInv.assertSameSize(new DoubleMatrix(n * 6, n * 6));
        H.assertSameSize(new DoubleMatrix(n * 7, n * 6));

        for (int i = 0; i < n; i++) {
            Entity entity = entities.get(i);
            PhysicsComponent physComp = entity.getPhysicsComponent();
            physComp.determineForceAndTorque();
            physComp.assertCorrectValues();

            MInv.put(new int[] {6 * i, 6 * i + 1, 6 * i + 2}, new int[] {6 * i, 6 * i + 1, 6 * i + 2}, Transforms.jomlMatrixToJBLASMatrix(new Matrix3d().identity().scale(physComp.massInv)));
            MInv.put(new int[] {6 * i + 3, 6 * i + 4, 6 * i + 5}, new int[] {6 * i + 3, 6 * i + 4, 6 * i + 5}, Transforms.jomlMatrixToJBLASMatrix(physComp.worldFrameInertiaInv));

            u.put(new int[] {6 * i, 6 * i + 1, 6 * i + 2}, 0, Transforms.jomlVectorToJBLASVector(physComp.velocity));
            u.put(new int[] {6 * i + 3, 6 * i + 4, 6 * i + 5}, 0, Transforms.jomlVectorToJBLASVector(physComp.angularVelocity));

            fExt.put(new int[] {6 * i, 6 * i + 1, 6 * i + 2}, 0, Transforms.jomlVectorToJBLASVector(physComp.force));
            // Formula is given in 'Contact and Friction Simulation for Computer Graphics' (SIGGRAPH 2022), p. 37
            Vector3d torque = new Vector3d(physComp.torque).sub(physComp.angularVelocity.cross(physComp.angularVelocity.mul(physComp.worldFrameInertia, new Vector3d()), new Vector3d()));
            fExt.put(new int[] {6 * i + 3, 6 * i + 4, 6 * i + 5}, 0, Transforms.jomlVectorToJBLASVector(torque));

            q.put(new int[] {7 * i, 7 * i + 1, 7 * i + 2}, 0, Transforms.jomlVectorToJBLASVector(physComp.position));
            q.put(new int[] {7 * i + 3, 7 * i + 4, 7 * i + 5, 7 * i + 6}, 0, Transforms.jomlQuaternionToJBLASVector(physComp.orientation));

            // Computing the change of orientation via the H-matrix uses an approximation
            // whose error is dependent on the timestep (See 'Foundations of Physically Based Modeling and Animation' p. 200)
            DoubleMatrix Hi = Transforms.createHMatrix(physComp.orientation);
            H.put(new int[] {7 * i, 7 * i + 1, 7 * i + 2}, new int[] {6 * i, 6 * i + 1, 6 * i + 2}, Transforms.jomlMatrixToJBLASMatrix(new Matrix3d().identity()));
            H.put(new int[] {7 * i + 3, 7 * i + 4, 7 * i + 5, 7 * i + 6}, new int[] {6 * i + 3, 6 * i + 4, 6 * i + 5}, Hi);
        }
    }

    public void writeEntityData(List<Entity> entities, DoubleMatrix uNext, DoubleMatrix qNext) {
        int n = entities.size();

        uNext.assertSameSize(new DoubleMatrix(n * 6, 1));
        qNext.assertSameSize(new DoubleMatrix(n * 7, 1));

        for (int i = 0; i < n; i++) {
            Entity entity = entities.get(i);
            PhysicsComponent physComp = entity.getPhysicsComponent();

            physComp.velocity.set(Transforms.jblasVectorToJomlVector(uNext.get(new int[] {6 * i, 6 * i + 1, 6 * i + 2}, 0)));
            physComp.angularVelocity.set(Transforms.jblasVectorToJomlVector(uNext.get(new int[] {6 * i + 3, 6 * i + 4, 6 * i + 5}, 0)));

            physComp.position.set(Transforms.jblasVectorToJomlVector(qNext.get(new int[] {7 * i, 7 * i + 1, 7 * i + 2}, 0)));
            physComp.orientation.set(Transforms.jblasVectorToJomlQuaternion(qNext.get(new int[] {7 * i + 3, 7 * i + 4, 7 * i + 5, 7 * i + 6}, 0))).normalize();

            physComp.worldMatrix.set(new Matrix4d().identity().translate(physComp.position).rotate(physComp.orientation));
            physComp.computeWorldFrameInertia(physComp.worldMatrix);
            physComp.collisionShape.update(physComp.worldMatrix);

            physComp.assertCorrectValues();
        }
    }

    public void computeConstraints(List<Entity> entities, List<Contact> contacts, DoubleMatrix J, DoubleMatrix e) {
        // This function implicitly assumes only contact constraints are used
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            Constraint[] constraints = ContactConstraint.generateConstraints(contact);

            for (int j = 0; j < entities.size(); j++) {
                Entity entity = entities.get(j);
                PhysicsComponent physComp = entity.getPhysicsComponent();;
                for (Constraint constraint : constraints) {
                    if (constraint.getJacobian(physComp).isPresent()) {
                        J.put(new int[]{3 * i, 3 * i + 1, 3 * i + 2}, new int[]{6 * j, 6 * j + 1, 6 * j + 2, 6 * j + 3, 6 * j + 4, 6 * j + 5}, constraint.getJacobian(physComp).get());
                    }
                }
            }
            e.put(new int[]{3 * i, 3 * i + 1, 3 * i + 2}, new int[]{0}, Transforms.jomlVectorToJBLASVector(new Vector3d(contact.penetration)));
        }
    }

    protected int key(Entity object1, Entity object2) {
        return Objects.hash(object1, object2);
    }

    /*public void performTimeStep(double timeStep) {
        for (Entity entity : entities) {
            entity.getPhysicsComponent().performTimeStep(timeStep, roundingDigit);
        }
    }*/

    /*public void performExplicitEulerTimeStep(double timeStep) {
        for (Entity entity : entities) {
            entity.getPhysicsComponent().performExplicitEulerTimeStep(timeStep, roundingDigit);
        }
    }*/

    /*public void performTimeStep(List<PhysicsComponent> physicsComponents, double timeStep) {
        for (PhysicsComponent physicsComponent : physicsComponents) {
            physicsComponent.performTimeStep(timeStep, roundingDigit);
        }
    }*/

    /*public void performExplicitEulerTimeStep(List<PhysicsComponent> physicsComponents, double timeStep) {
        for (PhysicsComponent physicsComponent : physicsComponents) {
            physicsComponent.performExplicitEulerTimeStep(timeStep, roundingDigit);
        }
    }*/

    /*public void checkForCollisions(double timeStep) {
        Set<Collision> collisions = findCollisions(new SolutionParameters(0.0, timeStep, -1.0), timeStep);
        if (collisions.size() > 0) { resolveCollisions(collisions, timeStep); }
    }*/

    /*protected Set<Collision> findCollisions(SolutionParameters parameters, double timeStep) {
        Set<Collision> collisions = new HashSet<>();
        Map<Integer, Boolean> alreadySearched = new HashMap<>();
        for (Entity object1 : entities) {
            List<Entity> toBeSearched = new ArrayList<>(entities);
            toBeSearched.remove(object1);

            for (Entity object2 : toBeSearched) {
                if (object1.getPhysicsComponent().objectType.performTimeStep || object2.getPhysicsComponent().objectType.performTimeStep) {
                    if (!alreadySearched.containsKey(indexKey(object1, object2))) {
                        Optional<Collision> collisionOptional = object1.getPhysicsComponent().getInitialCollision(object2.getPhysicsComponent(), parameters, timeStep, roundingDigit);
                        collisionOptional.ifPresent(collisions::add);
                    }
                    alreadySearched.putIfAbsent(indexKey(object1, object2), true);
                    alreadySearched.putIfAbsent(indexKey(object2, object1), true);
                }
            }
        }
        return collisions;
    }*/

    /*protected int indexKey(Entity object1, Entity object2) {
        return Objects.hash(object1, object2);
    }*/

    /*protected void resolveCollisions(Set<Collision> collisions, double timeStep) {
        List<Collision> sortedCollisionList = new ArrayList<>(collisions);
        sortedCollisionList.sort(Comparator.comparingDouble(c -> c.collisionTime));
        HashMap<Integer, Collision> sortedCollisions = new LinkedHashMap<>();
        for (int i = 0; i < sortedCollisionList.size(); i++) {
            sortedCollisions.put(i, sortedCollisionList.get(i));
        }

        // Collision resolution loop
        double currentTime = 0.0;
        boolean multipleIterations = false;
        HashMap<PhysicsComponent, List<Double>> timeSteps = new HashMap<>();
        physicsObjectsFromCollisions(sortedCollisionList).forEach(physicsComponent -> timeSteps.put(physicsComponent, new ArrayList<>()));
        List<Collision> fullyTimeSteppedCollisions = new ArrayList<>();
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

                // Do new collision resolution with the changed collisionTime for all already solved collisions if multipleIterations is true:
                // Backtrack to collision time for each collision; Then reverse the impulse using the saved solution from LCPSolver
                // Then recalculate the solution for the new time needed
                if (multipleIterations) {
                    // Find first index from i backwards that has a smaller collision time than i
                    // Find indices from i backwards that have a smaller collision time than i
                    List<Integer> indexList = new ArrayList<>();
                    // aComps and bComps are compiled to consider physics components from the entire sublist
                    List<PhysicsComponent> aComps = new ArrayList<>(), bComps = new ArrayList<>();
                    sublist.forEach(collision -> { aComps.add(collision.A); bComps.add(collision.B); });
                    for (int k = (i - 1); k >= 0; k--) {
                        if (sortedCollisions.get(k).collisionTime < sortedCollisions.get(i).collisionTime && sortedCollisions.get(k).containsActivePhysicsComponents(aComps, bComps)) {
                            indexList.add(k);
                        }
                    }

                    // Timestep backwards to last impulse and resolve for new time
                    for (Integer k : indexList) {
                        // Reverse timesteps and former solution
                        performReverseTimeSteps(sortedCollisions.get(k).A, timeSteps.get(sortedCollisions.get(k).A), sortedCollisions.get(k).collisionTime, currentTime);
                        performReverseTimeSteps(sortedCollisions.get(k).B, timeSteps.get(sortedCollisions.get(k).B), sortedCollisions.get(k).collisionTime, currentTime);
                        DoubleMatrix x = LCPSolver.solvedCollisions.get(sortedCollisions.get(k).hashCode()).neg();
                        sortedCollisions.get(k).applyImpulse(x.get(0), new double[] {x.get(1), x.get(2)}, new double[] {x.get(4), x.get(5), x.get(3)}, roundingDigit);
                        LCPSolver.solvedCollisions.remove(sortedCollisions.get(k).hashCode());

                        // Resolve and timestep
                        processCollisionSubList(Collections.singletonList(sortedCollisions.get(k)), currentTime - sortedCollisions.get(k).collisionTime);
                        sortedCollisions.get(k).A.performSemiImplicitEulerTimeStep(currentTime - sortedCollisions.get(k).collisionTime, roundingDigit);
                        sortedCollisions.get(k).B.performSemiImplicitEulerTimeStep(currentTime - sortedCollisions.get(k).collisionTime, roundingDigit);
                        timeSteps.get(sortedCollisions.get(k).A).add(currentTime - sortedCollisions.get(k).collisionTime);
                        timeSteps.get(sortedCollisions.get(k).B).add(currentTime - sortedCollisions.get(k).collisionTime);
                    }

                    // Update collision data
                    for (Collision collision : sublist) {
                        collision.collisionTime = currentTime;
                        collision.updateCollisionNormal(epsilon, roundingDigit);
                    }
                    for (int k : indexList) {
                        sortedCollisions.get(k).updateCollisionNormal(epsilon, roundingDigit);
                    }

                    // Re-sort according to the new estimates
                    List<Collision> newSortedCollisions = sortedCollisions.values().stream().sorted(Comparator.comparingDouble(c -> c.collisionTime)).collect(Collectors.toList());
                    for (int k = 0; k < newSortedCollisions.size(); k++) {
                        sortedCollisions.put(k, newSortedCollisions.get(k));
                    }

                    i = lastIndex;
                    multipleIterations = false;

                    // Find next biggest timestep to solve to, set the collision time and solve the collision
                    double maximumTime = timeStep;
                    for (int k = (lastIndex + 1); k < sortedCollisions.size(); k++) {
                        if (sortedCollisions.get(k).collisionTime < maximumTime && sortedCollisions.get(k).collisionTime > currentTime && sortedCollisions.get(k).containsActivePhysicsComponents(sortedCollisions.get(i).A, sortedCollisions.get(i).B)) {
                            maximumTime = sortedCollisions.get(k).collisionTime;
                        }
                    }
                    sublist.forEach(Collision::finalizeCollisionData);
                    processCollisionSubList(sublist, maximumTime - currentTime);

                    // If the collision should be stepped to the end of the timestep, perform timestep now and add it to fullyTimeSteppedCollisions
                    // The timestep is performed now to avoid having multiple timesteps to get to the end of the timestep as the number of timesteps changes the result of the timestep(s)
                    if (maximumTime == timeStep) {
                        // Check for further collisions in connectedCollisions until timestep ends
                        if (indexList.size() > 0) {
                            // Get all connected physics components and check for further collisions until the timestep
                            Set<PhysicsComponent> connectedPhysicsComponents = new HashSet<>(physicsObjectsFromCollisions(sublist));
                            for (Integer k : indexList) {
                                connectedPhysicsComponents.add(sortedCollisions.get(k).A);
                                connectedPhysicsComponents.add(sortedCollisions.get(k).B);
                            }
                            checkForFurtherCollisions(connectedPhysicsComponents, maximumTime - currentTime);
                            setAlreadyTimeSteppedObjects(connectedPhysicsComponents, false);

                            // Inform the engine of the connected physicComponents' fully timestepped nature
                            for (Integer k : indexList) {
                                fullyTimeSteppedCollisions.add(sortedCollisions.get(k));
                                removeFromTimeSteps(timeSteps, sortedCollisions.get(k).A);
                                removeFromTimeSteps(timeSteps, sortedCollisions.get(k).B);
                            }
                            for (int k = i; k > (i - sublist.size()); k--) {
                                fullyTimeSteppedCollisions.add(sortedCollisions.get(k));
                                removeFromTimeSteps(timeSteps, sortedCollisions.get(k).A);
                                removeFromTimeSteps(timeSteps, sortedCollisions.get(k).B);
                            }
                        } else {
                            // If there are no connected collisions, this collision can just be timestepped to the end
                            sortedCollisions.get(i).A.performSemiImplicitEulerTimeStep(maximumTime - currentTime, roundingDigit);
                            sortedCollisions.get(i).B.performSemiImplicitEulerTimeStep(maximumTime - currentTime, roundingDigit);

                            fullyTimeSteppedCollisions.add(sortedCollisions.get(i));
                            removeFromTimeSteps(timeSteps, sortedCollisions.get(i).A);
                            removeFromTimeSteps(timeSteps, sortedCollisions.get(i).B);
                        }
                    }

                } else {
                    i = lastIndex;

                    // Find next biggest timestep to solve to, set the collision time and solve the collision
                    double maximumTime = timeStep;
                    for (int k = (lastIndex + 1); k < sortedCollisions.size(); k++) {
                        if (sortedCollisions.get(k).collisionTime < maximumTime && sortedCollisions.get(k).collisionTime > currentTime && sortedCollisions.get(k).containsActivePhysicsComponents(sortedCollisions.get(i).A, sortedCollisions.get(i).B)) {
                            maximumTime = sortedCollisions.get(k).collisionTime;
                        }
                    }
                    sublist.forEach(Collision::finalizeCollisionData);
                    processCollisionSubList(sublist, maximumTime - currentTime);

                    // If the collision should be stepped to the end of the timestep, perform timestep now and add it to fullyTimeSteppedCollisions
                    // The timestep is performed now to avoid having multiple timesteps to get to the end of the timestep as the number of timesteps changes the result of the timestep(s)
                    if (maximumTime == timeStep) {
                        fullyTimeSteppedCollisions.add(sortedCollisions.get(i));
                        sortedCollisions.get(i).A.performSemiImplicitEulerTimeStep(maximumTime - currentTime, roundingDigit);
                        sortedCollisions.get(i).B.performSemiImplicitEulerTimeStep(maximumTime - currentTime, roundingDigit);
                        removeFromTimeSteps(timeSteps, sortedCollisions.get(i).A);
                        removeFromTimeSteps(timeSteps, sortedCollisions.get(i).B);
                    }
                }
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
                currentTime = Transforms.round(performResolutionTimeStep(new ArrayList<>(sortedCollisions.values()), timeSteps, currentTime, maximumTime, deltaTime, i), roundingDigit);

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
        // Perform a timestep back to the current time for collisions that should be timestepped to the full timestep
        for (PhysicsComponent physicsComponent : physicsObjectsFromCollisions(fullyTimeSteppedCollisions)) {
            physicsComponent.performExplicitEulerTimeStep(currentTime - timeStep, roundingDigit);
        }

        // Check if any further collisions arise in the remaining timestep
        checkForFurtherCollisions(physicsObjectsFromCollisions(sortedCollisionList), Transforms.round(timeStep - currentTime, roundingDigit));
        setAlreadyTimeSteppedCollisions(sortedCollisionList, true);
    }*/

    /*protected void removeFromTimeSteps(Map<PhysicsComponent, List<Double>> timeSteps, PhysicsComponent toBeRemoved) {
        if (toBeRemoved.objectType.performTimeStep) {
            timeSteps.remove(toBeRemoved);
        }
    }*/

    /*protected void performReverseTimeSteps(PhysicsComponent physicsComponent, List<Double> timeSteps, double collisionTime, double currentTime) {
        // Perform multiple reverse time steps and remove these timesteps from the list storing timesteps for this physics component
        double tempTime = currentTime;
        List<Double> backsteps = new ArrayList<>();
        List<Double> reversedTimeSteps = new ArrayList<>(timeSteps);
        Collections.reverse(reversedTimeSteps);
        for (Double h : reversedTimeSteps) {
            if ((tempTime - h) - collisionTime < -epsilon) {
                break;
            }
            backsteps.add(h);
            physicsComponent.performExplicitEulerTimeStep(-h, roundingDigit);
            tempTime -= h;
        }
        timeSteps.removeAll(backsteps);
    }*/

    /**
     * Perform a timestep in the collision resolution loop for all non-solved collisions, changing the currentTime variable, updating all collision data for non-solved collisions, and actually performing a timestep.
     * @return The updated value of currentTime
     */
    /*protected double performResolutionTimeStep(List<Collision> collisions, Map<PhysicsComponent, List<Double>> timeSteps, double currentTime, double maximumTime, double deltaTime, int currentIndex) {
        // Only the collisions that are not solved yet i.e. the ones that are relevant are timestepped
        LinkedList<Collision> relevantCollisions = new LinkedList<>(collisions);
        relevantCollisions.removeIf(collision -> collisions.indexOf(collision) < currentIndex);
        performTimeStep(physicsObjectsFromCollisions(relevantCollisions), deltaTime);
        physicsObjectsFromCollisions(relevantCollisions).forEach(physicsComponent -> timeSteps.get(physicsComponent).add(deltaTime));
        for (int i = currentIndex; i < collisions.size(); i++) {
            collisions.get(i).updateCollisionNormal(epsilon, 20);
            collisions.get(i).updateCollisionTime(collisions.get(i).updateTimeStepEstimate(maximumTime, currentTime, collisionTimeEpsilon, 20));
        }
        return (currentTime + deltaTime);
    }*/

    /*protected void processCollisionSubList(List<Collision> subList, double timeStep) {
        DoubleMatrix[] matrices = createMatrices(subList, timeStep);
        int size = matrices[0].rows / subList.size();
        for (int i = 0; i < subList.size(); i++) {
            DoubleMatrix APrime = matrices[0].get(Transforms.createIndexArray(i * size, size), Transforms.createIndexArray(i * size, size));
            DoubleMatrix bPrime = matrices[1].get(Transforms.createIndexArray(i * size, size), new int[] {0});
            DoubleMatrix x = solveLCP(APrime, bPrime, subList.get(i)).x;
            subList.get(i).applyImpulse(x.get(0), new double[] {x.get(1), x.get(2)}, new double[] {x.get(4), x.get(5), x.get(3)}, roundingDigit);
            LCPSolver.addSolvedCollision(subList.get(i), x);
        }
    }*/

    /*protected DoubleMatrix[] createMatrices(List<Collision> subList, double timeStep) {
        if (solverConfig.solverChoice.forBLCP) {
            return LCPSolver.constructBLCPMatrices(subList.toArray(new Collision[0]), timeStep);
        } else {
            subList.forEach(Collision::generateAdditionalTangentialDirections);
            return LCPSolver.constructLCPMatrices(subList.toArray(new Collision[0]), timeStep);
        }
    }*/

    /*protected LCPSolverResult solveLCP(DoubleMatrix APrime, DoubleMatrix bPrime, Collision currentCollision) {
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
    }*/

    /*protected void checkForFurtherCollisions(Collection<PhysicsComponent> physicsComponents, double timeStep) {
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
            performTimeStep(toBeUpdated, timeStep);
        } else {
            performTimeStep(new ArrayList<>(physicsComponents), timeStep);
        }
    }*/

    /*protected void setAlreadyTimeSteppedObjects(Collection<PhysicsComponent> physicsComponents, boolean timeStepped) {
        for (PhysicsComponent physicsComponent : physicsComponents) {
            physicsComponent.alreadyTimeStepped = timeStepped;
        }
    }*/

    /*protected void setAlreadyTimeSteppedCollisions(Collection<Collision> collisions, boolean timeStepped) {
        for (Collision collision : collisions) {
            collision.A.alreadyTimeStepped = timeStepped;
            collision.B.alreadyTimeStepped = timeStepped;
        }
    }*/

    /*protected void setSimulationTimeStep(double newTimeStep) {
        simulationTimeStep = Math.min(Math.max(newTimeStep, Learning6.minTimeStep), Learning6.maxTimeStep);
    }*/

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

    /*public void updateEntities() {
        for (Entity entity : entities) {
            entity.getTransformComponent().update(entity.getPhysicsComponent().worldMatrix);
        }
    }*/

    /*public static List<PhysicsComponent> physicsObjectsFromCollisions(List<Collision> collisions) {
        Set<PhysicsComponent> physicsComponents = new HashSet<>();
        collisions.forEach(collision -> {
            physicsComponents.add(collision.A);
            physicsComponents.add(collision.B);
        });
        return new ArrayList<>(physicsComponents);
    }*/

}
