package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.util.*;

public abstract class PhysicsComponent implements Component {

    public static double massThreshold = Double.MAX_VALUE / 1e160;

    public CollisionShape collisionShape;
    public ObjectType objectType;
    public Material material;

    protected double radius;
    protected final double mass;
    protected Vector3d position, velocity, force, angularVelocity, torque;
    protected final Quaterniond orientation;
    protected final Matrix3d bodyFrameInertia;
    protected Matrix3d worldFrameInertia, worldFrameInertiaInv;
    protected final Matrix4d worldMatrix;
    protected final Set<Force> forces;
    protected boolean alreadyTimeStepped;

    public PhysicsComponent(Material material, ObjectType objectType, CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double radius) {
        this.material = material;
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
        } else {
            this.worldFrameInertia = new Matrix3d(bodyFrameInertia);
            this.worldFrameInertiaInv = new Matrix3d().scale(0.0);
        }
        this.alreadyTimeStepped = false;
        determineForceAndTorque();
    }

    public void performSemiImplicitEulerTimeStep(double timeStep, int roundingDigit) {
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
    }

    public void performExplicitEulerTimeStep(double timeStep, int roundingDigit) {
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
    }

    private void updateComponents() {
        worldMatrix.set(new Matrix4d().identity().translate(position).rotate(orientation));
        computeWorldFrameInertia(worldMatrix);
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

    public abstract void performTimeStep(double timeStep, int roundingDigit);

    public abstract Optional<Collision> getInitialCollision(PhysicsComponent physicsComponent, SolutionParameters parameters, double timeStep, int roundingDigit);

    public abstract boolean isColliding(PhysicsComponent physicsComponent);

    public abstract Matrix4d predictTimeStep(double timeStep);

}
