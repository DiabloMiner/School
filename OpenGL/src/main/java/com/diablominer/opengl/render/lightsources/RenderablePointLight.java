package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.collisiondetection.*;
import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.main.MyGame;
import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.RenderingEngineUnit;
import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.util.*;

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
    public void collide(LogicalEngine logicalEngine, Set<PhysicsObject> physicsObjects) {
        // Collision Detection
        bv.update(new Vector3f(position).sub(bv.getCenter()));
        for (PhysicsObject physicsObject : physicsObjects) {
            // Broad Phase
            physicsObject.bv.update(new Vector3f(physicsObject.position).sub(new Vector3f(physicsObject.bv.getCenter())));
            if (this.bv.isIntersecting(physicsObject.bv)) {
                // Narrow Phase
                if (this.obbTree.isColliding(physicsObject.obbTree, this.modelMatrix, physicsObject.modelMatrix)) {
                    // TODO: Remove
                    /*Vector3f norm = new Vector3f(0.0f, 1.0f, 0.0f);
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
                    obbTree.updateTriangles(this.modelMatrix);
                    physicsObject.obbTree.updateTriangles(physicsObject.modelMatrix);*/

                    double time = resetPosition(physicsObject);

                    updateObjectState(0.0);
                    physicsObject.updateObjectState(0.0);
                    obbTree.updateTriangles(this.modelMatrix);
                    physicsObject.obbTree.updateTriangles(physicsObject.modelMatrix);


                    HashSet<Collision> collisions = new HashSet<>();
                    for (int i = 1; i< obbTree.getCollisionNodes().size(); i += 2) {
                        for (Face face : obbTree.getCollisionNodes().get(i - 1).getTriangles()) {
                            for (Face otherFace : obbTree.getCollisionNodes().get(i).getTriangles()) {
                                collisions.addAll(face.isColliding(otherFace, this, physicsObject));
                            }
                        }
                    }

                    // TODO: Finish integrating time critical cd: USE DOUBLE and debug face: edge-edge intersections
                    // TODO: Finish cleaning up the code

                    // Determine which points are colliding and prepare for the calculation of the average point
                    Vector3f averagePos = new Vector3f(0.0f);
                    List<Collision> collidingPoints = new ArrayList<>();
                    for (Collision collision : collisions) {
                        if (collision.isColliding()) {
                            averagePos.add(collision.getPoint());
                            collidingPoints.add(collision);
                        }
                    }
                    System.out.println();

                    // Calculate the average point, search in the collisions for a face which contains this point and use this face's saved normal and physics-objects
                    // to create a new a collision and calculate the collision response
                    averagePos.div(collidingPoints.size());
                    Vector3f normal = new Vector3f(0.0f);
                    for (Collision collision : collidingPoints) {
                        if (collision.getFace().isPointInsideTriangle(averagePos)) {
                            normal.set(collision.getFace().getNormalizedNormal());
                            new Collision(averagePos, normal, collision.getNormalObj(), collision.getOtherObj()).collisionResponse();
                            break;
                        }
                    }

                    double remainingTime = ((double) MyGame.millisecondsPerSimulationFrame / 1000.0) - time;
                    updateObjectState(remainingTime);

                    logicalEngine.addAlreadyCollidedPhysicsObject(this);
                    logicalEngine.addAlreadyCollidedPhysicsObject(physicsObject);
                    obbTree.clearCollisionNodes();
                    physicsObject.obbTree.clearCollisionNodes();
                }
            }
        }
    }

    private Vector3d findGreatestPenetrationDepth(PhysicsObject physicsObject) {
        subVelocityFromPosition();
        physicsObject.subVelocityFromPosition();

        updateObjectState(0.0);
        physicsObject.updateObjectState(0.0);
        Matrix4f thisMat = new Matrix4f(this.modelMatrix);
        Matrix4f otherMat = new Matrix4f(physicsObject.modelMatrix);
        obbTree.updatePoints(thisMat);
        physicsObject.obbTree.updatePoints(otherMat);

        List<Vector3d> thisPenetrationDepths = determinePenetrationDepths(1, physicsObject.velocity, physicsObject.angularVelocity);
        List<Vector3d> otherPenetrationDepths = physicsObject.determinePenetrationDepths(0, this.velocity, this.angularVelocity);

        obbTree.updatePoints(thisMat.invert());
        physicsObject.obbTree.updatePoints(otherMat.invert());

        Vector3d greatestPenetrationDepth = new Vector3d(0.0f);
        for (Vector3d thisPenDepth : thisPenetrationDepths) {
            if (thisPenDepth.distance(otherPenetrationDepths.get(thisPenetrationDepths.indexOf(thisPenDepth))) > greatestPenetrationDepth.length()) {
                greatestPenetrationDepth.set(new Vector3d(thisPenDepth).sub(otherPenetrationDepths.get(thisPenetrationDepths.indexOf(thisPenDepth))));
            }
        }

        return greatestPenetrationDepth;
    }

    private double resetPosition(PhysicsObject physicsObject) {
        Vector3d greatestPenetrationDepth = findGreatestPenetrationDepth(physicsObject);
        double time = 0.0;

        if (new Vector3d(this.velocity).equals(new Vector3d(0.0), epsilon) || new Vector3d(physicsObject.velocity).equals(new Vector3d(0.0), epsilon)) {
            if (new Vector3d(this.velocity).equals(new Vector3d(0.0), epsilon) && new Vector3d(physicsObject.velocity).equals(new Vector3d(0.0), epsilon)) {
                Vector3d thisPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), velocity);
                changePositionAccordingToPenetrationDepth(thisPenDepth);

                Vector3d otherPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), physicsObject.velocity);
                physicsObject.changePositionAccordingToPenetrationDepth(otherPenDepth);

                time = java.lang.Math.max(determineTimeThroughVelocity(thisPenDepth), physicsObject.determineTimeThroughVelocity(otherPenDepth));
            } else {
                if (new Vector3d(physicsObject.velocity).equals(new Vector3d(0.0f), epsilon)) {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), velocity);

                    changePositionAccordingToPenetrationDepth(penDepth);
                    physicsObject.addVelocityToPosition();

                    time = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                } else {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), physicsObject.velocity);

                    addVelocityToPosition();
                    physicsObject.changePositionAccordingToPenetrationDepth(penDepth);

                    time = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                }
            }
        } else {
            if (isVelPointingTowardsObject(physicsObject) && physicsObject.isVelPointingTowardsObject(this)) {
                double velRatio = distanceToObjectWithVel(physicsObject) / physicsObject.distanceToObjectWithVel(this);
                double otherPenetrationRatio = 1.0 / (velRatio + 1.0);
                double thisPenetrationRatio = 1.0 - otherPenetrationRatio;

                Vector3d thisPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).mul(thisPenetrationRatio), velocity);
                changePositionAccordingToPenetrationDepth(thisPenDepth);

                Vector3d otherPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).mul(otherPenetrationRatio), physicsObject.velocity);
                physicsObject.changePositionAccordingToPenetrationDepth(otherPenDepth);

                time = java.lang.Math.max(determineTimeThroughVelocity(thisPenDepth), physicsObject.determineTimeThroughVelocity(otherPenDepth));
            } else {
                if (!physicsObject.isVelPointingTowardsObject(this)) {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), velocity);

                    changePositionAccordingToPenetrationDepth(penDepth);
                    physicsObject.addVelocityToPosition();

                    time = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                } else if (!isVelPointingTowardsObject(physicsObject)) {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), physicsObject.velocity);

                    addVelocityToPosition();
                    physicsObject.changePositionAccordingToPenetrationDepth(penDepth);

                    time = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                } else {
                    Vector3d thisPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), velocity);
                    changePositionAccordingToPenetrationDepth(thisPenDepth);

                    Vector3d otherPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), physicsObject.velocity);
                    physicsObject.changePositionAccordingToPenetrationDepth(otherPenDepth);

                    time = java.lang.Math.max(determineTimeThroughVelocity(thisPenDepth), physicsObject.determineTimeThroughVelocity(otherPenDepth));
                }
            }
        }

        return time;
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

    public Model getModel() {
        return model;
    }

    public PointLight getPointLight() {
        return pointLight;
    }

}
