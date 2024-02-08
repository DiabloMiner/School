package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.lang.Math;
import java.util.*;

public class TestPhysicsCube extends PhysicsComponent {

    public AssimpModel model;

    private final double edgeLength;

    public TestPhysicsCube(AssimpModel model, Material material, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Set<Force> forces, double mass, double edgeLength, boolean isStatic) {
        super(material, new AABB(new Matrix4d().translate(position).rotate(orientation), position, edgeLength), position, velocity, orientation, angularVelocity, new Matrix3d().identity().scale((mass / 6.0) * edgeLength * edgeLength), forces, mass, Math.sqrt(3 * (edgeLength / 2) * (edgeLength / 2)), isStatic);
        this.edgeLength = edgeLength;
        this.model = model;
    }

    /*@Override
    public void performTimeStep(double timeStep, int roundingDigit) {
        performSemiImplicitEulerTimeStep(timeStep, roundingDigit);
    }*/

    @Override
    public boolean isColliding(PhysicsComponent physicsComponent) {
        return areCollisionShapesColliding(physicsComponent.collisionShape);
    }

    /*@Override
    public Optional<Collision> getInitialCollision(PhysicsComponent B, SolutionParameters parameters, double timeStep, int roundingDigit) {
        Vector3d collisionVector = B.collisionShape.findPenetrationDepth(this.collisionShape);
        Vector3d collisionDirection = computeCollisionDirection(B, collisionVector);
        double distance = Transforms.round(collisionVector.length(), roundingDigit);
        double deltaF = new Vector3d(B.force).div(B.mass).sub(new Vector3d(this.force).div(mass)).dot(collisionDirection);
        double deltaV = new Vector3d(B.velocity).sub(this.velocity).dot(collisionDirection);
        double h = 0.0;
        if (deltaF != 0.0 && deltaF != -0.0) {
            h = Transforms.chooseSuitableSolution(parameters.min, parameters.max, parameters.standardReturnValue, Transforms.solveQuadraticEquation(deltaF, deltaV, distance, roundingDigit));
        } else if (deltaV != 0.0 && deltaV != 0.0) {
            h = -distance / deltaV;
        }
        if (h >= 0 && h < timeStep) {
            return Optional.of(new StandardCollision(new Vector3d(this.position).add(B.position).div(2.0), collisionDirection.normalize(new Vector3d()), this, B, h, distance));
        } else {
            return Optional.empty();
        }
    }*/

    /*public Collision[] getCollisions(PhysicsComponent physicsComponent, double timeStep) {
        TestPhysicsCube cube = (TestPhysicsCube) physicsComponent;
        if (this.position.distance(cube.position) < (this.edgeLength + cube.edgeLength) / 2.0) {
            double distance = (this.edgeLength + cube.edgeLength) / 2.0;
            List<Vector3d> thisVertices = model.getAllVerticesInWorldCoordinates(worldMatrix);
            List<Vector3d> cubeVertices = cube.model.getAllVerticesInWorldCoordinates(cube.worldMatrix);
            for (Vector3d thisVertex : thisVertices) {
                for (Vector3d cubeVertex : cubeVertices) {
                    if (thisVertex.distance(cubeVertex) < distance) {
                        distance = thisVertex.distance(cubeVertex);
                    }
                }
            }
            Vector3d thisDir = new Vector3d(cube.position).sub(this.position).normalize();
            Vector3d cubeDir = new Vector3d(this.position).sub(cube.position).normalize();
            double v = Math.abs(new Vector3d(thisDir).dot(new Vector3d(this.velocity))) + Math.abs(new Vector3d(thisDir).dot(new Vector3d(cube.velocity)));
            double f1 = new Vector3d(this.velocity).dot(thisDir) / v;
            double f2 = new Vector3d(cube.velocity).dot(cubeDir) / v;
            double h1 = (distance * f1) / new Vector3d(this.velocity).dot(thisDir);
            double h2 = (distance * f2) / new Vector3d(cube.velocity).dot(cubeDir);
            double h = (h1 + h2) / 2.0;
            this.performTimeStep(-h, 15);
            cube.performTimeStep(-h, 15);

            Set<Vector3f> contactVertices = new HashSet<>();
            for (Face thisFace : this.model.getAllFaces(new Matrix4f(this.worldMatrix))) {
                for (Face cubeFace : cube.model.getAllFaces(new Matrix4f(cube.worldMatrix))) {
                    thisFace.isColliding(cubeFace, null, null).forEach(collision -> contactVertices.add(collision.getPoint()));
                }
            }
            Vector3f contactVertex = new Vector3f(0.0f);
            contactVertices.forEach(contactVertex::add);
            contactVertex.div(contactVertices.size());
            Vector3f normal = new Vector3f(contactVertices.toArray(new Vector3f[0])[0]).sub(contactVertices.toArray(new Vector3f[0])[1]).cross(new Vector3f(contactVertices.toArray(new Vector3f[0])[2]).sub(contactVertices.toArray(new Vector3f[0])[3])).normalize();
            if (new Vector3f(contactVertex).add(normal).distance(new Vector3f().set(this.position)) < new Vector3f(contactVertex).add(normal).distance(new Vector3f().set(cube.position))) {
                normal.mul(-1.0f);
            }

            Vector3d rA = new Vector3d(contactVertex).sub(this.position);
            Vector3d rB = new Vector3d(contactVertex).sub(cube.position);
            Vector3d kA = new Vector3d(rA).cross(new Vector3d(normal));
            Vector3d kB = new Vector3d(rB).cross(new Vector3d(normal));
            Vector3d uA = Transforms.round(Transforms.mulVectorWithMatrix4(kA, new Matrix4d().identity().set(worldFrameInertiaInv)), 2);
            Vector3d uB = Transforms.round(Transforms.mulVectorWithMatrix4(kB, new Matrix4d().identity().set(cube.worldFrameInertiaInv)), 2);

            double coefficientOfRestitution = Material.coefficientsOfRestitution.get(Material.hash(this.material, physicsComponent.material));

            double numerator = -(1 + coefficientOfRestitution) * (new Vector3d(normal).dot(new Vector3d(this.velocity).sub(new Vector3d(cube.velocity))) + (new Vector3d(this.angularVelocity).dot(kA)) - new Vector3d(cube.angularVelocity).dot(kB));
            double denominator = (1.0 / this.mass) + (1.0 / cube.mass) + kA.dot(uA) + kB.dot(uB);
            double f = Transforms.round(numerator / denominator, 10);
            Vector3d impulse = new Vector3d(normal).mul(f);

            double normalObjFrictionImpulse = computeFrictionImpulse(this, normal, Material.coefficientsOfKineticFriction.get(Material.hash(this.material, physicsComponent.material)));
            double otherObjFrictionImpulse = computeFrictionImpulse(cube, normal, Material.coefficientsOfKineticFriction.get(Material.hash(this.material, physicsComponent.material)));

            // DoubleMatrix[] lcpMatrices = LCPSolver.constructLCPMatrices(timeStep, new double[] {this.mass, cube.mass}, new double[] {(this.coefficientOfKineticFriction + cube.coefficientOfKineticFriction) / 2.0}, 4, new Vector3d[] {new Vector3d(contactVertex)}, new Vector3d[]{new Vector3d(this.position)}, new Vector3d[]{new Vector3d(cube.position)}, new Vector3d[]{new Vector3d(normal)}, new Vector3d[][]{{new Vector3d(0.0, 1.0, 0.0), new Vector3d(0.0, -1.0, 0.0), new Vector3d(0.0, 0.0, 1.0), new Vector3d(0.0, 0.0, -1.0)}}, new Vector3d[]{new Vector3d(this.velocity), new Vector3d(cube.velocity)}, new Vector3d[]{new Vector3d(0.0), new Vector3d(0.0)}, new Vector3d[]{new Vector3d(0.0), new Vector3d(0.0)}, new Vector3d[]{new Vector3d(0.0), new Vector3d(0.0)}, new Matrix3d[]{worldFrameInertia, cube.worldFrameInertia});
            *//*DoubleMatrix[] blcpMatrices = LCPSolver.constructBLCPMatrices(timeStep, new double[] {this.mass, cube.mass}, new double[] {coefficientOfRestitution}, new Vector3d[] {new Vector3d(contactVertex)}, new Vector3d[]{new Vector3d(this.position)}, new Vector3d[]{new Vector3d(cube.position)}, new Vector3d[]{new Vector3d(normal)}, new Vector3d[][]{{new Vector3d(0.0, 1.0, 0.0), new Vector3d(0.0, 0.0, 1.0)}}, new Vector3d[]{new Vector3d(this.velocity), new Vector3d(cube.velocity)}, new Vector3d[]{new Vector3d(0.0), new Vector3d(0.0)}, new Vector3d[]{new Vector3d(0.0), new Vector3d(0.0)}, new Vector3d[]{new Vector3d(0.0), new Vector3d(0.0)}, new Matrix3d[]{worldFrameInertia, cube.worldFrameInertia});
            LCPSolver.getInstance().solveBLCPWithPSOR(blcpMatrices[0], blcpMatrices[1], (this.coefficientOfKineticFriction + cube.coefficientOfKineticFriction) / 2.0, 1.0, 10e-6, 100);
            LCPSolver.getInstance().solveBLCPWithNNCG(blcpMatrices[0], blcpMatrices[1], (this.coefficientOfKineticFriction + cube.coefficientOfKineticFriction) / 2.0,1000);
            LCPSolver.getInstance().solveBLCPWithPGS(blcpMatrices[0], blcpMatrices[1], (this.coefficientOfKineticFriction + cube.coefficientOfKineticFriction) / 2.0, 10e-6, 100);
            LCPSolver.getInstance().solveBLCPWithMinimumMapNewton(blcpMatrices[0], blcpMatrices[1], (this.coefficientOfKineticFriction + cube.coefficientOfKineticFriction) / 2.0, 10e-4, 0.5, 10e-30, 10e-10, 10e-20, 250);*//*
            // LCPSolver.getInstance().solveLCPWithMinimumMapNewton(lcpMatrices[0], lcpMatrices[1], 10e-4, 0.5, 10e-30, 10e-10, 100);

            this.velocity.add(new Vector3f().set(new Vector3d(impulse).div(mass)));
            cube.velocity.sub(new Vector3f().set(new Vector3d(impulse).div(cube.mass)));
            this.angularVelocity.add(new Vector3f().set(new Vector3d(kA).mul(f).mul(worldFrameInertiaInv)));
            cube.angularVelocity.sub(new Vector3f().set(new Vector3d(kB).mul(f).mul(worldFrameInertiaInv)));

            applyFrictionImpulse(this, normal, normalObjFrictionImpulse);
            applyFrictionImpulse(cube, normal, otherObjFrictionImpulse);

            this.performTimeStep(timeStep - h, 15);
            cube.performTimeStep(timeStep - h, 15);
        }
        return new Collision[] {};
    }*/

    private void applyFrictionImpulse(TestPhysicsCube physicsObject, Vector3f normal, double frictionImpulse) {
        Vector3d tangentialVelDir = Transforms.safeNormalize(computeTangentialVelocity(physicsObject, normal));
        physicsObject.velocity.sub(new Vector3f().set(new Vector3d(tangentialVelDir).mul(Math.min(frictionImpulse, physicsObject.velocity.length() * physicsObject.mass)).div(physicsObject.mass)));
    }

    private double computeFrictionImpulse(TestPhysicsCube cube, Vector3f normal, double coefficientOfKineticFriction) {
        Vector3d vN = computeNormalVelocity(cube, normal);
        return new Vector3d(vN).mul(cube.mass).length() * coefficientOfKineticFriction;
    }

    private Vector3d computeNormalVelocity(TestPhysicsCube physicsObject, Vector3f normal) {
        return new Vector3d(normal).normalize().mul(new Vector3d(normal).normalize().dot(new Vector3d(physicsObject.velocity)));
    }

    private Vector3d computeTangentialVelocity(TestPhysicsCube physicsObject, Vector3f normal) {
        return new Vector3d(physicsObject.velocity).sub(new Vector3d(normal).normalize().mul(new Vector3d(normal).normalize().dot(new Vector3d(physicsObject.velocity))));
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
    public Optional<Contact> getContact(PhysicsComponent physicsComponent) {
        if (this.isColliding(physicsComponent)) {
            Vector3d[] closestPoints = collisionShape.findClosestPoints(physicsComponent.collisionShape);
            Vector3d point = closestPoints[0].add(closestPoints[1], new Vector3d()).mul(0.5);
            Vector3d normal = closestPoints[1].sub(closestPoints[0], new Vector3d()).normalize();
            Contact contact = new Contact(this, physicsComponent, point, normal);
            return Optional.of(contact);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Matrix4d predictTimeStep(double timeStep) {
        return predictEulerTimeStep(timeStep);
    }

}
