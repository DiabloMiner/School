package com.diablominer.opengl.examples.learning;

import org.joml.*;

import java.util.*;

public abstract class PhysicsComponent implements Component {

    public static double massThreshold = Double.MAX_VALUE / 1e160;

    public CollisionShape collisionShape;
    public Material material;

    protected final double mass, massInv;
    protected final Quaterniond orientation;
    protected final Matrix3d bodyFrameInertia;
    protected final Matrix4d worldMatrix;
    protected final Set<Force> forces;
    protected double radius;
    protected Vector3d position, velocity, force, angularVelocity, torque;
    protected Matrix3d worldFrameInertia, worldFrameInertiaInv;
    protected boolean isStatic;
    // protected boolean alreadyTimeStepped;

    public PhysicsComponent(Material material, CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double radius, boolean isStatic) {
        this.material = material;
        this.collisionShape = collisionShape;
        this.mass = mass;
        this.position = position;
        this.velocity = velocity;
        this.force = new Vector3d(0.0);
        this.orientation = orientation;
        this.angularVelocity = angularVelocity;
        this.torque = new Vector3d(0.0);
        this.bodyFrameInertia = bodyFrameInertia;
        this.worldMatrix = new Matrix4d().translate(position).rotate(orientation);
        this.forces = new HashSet<>(forces);
        this.radius = radius;
        this.isStatic = isStatic;
        if (mass < massThreshold  && !Double.isInfinite(mass)) {
            Matrix3d rotationMatrix = worldMatrix.get3x3(new Matrix3d());
            this.worldFrameInertia = new Matrix3d(rotationMatrix).mul(bodyFrameInertia).mul(rotationMatrix.transpose(new Matrix3d()));
            this.worldFrameInertiaInv = worldFrameInertia.invert(new Matrix3d());
            this.massInv = 1.0 / mass;
        } else {
            this.worldFrameInertia = new Matrix3d(bodyFrameInertia);
            this.worldFrameInertiaInv = new Matrix3d().scale(0.0);
            this.massInv = 0;
        }
        // this.alreadyTimeStepped = false;
        determineForceAndTorque();

        assertCorrectValues();
    }

    /*public void performSemiImplicitEulerTimeStep(double timeStep, int roundingDigit) {
        if (!alreadyTimeStepped && objectType.performTimeStep) {
            determineForceAndTorque();
            velocity.set(Transforms.round(velocity.add(new Vector3d(force).div(mass).mul(timeStep)), roundingDigit));
            Vector3d deltaX = new Vector3d(velocity).mul(timeStep);
            position.set(Transforms.round(position.add(deltaX), roundingDigit));

            angularVelocity.set(Transforms.round(angularVelocity.add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep)), roundingDigit));
            orientation.integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();

            updateComponents();
        }
        alreadyTimeStepped = false;
    }*/

    /*public void performExplicitEulerTimeStep(double timeStep, int roundingDigit) {
        if (!alreadyTimeStepped && objectType.performTimeStep) {
            determineForceAndTorque();
            Vector3d deltaX = new Vector3d(velocity).mul(timeStep);
            position.set(Transforms.round(position.add(deltaX, new Vector3d()), roundingDigit));
            velocity.set(Transforms.round(velocity.add(new Vector3d(force).div(mass).mul(timeStep), new Vector3d()), roundingDigit));

            orientation.integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();
            angularVelocity.set(Transforms.round(angularVelocity.add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep), new Vector3d()), roundingDigit));

            updateComponents();
        }
        alreadyTimeStepped = false;
    }*/

    public void determineForceAndTorque() {
        checkForces();
        force = new Vector3d(0.0);
        torque = new Vector3d(0.0);
        for (Force force : forces) {
            Map.Entry<Vector3d, Vector3d> results = force.applyForce(this);
            this.force.add(results.getKey());
            this.torque.add(results.getValue());
        }
    }

    protected void checkForces() {
        Set<Force> toBeRemoved = new HashSet<>();
        for (Force force : forces) {
            if (!force.isFulfilled(this)) {
                toBeRemoved.add(force);
            }
        }
        forces.removeAll(toBeRemoved);
    }

    protected void computeWorldFrameInertia(Matrix4d worldMatrix) {
        Matrix3d rotationMatrix = worldMatrix.get3x3(new Matrix3d());
        worldFrameInertia.set(new Matrix3d(rotationMatrix).mul(bodyFrameInertia).mul(rotationMatrix.transpose(new Matrix3d())));
        worldFrameInertia.invert(worldFrameInertiaInv);
    }

    public Matrix4d predictEulerTimeStep(double timeStep) {
        Vector3d velocity = new Vector3d(this.velocity).add(new Vector3d(force).div(mass).mul(timeStep));
        Vector3d position = new Vector3d(this.position).add(new Vector3d(velocity).mul(timeStep));

        Vector3d angularVelocity = new Vector3d(this.angularVelocity).add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep));
        Quaterniond orientation = new Quaterniond(this.orientation).integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();

        return new Matrix4d().identity().translate(position).rotate(orientation);
    }

    protected boolean areCollisionShapesColliding(CollisionShape collisionShape) {
        return this.collisionShape.isColliding(collisionShape);
    }

    public void assertCorrectValues() {
        assert !Objects.isNull(radius) : "Radius of rigid body is null";
        assert !Double.isNaN(radius) : "Radius of rigid body is NaN";
        assert Double.isFinite(radius) : "Radius of rigid body is not finite";
        assert !Objects.isNull(mass) : "Mass of rigid body is null";
        assert !Double.isNaN(mass) : "Mass of rigid body is NaN";
        assert !Objects.isNull(massInv) : "Inverse mass of rigid body is null";
        assert !Double.isNaN(massInv) : "Inverse mass of rigid body is NaN";
        assert Double.isFinite(massInv) : "Inverse mass of rigid body is not finite";

        assert !Objects.isNull(position) : "Position of rigid body is null";
        assert position.isFinite() : "Position of rigid body is not finite";
        for (int i = 0; i < 3; i++) {
            assert !Double.isNaN(position.get(i)) : "Position component of rigid body is NaN";
        }
        assert !Objects.isNull(velocity) : "Velocity of rigid body is null";
        assert velocity.isFinite() : "Velocity of rigid body is not finite";
        for (int i = 0; i < 3; i++) {
            assert !Double.isNaN(velocity.get(i)) : "Velocity component of rigid body is NaN";
        }
        assert !Objects.isNull(angularVelocity) : "Angular velocity  of rigid body is null";
        assert angularVelocity.isFinite() : "Angular velocity of rigid body is not finite";
        for (int i = 0; i < 3; i++) {
            assert !Double.isNaN(angularVelocity.get(i)) : "Angular velocity component of rigid body is NaN";
        }
        assert !Objects.isNull(force) : "Force  of rigid body is null";
        assert force.isFinite() : "Force of rigid body is not finite";
        for (int i = 0; i < 3; i++) {
            assert !Double.isNaN(force.get(i)) : "Force component of rigid body is NaN";
        }
        assert !Objects.isNull(torque) : "Torque  of rigid body is null";
        assert torque.isFinite() : "Torque of rigid body is not finite";
        for (int i = 0; i < 3; i++) {
            assert !Double.isNaN(torque.get(i)) : "Torque component of rigid body is NaN";
        }

        assert !Objects.isNull(orientation) : "Orientation  of rigid body is null";
        assert orientation.isFinite() : "Orientation of rigid body is not finite";
        assert !Double.isNaN(orientation.x()) : "Orientation x-component of rigid body is NaN";
        assert !Double.isNaN(orientation.y()) : "Orientation x-component of rigid body is NaN";
        assert !Double.isNaN(orientation.z()) : "Orientation x-component of rigid body is NaN";
        assert !Double.isNaN(orientation.w()) : "Orientation x-component of rigid body is NaN";

        assert !Objects.isNull(bodyFrameInertia) : "Bodyframe inertia matrix  of rigid body is null";
        assert !Objects.isNull(worldFrameInertia) : "Worldframe inertia matrix  of rigid body is null";
        assert !Objects.isNull(worldFrameInertiaInv) : "Inverse worldframe inertia matrix  of rigid body is null";
        assert !Objects.isNull(worldMatrix) : "World matrix  of rigid body is null";
        assert bodyFrameInertia.isFinite() : "Bodyframe inertia matrix of rigid body is not finite";
        assert worldFrameInertia.isFinite() : "Worldframe inertia matrix of rigid body is not finite";
        assert worldFrameInertiaInv.isFinite() : "Inverse worldframe inertia matrix of rigid body is not finite";
        assert worldMatrix.isFinite() : "World matrix of rigid body is not finite";
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assert !Double.isNaN(bodyFrameInertia.get(i, j)) : "Component of bodyframe inertia matrix of rigid body is NaN";
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assert !Double.isNaN(worldFrameInertia.get(i, j)) : "Component of worldframe inertia matrix of rigid body is NaN";
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assert !Double.isNaN(worldFrameInertiaInv.get(i, j)) : "Component of inverse worldframe inertia matrix of rigid body is NaN";
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assert !Double.isNaN(worldMatrix.get(i, j)) : "Component of world matrix of rigid body is NaN";
            }
        }

        for (Force force : forces) {
            assert !Objects.isNull(force) : "Force, " + force + ", of rigid body is null";
        }
    }

    public boolean isStatic() {
        return isStatic;
    }

    /*public abstract void performTimeStep(double timeStep, int roundingDigit);*/

    /*public abstract Optional<Collision> getInitialCollision(PhysicsComponent physicsComponent, SolutionParameters parameters, double timeStep, int roundingDigit);*/

    public abstract boolean isColliding(PhysicsComponent physicsComponent);

    public abstract Optional<Contact> getContact(PhysicsComponent physicsComponent);

    public abstract Matrix4d predictTimeStep(double timeStep);

}
