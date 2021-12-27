package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.collisiondetection.*;
import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.RenderingEngineUnit;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RenderablePointLight extends PhysicsObject {

    private Model model;
    private PointLight pointLight;

    private Vector3f momentum = new Vector3f(0.0f);
    private Vector3f angularMomentum = new Vector3f(0.0f);

    public RenderablePointLight(PointLight pointLight, float forceY, String path, LogicalEngine logicalEngine, RenderingEngineUnit renderingEngineUnit) {
        super();
        logicalEngine.addGameObject(this);
        this.pointLight = pointLight;
        model = new Model(path, renderingEngineUnit, pointLight.getPosition());

        List<Vector3f> uniqueVertices = model.getAllUniqueVertices();
        mass = 10.0f;
        coefficientOfRestitution = 1.0f;
        position = determineCenterOfMass(uniqueVertices);
        inertia = createInertiaTensor(uniqueVertices, true);
        position.add(pointLight.getPosition());

        velocity = new Vector3f(0.0f, forceY / mass, 0.0f);
        force = new Vector3f(0.0f, 0.0f, 0.0f);
        orientation = new Quaternionf();
        angularVelocity = new Vector3f(0.0f);
        torque = new Vector3f(0.0f, 0.0f, 0.0f);

        this.bv = new AxisAlignedBoundingBox(position, uniqueVertices);
        this.obbTree = new OBBTree(model.getAllVertices(), 1);
        this.modelMatrix = new Matrix4f().identity();
    }

    @Override
    public void updateObjectState(double timeStep) {
        Matrix3f inertia = new Matrix3f().identity().rotate(orientation).mul(this.inertia).mul(new Matrix3f().identity().rotate(orientation).transpose());

        angularMomentum = new Vector3f(torque).mul((float) timeStep);
        angularVelocity.add(new Vector3f(angularMomentum).mul(new Matrix3f(inertia).invert()));
        orientation.integrate((float) timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z);

        momentum = new Vector3f(force).mul((float) timeStep);
        velocity.add(new Vector3f(momentum).div(mass));
        position.add(new Vector3f(velocity).mul((float) timeStep));

        modelMatrix = new Matrix4f().identity().translationRotate(position.x, position.y, position.z, orientation);
        pointLight.setPosition(position);
        model.setModelMatrix(modelMatrix);
    }

    @Override
    public void collide(Set<PhysicsObject> physicsObjects) {
        // Collision Detection
        bv.update(new Vector3f(position).sub(bv.getCenter()));
        for (PhysicsObject physicsObject : physicsObjects) {
            // Broad Phase
            physicsObject.bv.update(new Vector3f(physicsObject.position).sub(new Vector3f(physicsObject.bv.getCenter())));
            if (this.bv.isIntersecting(physicsObject.bv)) {
                // Narrow Phase
                if (this.obbTree.isColliding(physicsObject.obbTree, this.modelMatrix, physicsObject.modelMatrix)) {
                    // TODO: Remove
                    Vector3f norm = new Vector3f(0.0f, 1.0f, 0.0f);
                    Vector3f p1 = new Vector3f(0.0f).add(0.0f, 1.0f, 0.0f);
                    Vector3f p2 = new Vector3f(this.position).add(0.0f, -1.0f, 0.0f).sub(physicsObject.position);
                    if (p1.dot(norm) >= 0) {
                        norm.normalize();
                    } else {
                        norm.mul(-1.0f).normalize();
                    }
                    float d = p1.dot(norm);
                    if ((p2.dot(norm) - d) < -Math.ulp(1.0f)) {
                        this.position.add(new Vector3f(p1).sub(new Vector3f(p2)));
                    }
                    updateObjectState(0.0);
                    physicsObject.updateObjectState(0.0);

                    HashSet<Collision> collisions = new HashSet<>();
                    obbTree.updateTriangles(this.modelMatrix);
                    physicsObject.obbTree.updateTriangles(physicsObject.modelMatrix);

                    HashSet<Face> f1 = new HashSet<>();
                    HashSet<Face> f2 = new HashSet<>();
                    for (int i = 1; i< obbTree.getCollisionNodes().size(); i += 2) {
                        for (Face face : obbTree.getCollisionNodes().get(i - 1).getTriangles()) {
                            for (Face otherFace : obbTree.getCollisionNodes().get(i).getTriangles()) {
                                collisions.addAll(face.isColliding(otherFace, this, physicsObject, f1, f2));
                            }
                        }
                    }

                    // TODO: No collisions are reported
                    // TODO: Check if calcDeterminate works; Only face edge collisions are reported
                    // TODO: Problem is in face-edge collisions
                    // TODO: Possible Solution: Have edge-edge points who are in faces have their faces normals and remove face-edge colls
                    // TODO: Try to check if coplanar collisions works and then implement a solution getting to the coplanar collisons
                        // TODO: Are wrong, fix
                        // TODO: Check if physics system is right, else implement LCP

                    Vector3d dVA = new Vector3d(this.velocity);
                    Vector3d dVB = new Vector3d(physicsObject.velocity);
                    Vector3d dWA = new Vector3d(this.angularVelocity);
                    Vector3d dWB = new Vector3d(physicsObject.angularVelocity);
                    int colliding = 0;
                    Vector3f vec = new Vector3f(0.0f);
                    List<Collision> col = new ArrayList<>();
                    for (Collision collision : collisions) {
                        collision.collisionResponse(this, dVA, dVB, dWA, dWB);
                        if (collision.ct.equals(CollisionType.Colliding)) {
                            colliding++;
                            col.add(collision);
                            vec.add(collision.getPoint());
                        }
                    }
                    System.out.println();

                    vec.div(colliding);

                    this.velocity.set(dVA);
                    physicsObject.velocity.set(dVB);
                    this.angularVelocity.set(dWA);
                    physicsObject.angularVelocity.set(dWB);

                    LogicalEngine.addAlreadyCollidedPhysicsObject(this);
                    LogicalEngine.addAlreadyCollidedPhysicsObject(physicsObject);
                }
            }
        }
    }

    @Override
    public void predictGameObjectState(double timeStep) {
        Matrix3f inertia = new Matrix3f().identity().rotate(orientation).mul(this.inertia).mul(new Matrix3f().identity().rotate(orientation).transpose());

        Vector3f angularMomentum = new Vector3f(torque).mul((float) timeStep);
        Vector3f angularVelocity = new Vector3f(this.angularVelocity).add(new Vector3f(angularMomentum).mul(new Matrix3f(inertia).invert()));
        Quaternionf orientation = new Quaternionf(this.orientation).integrate((float) timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z);

        Vector3f momentum = new Vector3f(force).mul((float) timeStep);
        Vector3f velocity = new Vector3f(this.velocity).add(new Vector3f(momentum).div(mass));
        Vector3f position = new Vector3f(this.position).add(new Vector3f(velocity).mul((float) timeStep));

        Matrix4f modelMatrix = new Matrix4f().identity().translationRotate(position.x, position.y, position.z, orientation);
        pointLight.setPosition(position);
        model.setModelMatrix(modelMatrix);
    }

    public Model getModel() {
        return model;
    }

    public PointLight getPointLight() {
        return pointLight;
    }

    private Vector3f determineCenterOfMass(List<Vector3f> uniqueVertices) {
        Vector3f result = new Vector3f();
        for (Vector3f uniqueVertex : uniqueVertices) {
            result.add(new Vector3f(uniqueVertex).mul(mass / uniqueVertices.size()));
        }
        return result.div(mass);
    }

    public Matrix3f getInertia() {
        return new Matrix3f().identity().rotate(orientation).mul(this.inertia).mul(new Matrix3f().identity().rotate(orientation).transpose());
    }

    /**
     * All vertices are assumed to be defined relative to the center of mass.
     * The center of mass is assumed to be at (0, 0, 0).
     */
    private Matrix3f createInertiaTensor(List<Vector3f> uniqueVertices, boolean isSolid) {
        List<Vector3f> vertices = uniqueVertices;
        if (isSolid) {
            vertices = generateSolidGeometry(vertices, 1000);
        }

        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float slotResult = 0.0f;
                for (Vector3f uniqueVertex : vertices) {
                    slotResult += (mass / vertices.size()) * ((uniqueVertex.lengthSquared() * kroneckerDelta(i, j)) - (uniqueVertex.get(i) * uniqueVertex.get(j)));
                }
                result.set(j, i, slotResult);
            }
        }
        return result;
    }

    private float kroneckerDelta(int i, int j) {
        if (i != j) {
            return 0.0f;
        } else {
            return 1.0f;
        }
    }

    private List<Vector3f> generateSolidGeometry(List<Vector3f> vertices, int steps) {
        List<Vector3f> newList = new ArrayList<>();
        float factor = 1.0f / steps;
        for (int i = 0; i < steps; i++) {
            for (Vector3f vertex : vertices) {
                newList.add(new Vector3f(vertex).mul(factor));
            }
            factor += (1.0f / steps);
        }
        return newList;
    }

}
