package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Collection;

public abstract class StandardPhysicsComponent extends PhysicsComponent {

    protected boolean useRK2;

    public StandardPhysicsComponent(Material material, ObjectType objectType, CollisionShape collisionShape, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Matrix3d bodyFrameInertia, Collection<Force> forces, double mass, double radius) {
        super(material, objectType, collisionShape, position, velocity, orientation, angularVelocity, bodyFrameInertia, forces, mass, radius);
        this.useRK2 = false;
    }

    @Override
    public void performTimeStep(double timeStep) {
        if (objectType.equals(ObjectType.DYNAMIC) && angularVelocity.length() > 0) {
            // TODO: Remove
            // System.out.println(position.y);
            // System.out.println(velocity.length());
            System.out.println(angularVelocity.length());
            if (angularVelocity.length() == 10.26172909488669) {
                System.out.print("");
            }
        }
        if (useRK2) { performRK2TimeStep(timeStep); }
        else { performSemiImplicitEulerTimeStep(timeStep); }
        useRK2 = false;
    }

    @Override
    public boolean willCollide(PhysicsComponent B, double timeStep) {
        // This will only work for constant/nearly-constant forces
        // may be add conditional case if it says it will collide, make timestep and check geometrically to avoid replacing angular velocity
        // Seems like willCollide is incorrect as too long timesteps seem to be performed
        // may be add class returning collisionInformation from willCollide to getCollisions Or store it in the component
        Vector3d collisionVector = B.collisionShape.findPenetrationDepth(this.collisionShape);
        Vector3d collisionDirection = computeCollisionDirection(B, collisionVector);
        double distance = collisionVector.length();
        double deltaF = new Vector3d(B.force).div(B.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
        double deltaV = new Vector3d(B.velocity).sub(this.velocity).dot(collisionDirection);
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

        return new Collision[] {new StandardCollision(new Vector3d(collisionData.position), new Vector3d(collisionData.direction), this, physicsComponent, collisionData.timeStepTaken)};
    }

    protected CollisionData collectCollisionData(PhysicsComponent B, double timeStep) {
        if (this.position.z >= 2.648) {
            System.out.print("");
        }
        Vector3d[] closestPoints = new Vector3d[0];
        Vector3d collisionVector = B.collisionShape.findPenetrationDepth(this.collisionShape), collisionDirection = computeCollisionDirection(B, collisionVector), position = new Vector3d(0.0);
        double distance = collisionVector.length(), h = 0.0;
        for (int i = 0; i < maxCollisionTimeIterations; i++) {
            if (distance < epsilon && distance > -epsilon) { break; }
            double deltaF = new Vector3d(B.force).div(B.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
            double deltaV = new Vector3d(B.velocity).sub(this.velocity).dot(collisionDirection);
            double localH = 0.0;
            if (deltaF != 0.0 && deltaF != -0.0) {
                localH = Transforms.chooseSuitableSolution(collisionTimeEpsilon, timeStep, 0.0, Transforms.solveQuadraticEquation(deltaF, deltaV, distance, roundingDigit));
            } else if (deltaV != 0.0 && deltaV != 0.0) {
                localH = -distance / deltaV;
            }
            this.performSemiImplicitEulerTimeStep(localH);
            B.performSemiImplicitEulerTimeStep(localH);
            // A timestep is performed while only changes along the z axis should have been considered as changes along the y axis havent been taken into account
            // Depending on the ordering of entities incorrect behavior may arise
            // Fundamental problem: A timestep is performed at a time when it is not yet known if another force has to be applied before then
            // Solutions have to perform timesteps at a time when it is known no other forces will have to be resolved before that
            // May be solve by letting willCollide provide initial estimate and actual collision information is gathered when it is needed shortly before it is solved
            // In this second collision information gathering phase collisions should also be rejected i.e. "resolved" by just applying a time step, if they do not actually collide

            h += localH;
            collisionVector = B.collisionShape.findPenetrationDepth(this.collisionShape);
            if (collisionVector.equals(new Vector3d(0.0), epsilon)) {
                closestPoints = this.collisionShape.findClosestPoints(B.collisionShape);
                collisionDirection = Transforms.safeNormalize(new Vector3d(closestPoints[0]).sub(this.position));
                position.add(closestPoints[0]).add(closestPoints[1]).div(2.0);
            } else {
                collisionDirection = collisionVector.normalize(new Vector3d());
            }
            distance = collisionVector.length();
        }
        if (closestPoints.length == 0) {
            closestPoints = this.collisionShape.findClosestPoints(B.collisionShape);
            position.add(closestPoints[0]).add(closestPoints[1]).div(2.0);
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
