package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.collisiondetection.Face;
import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.lang.Math;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

public class TestPhysicsCube extends AssimpModel implements PhysicsObject {

    public static float epsilon = Math.ulp(1.0f);

    public Vector3d max, min;
    public double coefficientOfRestitution = 1.0, coefficientOfStaticFriction = 0.1, coefficientOfKineticFriction = 0.14;

    private double mass, edgeLength;
    private Vector3d position, momentum, force, angularMomentum, torque;
    private Quaterniond orientation;
    private Matrix3d inertiaMatrix;
    private Matrix4d worldMatrix;

    public TestPhysicsCube(String path, Vector3d position, Vector3d momentum, Vector3d force, Quaterniond orientation, Vector3d angularMomentum, Vector3d torque, double mass, double edgeLength) {
        super(path, new Vector3f(0.0f).set(position));
        this.position = position;
        this.momentum = momentum;
        this.force = force;
        this.mass = mass;
        this.orientation = orientation;
        this.angularMomentum = angularMomentum;
        this.torque = torque;
        this.edgeLength = edgeLength;
        this.worldMatrix = new Matrix4d().identity().translate(position);
        inertiaMatrix = new Matrix3d().identity().scale((mass / 6.0) * edgeLength * edgeLength).invert();
        this.max = new Vector3d(position).add(new Vector3d(edgeLength / 2.0));
        this.min = new Vector3d(position).sub(new Vector3d(edgeLength / 2.0));
        performTimeStep(0.0);
    }

    @Override
    public void performTimeStep(double timeStep) {
        momentum.add(new Vector3d(force).mul(timeStep));
        position.add(new Vector3d(momentum).mul(timeStep).div(mass));

        angularMomentum.add(new Vector3d(torque).mul(timeStep));
        Vector3d angularVelocity = new Vector3d(angularMomentum).mul(inertiaMatrix);
        orientation.integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z);

        worldMatrix = new Matrix4d().identity().translate(position).rotate(orientation);
        this.setModelMatrix(new Matrix4f(worldMatrix));
        this.max = new Vector3d(position).add(new Vector3d(edgeLength / 2.0));
        this.min = new Vector3d(position).sub(new Vector3d(edgeLength / 2.0));
    }

    @Override
    public boolean isColliding(PhysicsObject physicsObject) {
        TestPhysicsCube cube = (TestPhysicsCube) physicsObject;
        return (this.min.x <= cube.max.x && this.max.x >= cube.min.x) && (this.min.y <= cube.max.y && this.max.y >= cube.min.y) && (this.min.z <= cube.max.z && this.max.z >= cube.min.z);
    }

    @Override
    public void handleCollisions(PhysicsObject physicsObject, double timeStep) {
        TestPhysicsCube cube = (TestPhysicsCube) physicsObject;
        if (this.position.distance(cube.position) < (this.edgeLength + cube.edgeLength) / 2.0) {
            double distance = (this.edgeLength + cube.edgeLength) / 2.0;
            List<Vector3d> thisVertices = getAllVerticesInWorldCoordinates(worldMatrix);
            List<Vector3d> cubeVertices = cube.getAllVerticesInWorldCoordinates(cube.worldMatrix);
            for (Vector3d thisVertex : thisVertices) {
                for (Vector3d cubeVertex : cubeVertices) {
                    if (thisVertex.distance(cubeVertex) < distance) {
                        distance = thisVertex.distance(cubeVertex);
                    }
                }
            }
            Vector3d thisDir = new Vector3d(cube.position).sub(this.position).normalize();
            Vector3d cubeDir = new Vector3d(this.position).sub(cube.position).normalize();
            double v = Math.abs(new Vector3d(thisDir).dot(new Vector3d(this.momentum).div(mass))) + Math.abs(new Vector3d(thisDir).dot(new Vector3d(cube.momentum).div(mass)));
            double f1 = new Vector3d(this.momentum).div(this.mass).dot(thisDir) / v;
            double f2 = new Vector3d(cube.momentum).div(cube.mass).dot(cubeDir) / v;
            double h1 = (distance * f1) / new Vector3d(this.momentum).div(this.mass).dot(thisDir);
            double h2 = (distance * f2) / new Vector3d(cube.momentum).div(cube.mass).dot(cubeDir);
            double h = (h1 + h2) / 2.0;
            this.performTimeStep(-h);
            cube.performTimeStep(-h);

            Set<Vector3f> contactVertices = new HashSet<>();
            for (Face thisFace : this.getAllFaces(new Matrix4f(this.worldMatrix))) {
                for (Face cubeFace : cube.getAllFaces(new Matrix4f(cube.worldMatrix))) {
                    thisFace.isColliding(cubeFace, null, null).forEach(collision -> contactVertices.add(collision.getPoint()));
                }
            }
            Vector3f contactVertex = new Vector3f(0.0f);
            contactVertices.forEach(contactVertex::add);
            contactVertex.div(contactVertices.size());
            Vector3f normal = new Vector3f(contactVertices.toArray(new Vector3f[0])[0]).sub(contactVertices.toArray(new Vector3f[0])[1]).cross(new Vector3f(contactVertices.toArray(new Vector3f[0])[2]).sub(contactVertices.toArray(new Vector3f[0])[3])).normalize();
            if (new Vector3f(contactVertex).sub(normal).distance(new Vector3f().set(this.position)) < new Vector3f(contactVertex).distance(new Vector3f().set(this.position))) {
                normal.mul(-1.0f);
            }

            Vector3d rA = new Vector3d(contactVertex).sub(this.position);
            Vector3d rB = new Vector3d(contactVertex).sub(cube.position);
            Vector3d kA = new Vector3d(rA).cross(new Vector3d(normal));
            Vector3d kB = new Vector3d(rB).cross(new Vector3d(normal));
            Vector3d uA = Transforms.round(Transforms.mulVectorWithMatrix4(kA, new Matrix4d().identity().set(new Matrix3d(this.inertiaMatrix)).invert()), 2);
            Vector3d uB = Transforms.round(Transforms.mulVectorWithMatrix4(kB, new Matrix4d().identity().set(new Matrix3d(cube.inertiaMatrix)).invert()), 2);

            double coefficientOfRestitution = (this.coefficientOfRestitution + cube.coefficientOfRestitution) / 2;

            double numerator = -(1 + coefficientOfRestitution) * (new Vector3d(normal).dot(new Vector3d(this.getVelocity()).sub(new Vector3d(cube.getVelocity()))) + (new Vector3d(this.getAngularVelocity()).dot(kA)) - new Vector3d(cube.getAngularVelocity()).dot(kB));
            double denominator = (1.0 / this.mass) + (1.0 / cube.mass) + kA.dot(uA) + kB.dot(uB);
            double f = numerator / denominator;
            Vector3d impulse = new Vector3d(normal).mul(f);

            double normalObjFrictionImpulse = computeFrictionImpulse(this, normal, (this.coefficientOfKineticFriction + cube.coefficientOfKineticFriction) / 2.0);
            double otherObjFrictionImpulse = computeFrictionImpulse(cube, normal, (this.coefficientOfKineticFriction + cube.coefficientOfKineticFriction) / 2.0);

            this.momentum.add(new Vector3f().set(new Vector3d(impulse)));
            cube.momentum.sub(new Vector3f().set(new Vector3d(impulse)));
            this.angularMomentum.add(new Vector3f().set(new Vector3d(kA).mul(f)));
            cube.angularMomentum.sub(new Vector3f().set(new Vector3d(kB).mul(f)));

            applyFrictionImpulse(this, normal, normalObjFrictionImpulse);
            applyFrictionImpulse(cube, normal, otherObjFrictionImpulse);

            this.performTimeStep(timeStep - h);
            cube.performTimeStep(timeStep - h);
        }
    }

    private void applyFrictionImpulse(TestPhysicsCube physicsObject, Vector3f normal, double frictionImpulse) {
        Vector3d tangentialVelDir = Transforms.safeNormalize(computeTangentialVelocity(physicsObject, normal));
        physicsObject.getVelocity().sub(new Vector3f().set(new Vector3d(tangentialVelDir).mul(Math.min(frictionImpulse, physicsObject.getVelocity().length() * physicsObject.mass)).div(physicsObject.mass)));
    }

    private double computeFrictionImpulse(TestPhysicsCube cube, Vector3f normal, double coefficientOfKineticFriction) {
        Vector3d vN = computeNormalVelocity(cube, normal);
        return new Vector3d(vN).mul(cube.mass).length() * coefficientOfKineticFriction;
    }

    private Vector3d computeNormalVelocity(TestPhysicsCube physicsObject, Vector3f normal) {
        return new Vector3d(normal).normalize().mul(new Vector3d(normal).normalize().dot(new Vector3d(physicsObject.getVelocity())));
    }

    private Vector3d computeTangentialVelocity(TestPhysicsCube physicsObject, Vector3f normal) {
        return new Vector3d(physicsObject.getVelocity()).sub(new Vector3d(normal).normalize().mul(new Vector3d(normal).normalize().dot(new Vector3d(physicsObject.getVelocity()))));
    }

    @Override
    public void predictTimeStep(double timeStep) {
        Vector3d momentum = new Vector3d(this.momentum).add(new Vector3d(force).mul(timeStep));
        Vector3d position = new Vector3d(this.position).add(new Vector3d(momentum).mul(timeStep).div(mass));

        Vector3d angularMomentum = new Vector3d(this.angularMomentum).add(new Vector3d(torque).mul(timeStep));
        Vector3d angularVelocity = new Vector3d(angularMomentum).mul(inertiaMatrix);
        Quaterniond orientation = new Quaterniond(this.orientation).integrate(timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z);

        Matrix4d mat = new Matrix4d().identity().translate(position).rotate(orientation);
        this.setModelMatrix(new Matrix4f(mat));
    }

    private Vector3d getVelocity() {
        return new Vector3d(this.momentum).div(mass);
    }

    private Vector3d getAngularVelocity() {
        return new Vector3d(this.angularMomentum).mul(new Matrix3d(inertiaMatrix).invert());
    }

}
