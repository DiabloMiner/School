package com.diablominer.opengl.examples.learning;

import org.joml.*;

import java.lang.Math;
import java.util.*;

public abstract class PhysicsObject {

    public static float epsilon = Math.ulp(1.0f);

    public double coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction;
    public CollisionShape collisionShape;

    protected final double mass;
    protected Vector3d position, velocity, force, angularVelocity, torque;
    protected final Quaterniond orientation;
    protected final Matrix3d bodyFrameInertia;
    protected Matrix3d worldFrameInertia, worldFrameInertiaInv;
    protected final Matrix4d worldMatrix;
    protected final Set<Force> forces;
    protected final boolean updateInertiaMatrix;

    public PhysicsObject(CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double coefficientOfRestitution, double coefficientOfStaticFriction, double coefficientOfKineticFriction) {
        this.collisionShape = collisionShape;
        this.mass = mass;
        this.position = position;
        this.velocity = velocity;
        this.force = new Vector3d(0.0);
        this.orientation = orientation;
        this.angularVelocity = angularVelocity;
        this.torque = new Vector3d(0.0);
        this.bodyFrameInertia = bodyFrameInertia;
        this.worldMatrix = new Matrix4d().identity().translate(position).rotate(orientation);
        this.forces = new HashSet<>(forces);
        if (mass < Double.MAX_VALUE / 1e160 && !Double.isInfinite(mass)) {
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
        sumUpForces();
    }

    public void performEulerTimeStep(double timeStep) {
        checkForces();
        sumUpForces();

        velocity.add(new Vector3d(force).div(mass).mul(timeStep));
        Vector3d deltaX = new Vector3d(velocity).mul(timeStep);
        position.add(deltaX);

        angularVelocity.add(new Vector3d(torque).mul(worldFrameInertiaInv).mul(timeStep));
        orientation.integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z).normalize();

        worldMatrix.set(new Matrix4d().identity().translate(position).rotate(orientation));
        if (updateInertiaMatrix) { computeWorldFrameInertia(worldMatrix); }
        collisionShape.update(deltaX);
    }

    protected void sumUpForces() {
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

    public abstract void performTimeStep(double timeStep);

    public abstract boolean isColliding(PhysicsObject physicsObject);

    public abstract Collision[] getCollisions(PhysicsObject physicsObject, double timeStep);

    public abstract void predictTimeStep(double timeStep);

}
