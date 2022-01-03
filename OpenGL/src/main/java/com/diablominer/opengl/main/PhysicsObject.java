package com.diablominer.opengl.main;

import com.diablominer.opengl.collisiondetection.BoundingVolume;
import com.diablominer.opengl.collisiondetection.OBBTree;
import org.joml.*;

import java.lang.Math;
import java.util.*;

public abstract class PhysicsObject implements GameObject {

    public static float epsilon = Math.ulp(1.0f);
    public static double bigNumber = 100000000.0;

    public PhysicsObject() {
        LogicalEngine.allPhysicsObjects.add(this);
    }

    public BoundingVolume bv;
    public OBBTree obbTree;

    public float coefficientOfRestitution;
    public float mass;
    public Matrix3f inertia;
    public Vector3f position;
    public Vector3f velocity;
    public Vector3f force;
    public Quaternionf orientation;
    public Vector3f angularVelocity;
    public Vector3f torque;
    public Matrix4f modelMatrix;

    public abstract void collide(LogicalEngine logicalEngine, Set<PhysicsObject> physicsObjects);

    public abstract Matrix3f getInertia();

    public boolean isVelPointingTowardsObject(PhysicsObject physicsObject) {
        Vector3d newPoint = new Vector3d(this.position).add(new Vector3d(this.velocity));
        newPoint.add(new Vector3d(angularVelocity).cross(newPoint));
        return newPoint.distance(new Vector3d(physicsObject.position)) < new Vector3d(this.position).distance(new Vector3d(physicsObject.position));
    }

    public double distanceToObjectWithVel(PhysicsObject physicsObject) {
        Vector3d newPoint = new Vector3d(this.position).add(this.velocity);
        newPoint.add(new Vector3d(angularVelocity).cross(newPoint));
        return newPoint.distance(new Vector3d(physicsObject.position));
    }

    public double determineTimeThroughVelocity(Vector3d thisPenDepth) {
        return Math.abs(thisPenDepth.get(thisPenDepth.maxComponent()));
    }

    public List<Vector3d> determinePenetrationDepths(int startingIndex, Vector3f velocity, Vector3f angularVelocity) {
        List<Vector3d> thisPenetrationDepths = new ArrayList<>();
        Map<Double, List<Vector3d>> multiplePoints = new HashMap<>();
        for (int i = startingIndex; i < obbTree.getCollisionNodes().size(); i += 2) {
            Vector3d nearestPoint = new Vector3d(this.position).add(bigNumber, bigNumber, bigNumber);
            for (Vector3f point : obbTree.getCollisionNodes().get(i).getPoints()) {
                if (point.distance(this.position) < nearestPoint.distance(new Vector3d(this.position))) {
                    nearestPoint.set(point);
                }

                if (multiplePoints.containsKey(new Vector3d(point).distance(new Vector3d(this.position)))) {
                    multiplePoints.get(new Vector3d(point).distance(new Vector3d(this.position))).add(new Vector3d(point));
                } else {
                    ArrayList<Vector3d> list = new ArrayList<>();
                    list.add(new Vector3d(point));
                    multiplePoints.put(new Vector3d(point).distance(new Vector3d(this.position)), list);
                }
            }

            if (multiplePoints.get(nearestPoint.distance(new Vector3d(this.position))).size() > 1) {
                Vector3d averagePoint = new Vector3d(0.0);
                for (Vector3d point : multiplePoints.get(nearestPoint.distance(new Vector3d(this.position)))) {
                    averagePoint.add(point);
                }
                averagePoint.div(multiplePoints.get(nearestPoint.distance(new Vector3d(this.position))).size());
                nearestPoint.set(averagePoint);
            }

            thisPenetrationDepths.add(nearestPoint.add(new Vector3d(velocity).mul(MyGame.millisecondsPerSimulationFrame / 1000.0f).add(new Vector3d(angularVelocity).cross(nearestPoint).mul(MyGame.millisecondsPerSimulationFrame / 1000.0))));
        }
        return thisPenetrationDepths;
    }

    public void changePositionAccordingToPenetrationDepth(Vector3d penetrationDepth) {
        addVelocityToPosition();
        this.position.add(new Vector3f().set(new Vector3d(this.velocity).mul(penetrationDepth.get(penetrationDepth.maxComponent()))));
        if (!angularVelocity.equals(new Vector3f(0.0f), epsilon)) {
            this.orientation.add(new Quaternionf().set(new Quaterniond().fromAxisAngleRad(new Vector3d(angularVelocity), new Vector3d(angularVelocity).length()).scale(penetrationDepth.get(penetrationDepth.maxComponent()))));
        }
    }

    public void subVelocityFromPosition() {
        this.position.sub(new Vector3f().set(new Vector3d(this.velocity).mul(MyGame.millisecondsPerSimulationFrame / 1000.0)));
        if (!angularVelocity.equals(new Vector3f(0.0f), epsilon)) {
            this.orientation.add(new Quaternionf().set(new Quaterniond().fromAxisAngleRad(new Vector3d(angularVelocity), new Vector3d(angularVelocity).length()).scale(MyGame.millisecondsPerSimulationFrame / 1000.0)).invert());
        }
    }

    public void addVelocityToPosition() {
        this.position.add(new Vector3f().set(new Vector3d(this.velocity).mul(MyGame.millisecondsPerSimulationFrame / 1000.0)));
        if (!angularVelocity.equals(new Vector3f(0.0f), epsilon)) {
            this.orientation.add(new Quaternionf().set(new Quaterniond().fromAxisAngleRad(new Vector3d(angularVelocity), new Vector3d(angularVelocity).length()).scale(MyGame.millisecondsPerSimulationFrame / 1000.0)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicsObject that = (PhysicsObject) o;
        return Float.compare(that.coefficientOfRestitution, coefficientOfRestitution) == 0 && Float.compare(that.mass, mass) == 0 && bv.equals(that.bv) && obbTree.equals(that.obbTree) && inertia.equals(that.inertia) && position.equals(that.position) && velocity.equals(that.velocity) && force.equals(that.force) && orientation.equals(that.orientation) && angularVelocity.equals(that.angularVelocity) && torque.equals(that.torque) && modelMatrix.equals(that.modelMatrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bv, obbTree, coefficientOfRestitution, mass, inertia, position, velocity, force, orientation, angularVelocity, torque, modelMatrix);
    }
}
