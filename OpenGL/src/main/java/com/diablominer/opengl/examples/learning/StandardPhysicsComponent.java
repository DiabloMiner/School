package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Collection;

public abstract class StandardPhysicsComponent extends PhysicsComponent {

    protected boolean useRK2;

    public StandardPhysicsComponent(ObjectType objectType, CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double radius, double coefficientOfRestitution, double coefficientOfStaticFriction, double coefficientOfKineticFriction) {
        super(objectType, collisionShape, position, velocity, orientation, angularVelocity, bodyFrameInertia, forces, mass, radius, coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction);
        this.useRK2 = false;
    }

    @Override
    public void performTimeStep(double timeStep) {
        if (useRK2) { performRK2TimeStep(timeStep); }
        else { performSemiImplicitEulerTimeStep(timeStep); }
        useRK2 = false;
    }

    @Override
    public boolean willCollide(PhysicsComponent physicsComponent, double timeStep) {
        // This will only work for constant/nearly-constant forces
        PhysicsSphere sphere = (PhysicsSphere) physicsComponent;
        Vector3d collisionVector = sphere.collisionShape.findPenetrationDepth(this.collisionShape);
        Vector3d collisionDirection = computeCollisionDirection(sphere, collisionVector);
        double distance = collisionVector.length();
        double deltaF = new Vector3d(sphere.force).div(sphere.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
        double deltaV = new Vector3d(sphere.velocity).sub(this.velocity).dot(collisionDirection) + this.radius * this.angularVelocity.length() + sphere.radius * sphere.angularVelocity.length();
        double rk2Distance = 0.5 * deltaF * timeStep * timeStep + deltaV * timeStep + distance;
        if (rk2Distance > -epsilon) { useRK2 = true; }
        double futureDistance = deltaF * timeStep * timeStep + deltaV * timeStep + distance;
        return futureDistance < -epsilon;
    }

    @Override
    public boolean isColliding(PhysicsComponent physicsComponent) {
        return areCollisionShapesColliding(physicsComponent.collisionShape);
    }

    @Override
    public Collision[] getCollisions(PhysicsComponent physicsComponent, double timeStep) {
        CollisionData collisionData = collectCollisionData(physicsComponent, timeStep);

        double coefficientOfRestitution = (this.coefficientOfRestitution + physicsComponent.coefficientOfRestitution) / 2.0;
        double coefficientOfStaticFriction = (this.coefficientOfStaticFriction + physicsComponent.coefficientOfStaticFriction) / 2.0;
        double coefficientOfKineticFriction = (this.coefficientOfKineticFriction + physicsComponent.coefficientOfKineticFriction) / 2.0;

        return new Collision[] {new Collision(new Vector3d(collisionData.position), new Vector3d(collisionData.direction), collisionData.getTangentialDirections(), coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction, this, physicsComponent, collisionData.timeStepTaken)};
    }

    protected CollisionData collectCollisionData(PhysicsComponent B, double timeStep) {
        Vector3d position = new Vector3d(0.0);
        Vector3d collisionVector = B.collisionShape.findPenetrationDepth(this.collisionShape), collisionDirection = computeCollisionDirection(B, collisionVector);
        double distance = collisionVector.length(), h = 0.0;
        for (int i = 0; i < maxCollisionTimeIterations; i++) {
            double deltaF = new Vector3d(B.force).div(B.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
            double deltaV = new Vector3d(B.velocity).sub(this.velocity).dot(collisionDirection) + this.radius * this.angularVelocity.length() + B.radius * B.angularVelocity.length();
            double localH = 0.0;
            if (deltaF != 0.0 && deltaF != -0.0) {
                localH = Transforms.chooseSuitableSolution(collisionTimeEpsilon, timeStep, 0.0, Transforms.solveQuadraticEquation(deltaF, deltaV, distance, roundingDigit));
            } else if (deltaV != 0.0 && deltaV != 0.0) {
                localH = -distance / deltaV;
            }
            this.performSemiImplicitEulerTimeStep(localH);
            B.performSemiImplicitEulerTimeStep(localH);

            h += localH;
            collisionVector = this.collisionShape.findPenetrationDepth(B.collisionShape);
            if (collisionVector.equals(new Vector3d(0.0), epsilon)) {
                Vector3d[] closestPoints = this.collisionShape.findClosestPoints(B.collisionShape);
                collisionDirection = Transforms.safeNormalize(new Vector3d(closestPoints[0]).sub(this.position));
                position.add(closestPoints[0]).add(closestPoints[1]).div(2.0);
            } else {
                collisionDirection = collisionVector.normalize(new Vector3d());
            }
            distance = collisionVector.length();
            if (distance < epsilon && distance > -epsilon) { break; }
        }
        return new CollisionData(position, collisionDirection, h);
    }

    protected Vector3d computeCollisionDirection(PhysicsComponent object, Vector3d collisionVector) {
        Vector3d collisionDirection;
        if (Transforms.fixZeros(collisionVector).equals(new Vector3d(0.0))) {
            collisionDirection = Transforms.safeNormalize(new Vector3d(this.collisionShape.findClosestPoints(object.collisionShape)[0]).sub(this.position));
        } else {
            collisionDirection = collisionVector.normalize(new Vector3d());
        }
        return collisionDirection;
    }

    @Override
    public Matrix4d predictTimeStep(double timeStep) {
        return predictRK2TimeStep(timeStep);
    }

}
