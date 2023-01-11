package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.Optional;

public abstract class StandardPhysicsComponent extends PhysicsComponent {

    protected boolean useRK2;

    public StandardPhysicsComponent(Material material, ObjectType objectType, CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double radius) {
        super(material, objectType, collisionShape, position, velocity, orientation, angularVelocity, bodyFrameInertia, forces, mass, radius);
        this.useRK2 = false;
    }

    @Override
    public void performTimeStep(double timeStep, int roundingDigit) {
        if (objectType.equals(ObjectType.DYNAMIC) && Math.signum(angularVelocity.x) * angularVelocity.length() > 0) {
            // TODO: Remove
            System.out.println(position.y + "  |  " + velocity.y);
            // System.out.println(velocity.y);
            // System.out.println(angularVelocity.length() + "  |  " + velocity.z);
        }
        // TODO: Properly remove rk2 timestep
        if (useRK2) { performRK2TimeStep(timeStep, roundingDigit); }
        else { performSemiImplicitEulerTimeStep(timeStep, roundingDigit); }
        useRK2 = false;
    }

    @Override
    public Optional<Collision> getInitialCollision(PhysicsComponent B, SolutionParameters parameters, double timeStep, int roundingDigit) {
        // This will only work for constant/nearly-constant forces

        // From collectCollisionData:
        // A timestep is performed while only changes along the z axis should have been considered as changes along the y axis havent been taken into account
        // Depending on the ordering of entities incorrect behavior may arise
        // Fundamental problem: A timestep is performed at a time when it is not yet known if another force has to be applied before then
        // Solutions have to perform timesteps at a time when it is known no other forces will have to be resolved before that
        // May be solve by letting willCollide provide initial estimate and actual collision information is gathered when it is needed shortly before it is solved
        // In this second collision information gathering phase collisions should also be rejected i.e. "resolved" by just applying a time step, if they do not actually collide

        // may be add conditional case if it says it will collide, make timestep and check geometrically to avoid replacing angular velocity
        // Seems like willCollide is incorrect as too long timesteps seem to be performed
        // may be add class returning collisionInformation from willCollide to getCollisions Or store it in the component
        Vector3d collisionVector = B.collisionShape.findPenetrationDepth(this.collisionShape);
        Vector3d collisionDirection = computeCollisionDirection(B, collisionVector);
        double distance = Transforms.round(collisionVector.length(), roundingDigit);
        double deltaF = new Vector3d(B.force).div(B.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
        double deltaV = new Vector3d(B.velocity).sub(this.velocity).dot(collisionDirection);
        double h = parameters.standardReturnValue, rk2H = h;
        if (deltaF != 0.0 && deltaF != -0.0) {
            h = Transforms.chooseSuitableSolution(parameters.min, parameters.max, parameters.standardReturnValue, Transforms.solveQuadraticEquation(deltaF, deltaV, distance, roundingDigit));
            rk2H = Transforms.chooseSuitableSolution(parameters.min, parameters.max, parameters.standardReturnValue, Transforms.solveQuadraticEquation(0.5 * deltaF, deltaV, distance, roundingDigit));
        } else if (deltaV != 0.0 && deltaV != 0.0) {
            h = Transforms.chooseSuitableSolution(parameters.min, parameters.max, parameters.standardReturnValue,new double[] {-distance / deltaV});
            rk2H = h;
        }
        if (rk2H >= parameters.min && rk2H < timeStep) { useRK2 = true; }
        if (h >= parameters.min && h < timeStep) {
            if (h == 0.0 && Transforms.round(deltaV + deltaF * timeStep, roundingDigit - 2) >= 0.0) {
                return Optional.empty();
            }
            return Optional.of(new StandardCollision(new Vector3d(this.position).add(B.position).div(2.0), collisionDirection.normalize(new Vector3d()), this, B, h, distance));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isColliding(PhysicsComponent physicsComponent) {
        return areCollisionShapesColliding(physicsComponent.collisionShape);
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
