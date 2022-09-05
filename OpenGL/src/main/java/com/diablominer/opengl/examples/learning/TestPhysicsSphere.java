package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.util.Set;

public class TestPhysicsSphere extends PhysicsObject {

    public final AssimpModel model;

    private final Sphere collisionSphere;

    public TestPhysicsSphere(String path, ObjectType objectType, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Set<Force> forces, double mass, double radius) {
        super(objectType, new Sphere(new Matrix4d().translate(position).rotate(orientation), position, radius), position, velocity, orientation, angularVelocity, new Matrix3d().identity().scaling((2.0 / 5.0) * mass * radius), forces, mass, radius, 1.0, 0.1, 0.14);
        model = new AssimpModel(path, new Matrix4f().set(this.worldMatrix));
        collisionSphere = (Sphere) collisionShape;
    }

    @Override
    public void performTimeStep(double timeStep) {
        if (useRK2) { performRK2TimeStep(timeStep); }
        else { performSemiImplicitEulerTimeStep(timeStep); }
        model.setModelMatrix(new Matrix4f(worldMatrix));
        useRK2 = false;
    }

    @Override
    public boolean willCollide(PhysicsObject physicsObject, double timeStep) {
        // This will only work for constant/nearly-constant forces
        TestPhysicsSphere sphere = (TestPhysicsSphere) physicsObject;
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
    public boolean isColliding(PhysicsObject physicsObject) {
        return areCollisionShapesColliding(physicsObject.collisionShape);
    }

    @Override
    public Collision[] getCollisions(PhysicsObject physicsObject, double timeStep) {
        TestPhysicsSphere sphere = (TestPhysicsSphere) physicsObject;
        Vector3d collisionVector = sphere.collisionShape.findPenetrationDepth(this.collisionShape);
        Vector3d collisionDirection = computeCollisionDirection(sphere, collisionVector);
        double distance = collisionVector.length(), h = 0.0;
        for (int i = 0; i < maxCollisionTimeIterations; i++) {
            double deltaF = new Vector3d(sphere.force).div(sphere.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
            double deltaV = new Vector3d(sphere.velocity).sub(this.velocity).dot(collisionDirection) + this.radius * this.angularVelocity.length() + sphere.radius * sphere.angularVelocity.length();
            double localH = 0.0;
            if (deltaF != 0.0 && deltaF != -0.0) {
                localH = Transforms.chooseSuitableSolution(collisionTimeEpsilon, timeStep, 0.0, Transforms.solveQuadraticEquation(deltaF, deltaV, distance, roundingDigit));
            } else if (deltaV != 0.0 && deltaV != 0.0) {
                localH = -distance / deltaV;
            }
            this.performSemiImplicitEulerTimeStep(localH);
            sphere.performSemiImplicitEulerTimeStep(localH);

            h += localH;
            collisionVector = this.collisionShape.findPenetrationDepth(sphere.collisionShape);
            if (collisionVector.equals(new Vector3d(0.0))) {
                collisionDirection = Transforms.safeNormalize(new Vector3d(this.collisionShape.findClosestPoints(sphere.collisionShape)[0]).sub(this.position));
            } else {
                collisionDirection = collisionVector.normalize(new Vector3d());
            }
            distance = collisionVector.length();
            if (distance < epsilon && distance > -epsilon) { break; }
        }

        Vector3d dir = new Vector3d(this.position).sub(sphere.position).normalize();
        Vector3d contactPoint = new Vector3d(this.position).sub(new Vector3d(dir).mul(collisionSphere.radius));
        Vector3d tangentialDir1 = dir.cross(new Vector3d(1.0), new Vector3d());
        Vector3d tangentialDir2 = dir.cross(tangentialDir1, new Vector3d());
        double coefficientOfRestitution = (this.coefficientOfRestitution + sphere.coefficientOfRestitution) / 2.0;
        double coefficientOfStaticFriction = (this.coefficientOfStaticFriction + sphere.coefficientOfStaticFriction) / 2.0;
        double coefficientOfKineticFriction = (this.coefficientOfKineticFriction + sphere.coefficientOfKineticFriction) / 2.0;
        double timeStepTaken = h;

        return new Collision[] {new Collision(contactPoint, dir, new Vector3d[]{tangentialDir1, tangentialDir2}, coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction, this, sphere, timeStepTaken)};
    }

    private Vector3d computeCollisionDirection(PhysicsObject object, Vector3d collisionVector) {
        Vector3d collisionDirection;
        if (Transforms.fixZeros(collisionVector).equals(new Vector3d(0.0))) {
            collisionDirection = Transforms.safeNormalize(new Vector3d(this.collisionShape.findClosestPoints(object.collisionShape)[0]).sub(this.position));
        } else {
            collisionDirection = collisionVector.normalize(new Vector3d());
        }
        return collisionDirection;
    }

    @Override
    public void predictTimeStep(double timeStep) {
        Matrix4d mat = predictRK2TimeStep(timeStep);
        this.model.setModelMatrix(new Matrix4f(mat));
    }

}
