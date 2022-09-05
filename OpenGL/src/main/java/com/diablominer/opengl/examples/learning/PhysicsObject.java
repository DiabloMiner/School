package com.diablominer.opengl.examples.learning;

import org.joml.*;

import java.lang.Math;
import java.util.*;

public abstract class PhysicsObject {

    public static double epsilon = Math.ulp(1.0);
    public static double collisionTimeEpsilon = 10e-50;
    public static double massThreshold = Double.MAX_VALUE / 1e160;
    public static int maxCollisionTimeIterations = 30;
    public static final int roundingDigit = 20;

    public double coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction;
    public CollisionShape collisionShape;
    public ObjectType objectType;

    protected final double mass, radius;
    protected Vector3d position, velocity, force, angularVelocity, torque;
    protected final Quaterniond orientation;
    protected final Matrix3d bodyFrameInertia;
    protected Matrix3d worldFrameInertia, worldFrameInertiaInv;
    protected final Matrix4d worldMatrix;
    protected final Set<Force> forces;
    protected final boolean updateInertiaMatrix;
    protected boolean alreadyTimeStepped, useRK2;

    public PhysicsObject(ObjectType objectType, CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double radius, double coefficientOfRestitution, double coefficientOfStaticFriction, double coefficientOfKineticFriction) {
        this.objectType = objectType;
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
        if (mass < massThreshold  && !Double.isInfinite(mass)) {
            Matrix3d rotationMatrix = worldMatrix.get3x3(new Matrix3d());
            this.worldFrameInertia = new Matrix3d(rotationMatrix).mul(bodyFrameInertia).mul(rotationMatrix.transpose(new Matrix3d()));
            this.worldFrameInertiaInv = worldFrameInertia.invert(new Matrix3d());
            this.updateInertiaMatrix = true;
        } else {
            this.worldFrameInertia = new Matrix3d(bodyFrameInertia);
            this.worldFrameInertiaInv = new Matrix3d().scale(0.0);
            this.updateInertiaMatrix = false;
        }
        this.coefficientOfRestitution = coefficientOfRestitution;
        this.coefficientOfStaticFriction = coefficientOfStaticFriction;
        this.coefficientOfKineticFriction = coefficientOfKineticFriction;
        this.alreadyTimeStepped = false;
        this.useRK2 = false;
        determineForceAndTorque();
    }

    protected Vector3d[] performEulerSubStep(double timeStep) {
        Vector3d velocity = new Vector3d(this.velocity).add(new Vector3d(force).div(mass).mul(timeStep));
        Vector3d deltaV = new Vector3d(new Vector3d(force).div(mass));
        Vector3d angularVelocity = new Vector3d(this.angularVelocity).add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep));
        Vector3d deltaOmega = new Vector3d(torque).mul(worldFrameInertiaInv);
        return new Vector3d[] {velocity, deltaV, angularVelocity, deltaOmega};
    }

    public void performRK2TimeStep(double timeStep) {
        if (!alreadyTimeStepped && objectType.performTimeStep) {
            determineForceAndTorque();
            // This will only work for constant/nearly-constant forces
            Vector3d[] k2 = performEulerSubStep(0.5 * timeStep);

            Vector3d deltaX = k2[0].mul(timeStep);
            position.add(deltaX);
            velocity.add(k2[1].mul(timeStep));

            orientation.integrate(timeStep, k2[2].x, k2[2].y, k2[2].z).normalize();
            angularVelocity.add(k2[3].mul(timeStep));

            updateComponents();
        }
        alreadyTimeStepped = false;
    }

    public void performSemiImplicitEulerTimeStep(double timeStep) {
        if (!alreadyTimeStepped && objectType.performTimeStep) {
            determineForceAndTorque();
            velocity.add(new Vector3d(force).div(mass).mul(timeStep));
            Vector3d deltaX = new Vector3d(velocity).mul(timeStep);
            position.add(deltaX);

            angularVelocity.add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep));
            orientation.integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();

            updateComponents();
        }
        alreadyTimeStepped = false;
    }

    public void performExplicitEulerTimeStep(double timeStep) {
        if (!alreadyTimeStepped && objectType.performTimeStep) {
            determineForceAndTorque();
            Vector3d deltaX = new Vector3d(velocity).mul(timeStep);
            position.add(deltaX);
            velocity.add(new Vector3d(force).div(mass).mul(timeStep));

            orientation.integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();
            angularVelocity.add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep));

            updateComponents();
        }
        alreadyTimeStepped = false;
    }

    private void updateComponents() {
        worldMatrix.set(new Matrix4d().identity().translate(position).rotate(orientation));
        if (updateInertiaMatrix) { computeWorldFrameInertia(worldMatrix); }
        collisionShape.update(worldMatrix);
    }

    protected void determineForceAndTorque() {
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

    public Matrix4d predictRK2TimeStep(double timeStep) {
        if (objectType.performTimeStep) {
            Vector3d velocity = new Vector3d(this.velocity).add(new Vector3d(force).div(mass).mul(0.5 * timeStep));
            Vector3d position = new Vector3d(this.position).add(new Vector3d(velocity).mul(timeStep));

            Vector3d angularVelocity = new Vector3d(this.angularVelocity).add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(0.5 * timeStep));
            Quaterniond orientation = new Quaterniond(this.orientation).integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();

            return new Matrix4d().identity().translate(position).rotate(orientation);
        } else {
            return worldMatrix;
        }
    }

    public Matrix4d predictEulerTimeStep(double timeStep) {
        if (objectType.performTimeStep) {
            Vector3d velocity = new Vector3d(this.velocity).add(new Vector3d(force).div(mass).mul(timeStep));
            Vector3d position = new Vector3d(this.position).add(new Vector3d(velocity).mul(timeStep));

            Vector3d angularVelocity = new Vector3d(this.angularVelocity).add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep));
            Quaterniond orientation = new Quaterniond(this.orientation).integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();

            return new Matrix4d().identity().translate(position).rotate(orientation);
        } else {
            return worldMatrix;
        }
    }

    protected boolean areCollisionShapesColliding(CollisionShape collisionShape) {
        return this.collisionShape.isColliding(collisionShape);
    }

    public abstract void performTimeStep(double timeStep);

    /**
     * Only if the two objects are intersecting should this function return true. If the two objects are just touching, it should return false.
     * It should also update useRK2 if an RK2-timestep will not lead to a collision.
     */
    public abstract boolean willCollide(PhysicsObject physicsObject, double timeStep);

    public abstract boolean isColliding(PhysicsObject physicsObject);

    public abstract Collision[] getCollisions(PhysicsObject physicsObject, double timeStep);

    public abstract void predictTimeStep(double timeStep);

}
