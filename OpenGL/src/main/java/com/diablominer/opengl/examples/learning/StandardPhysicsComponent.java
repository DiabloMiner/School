package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.Optional;

public abstract class StandardPhysicsComponent extends PhysicsComponent {

    public StandardPhysicsComponent(Material material, CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double radius) {
        super(material, collisionShape, position, velocity, orientation, angularVelocity, bodyFrameInertia, forces, mass, radius);
    }

    /*@Override
    public void performTimeStep(double timeStep, int roundingDigit) {
        performSemiImplicitEulerTimeStep(timeStep, roundingDigit);
    }*/

    /*@Override
    public Optional<Collision> getInitialCollision(PhysicsComponent B, SolutionParameters parameters, double timeStep, int roundingDigit) {
        // This will only work for constant/nearly-constant forces

        Vector3d collisionVector = B.collisionShape.findPenetrationDepth(this.collisionShape);
        Vector3d collisionDirection = computeCollisionDirection(B, collisionVector);
        double distance = Transforms.round(collisionVector.length(), roundingDigit);
        double deltaF = new Vector3d(B.force).div(B.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
        double deltaV = new Vector3d(B.velocity).sub(this.velocity).dot(collisionDirection);
        double h = parameters.standardReturnValue;
        if (deltaF != 0.0 && deltaF != -0.0) {
            h = Transforms.chooseSuitableSolution(parameters.min, parameters.max, parameters.standardReturnValue, Transforms.solveQuadraticEquation(deltaF, deltaV, distance, roundingDigit));
        } else if (deltaV != 0.0 && deltaV != 0.0) {
            h = Transforms.chooseSuitableSolution(parameters.min, parameters.max, parameters.standardReturnValue,new double[] {-distance / deltaV});
        }
        if (h >= parameters.min && h < timeStep) {
            if (h == 0.0 && Transforms.round(deltaV + deltaF * timeStep, roundingDigit - 2) >= 0.0) {
                return Optional.empty();
            }
            return Optional.of(new StandardCollision(new Vector3d(this.position).add(B.position).div(2.0), collisionDirection.normalize(new Vector3d()), this, B, h, distance));
        } else {
            return Optional.empty();
        }
    }*/

    @Override
    public boolean isColliding(PhysicsComponent physicsComponent) {
        return areCollisionShapesColliding(physicsComponent.collisionShape);
    }

    /*protected Vector3d computeCollisionDirection(PhysicsComponent object, Vector3d collisionVector) {
        Vector3d collisionDirection;
        if (Transforms.fixZeros(collisionVector).equals(new Vector3d(0.0))) {
            collisionDirection = Transforms.safeNormalize(new Vector3d(this.collisionShape.findClosestPoints(object.collisionShape)[0]).sub(this.position));
        } else {
            collisionDirection = collisionVector.normalize(new Vector3d());
        }
        return collisionDirection;
    }*/

    @Override
    public Matrix4d predictTimeStep(double timeStep) {
        return predictEulerTimeStep(timeStep);
    }

}
