package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.lang.Math;
import java.util.Set;

public class TestPhysicsSphere extends PhysicsObject {

    public static final int digitAccuracy = 20;

    public final AssimpModel model;

    private final Sphere collisionSphere;

    public TestPhysicsSphere(String path, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Set<Force> forces, double mass, double radius) {
        super(new Sphere(position, radius), position, velocity, orientation, angularVelocity, new Matrix3d().identity().scaling((2.0 / 5.0) * mass * radius), forces, mass, 1.0, 0.1, 0.14);
        model = new AssimpModel(path, new Matrix4f().set(this.worldMatrix));
        collisionSphere = (Sphere) collisionShape;
    }

    @Override
    public void performTimeStep(double timeStep) {
        performEulerTimeStep(timeStep);
        model.setModelMatrix(new Matrix4f(worldMatrix));
    }

    @Override
    public boolean isColliding(PhysicsObject physicsObject) {
        TestPhysicsSphere sphere = (TestPhysicsSphere) physicsObject;
        return areCollisionShapesColliding(sphere.collisionShape);
    }

    @Override
    public Collision[] getCollisions(PhysicsObject physicsObject, double timeStep) {
        TestPhysicsSphere sphere = (TestPhysicsSphere) physicsObject;
        double distance = this.collisionShape.findPenetrationDepth(sphere.collisionShape).length();
        Vector3d thisDir = new Vector3d(sphere.position).sub(this.position).normalize(), sphereDir = new Vector3d(this.position).sub(sphere.position).normalize();
        sphere.velocity = new Vector3d(0.0, -1e-200, 0.0);
        double v = Math.abs(new Vector3d(thisDir).dot(this.velocity)) + Math.abs(new Vector3d(thisDir).dot(sphere.velocity));
        double f1 = this.velocity.dot(thisDir) / v;
        double f2 = sphere.velocity.dot(sphereDir) / v;
        double h1, h2, h = 0.0;
        if (!this.force.equals(new Vector3d(0.0))) {
            h1 = Transforms.chooseSuitableSolution(-timeStep, 0.0, Transforms.solveQuadraticEquation(this.force.dot(thisDir) / this.mass, this.velocity.dot(thisDir), distance * f1, digitAccuracy));
        } else {
            h1 = -(distance * f1) / new Vector3d(this.velocity).dot(thisDir);
        }
        if (!sphere.force.equals(new Vector3d(0.0))) {
            h2 = Transforms.chooseSuitableSolution(-timeStep, 0.0, Transforms.solveQuadraticEquation(sphere.force.dot(sphereDir) / sphere.mass, sphere.velocity.dot(sphereDir), distance * f2, digitAccuracy));
        } else {
            h2 = -(distance * f2) / new Vector3d(sphere.velocity).dot(sphereDir);
        }
        boolean ish1Finite = !Double.isNaN(h1) && Double.isFinite(h1), ish2Finite = !Double.isNaN(h2) && Double.isFinite(h2);
        if (ish1Finite && ish2Finite) {
            h = (h1 + h2) / 2.0;
        } else if (ish1Finite) {
            h = h1;
        } else if (ish2Finite) {
            h = h2;
        }
        this.performEulerTimeStep(h);
        sphere.performEulerTimeStep(h);

        double deltaF = new Vector3d(sphere.force).div(sphere.mass).sub(new Vector3d(this.force).div(this.mass)).dot(thisDir);
        double deltaV = new Vector3d(sphere.velocity).sub(this.velocity).dot(thisDir);
        double[] solutions = Transforms.solveQuadraticEquation(deltaF, deltaV, -distance, digitAccuracy);

        Vector3d dir = new Vector3d(this.position).sub(sphere.position).normalize();
        Vector3d contactPoint = new Vector3d(this.position).sub(new Vector3d(dir).mul(collisionSphere.radius));
        Vector3d tangentialDir1 = dir.cross(new Vector3d(1.0), new Vector3d());
        Vector3d tangentialDir2 = dir.cross(tangentialDir1, new Vector3d());
        double coefficientOfRestitution = (this.coefficientOfRestitution + sphere.coefficientOfRestitution) / 2.0;
        double coefficientOfStaticFriction = (this.coefficientOfStaticFriction + sphere.coefficientOfStaticFriction) / 2.0;
        double coefficientOfKineticFriction = (this.coefficientOfKineticFriction + sphere.coefficientOfKineticFriction) / 2.0;
        double timeStepTaken = timeStep + h;
        // TODO: Small velocity can fuck up timestep calculation: Maybe change to other computation ; Also numerical error
        // TODO: Test resting contact and try to work on lcp ; (maybe also implement collision detection after contact resolution?)
        // TODO: Then proceed to multibody collisions ; Implement VLP ; Also maybe error correction ala Erleben in VLP
        // TODO: Compare with other solver/provided data: Input raw data/compiled matrix to compare results: PyBullet is installed
        // TODO: Think about force objects

        return new Collision[] {new Collision(contactPoint, dir, new Vector3d[]{tangentialDir1, tangentialDir2}, coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction, this, sphere, timeStepTaken)};
    }

    @Override
    public void predictTimeStep(double timeStep) {
        Matrix4d mat = predictEulerTimeStep(timeStep);
        this.model.setModelMatrix(new Matrix4f(mat));
    }

}
