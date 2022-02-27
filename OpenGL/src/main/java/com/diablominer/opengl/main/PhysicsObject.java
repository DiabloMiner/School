package com.diablominer.opengl.main;

import com.diablominer.opengl.collisiondetection.BoundingVolume;
import com.diablominer.opengl.collisiondetection.OBBTree;
import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.lang.Math;
import java.util.*;

public abstract class PhysicsObject implements GameObject {

    public static float epsilon = Math.ulp(1.0f);
    public static double bigNumber = 100000000.0;
    public static double factor = 2.0;

    public PhysicsObject() {
        LogicalEngine.allPhysicsObjects.add(this);
    }

    public BoundingVolume bv;
    public OBBTree obbTree;

    public float coefficientOfRestitution;
    public float coefficientOfKineticFriction;
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

    protected Vector3d[] findGreatestPenetrationDepth(PhysicsObject physicsObject) {
        Matrix4f thisMat = new Matrix4f(this.modelMatrix);
        Matrix4f otherMat = new Matrix4f(physicsObject.modelMatrix);
        obbTree.updatePoints(thisMat);
        physicsObject.obbTree.updatePoints(otherMat);

        List<Vector3d> thisPenetrationDepths = determinePenetrationDepths(1, physicsObject.velocity, physicsObject.angularVelocity);
        List<Vector3d> otherPenetrationDepths = physicsObject.determinePenetrationDepths(0, this.velocity, this.angularVelocity);

        obbTree.updatePoints(thisMat.invert());
        physicsObject.obbTree.updatePoints(otherMat.invert());

        Vector3d[] greatestPenetrationDepths = {thisPenetrationDepths.get(0), otherPenetrationDepths.get(0)};
        for (Vector3d thisPenDepth : thisPenetrationDepths) {
            if (thisPenDepth.distance(otherPenetrationDepths.get(thisPenetrationDepths.indexOf(thisPenDepth))) > Transforms.calculateDistance(greatestPenetrationDepths)) {
                greatestPenetrationDepths = new Vector3d[] {thisPenDepth, otherPenetrationDepths.get(thisPenetrationDepths.indexOf(thisPenDepth))};
            }
        }

        return greatestPenetrationDepths;
    }

    protected double resetPosition(PhysicsObject physicsObject) {
        // Reset position to before the collision
        subVelocityFromPosition();
        physicsObject.subVelocityFromPosition();

        updateObjectState(0.0);
        physicsObject.updateObjectState(0.0);

        // Determine greatest penetration-depth from all penetrating parts of the two objects
        Vector3d[] greatestPenetrationDepthPair = findGreatestPenetrationDepth(physicsObject);
        Vector3d greatestPenetrationDepth = new Vector3d(greatestPenetrationDepthPair[0]).sub(greatestPenetrationDepthPair[1]);
        correctPenetrationDepth(physicsObject, greatestPenetrationDepth);

        // Determine how much the object should pushed back and how much time this would have taken (if executed in its own timestep)
        double unusedTime = 0.0;
        Vector3d thisVelocity = new Vector3d(this.velocity).add(new Vector3d(angularVelocity).cross(greatestPenetrationDepthPair[0]));
        Vector3d otherVelocity = new Vector3d(physicsObject.velocity).add(new Vector3d(physicsObject.angularVelocity).cross(greatestPenetrationDepthPair[0]));

        if (new Vector3d(thisVelocity).equals(new Vector3d(0.0), epsilon) || new Vector3d(otherVelocity).equals(new Vector3d(0.0), epsilon)) {
            if (new Vector3d(thisVelocity).equals(new Vector3d(0.0), epsilon) && new Vector3d(otherVelocity).equals(new Vector3d(0.0), epsilon)) {
                Vector3d thisPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), thisVelocity);
                changePositionAccordingToPenetrationDepth(thisPenDepth);

                Vector3d otherPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), otherVelocity);
                physicsObject.changePositionAccordingToPenetrationDepth(otherPenDepth);

                unusedTime = java.lang.Math.max(determineTimeThroughVelocity(thisPenDepth), physicsObject.determineTimeThroughVelocity(otherPenDepth));
            } else {
                if (new Vector3d(otherVelocity).equals(new Vector3d(0.0f), epsilon)) {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), thisVelocity);

                    changePositionAccordingToPenetrationDepth(penDepth);
                    physicsObject.addVelocityToPosition();

                    unusedTime = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                } else {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), otherVelocity);

                    addVelocityToPosition();
                    physicsObject.changePositionAccordingToPenetrationDepth(penDepth);

                    unusedTime = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                }
            }
        } else {
            if (isVelPointingTowardsObject(physicsObject) && physicsObject.isVelPointingTowardsObject(this)) {
                double velRatio = distanceToObjectWithVel(physicsObject) / physicsObject.distanceToObjectWithVel(this);
                double otherPenetrationRatio = 1.0 / (velRatio + 1.0);
                double thisPenetrationRatio = 1.0 - otherPenetrationRatio;

                Vector3d thisPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).mul(thisPenetrationRatio), thisVelocity);
                changePositionAccordingToPenetrationDepth(thisPenDepth);

                Vector3d otherPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).mul(otherPenetrationRatio), otherVelocity);
                physicsObject.changePositionAccordingToPenetrationDepth(otherPenDepth);

                unusedTime = java.lang.Math.max(determineTimeThroughVelocity(thisPenDepth), physicsObject.determineTimeThroughVelocity(otherPenDepth));
            } else {
                if (!physicsObject.isVelPointingTowardsObject(this)) {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), thisVelocity);

                    changePositionAccordingToPenetrationDepth(penDepth);
                    physicsObject.addVelocityToPosition();

                    unusedTime = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                } else if (!isVelPointingTowardsObject(physicsObject)) {
                    Vector3d penDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth), otherVelocity);

                    addVelocityToPosition();
                    physicsObject.changePositionAccordingToPenetrationDepth(penDepth);

                    unusedTime = java.lang.Math.max(determineTimeThroughVelocity(penDepth), physicsObject.determineTimeThroughVelocity(penDepth));
                } else {
                    Vector3d thisPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), thisVelocity);
                    changePositionAccordingToPenetrationDepth(thisPenDepth);

                    Vector3d otherPenDepth = Transforms.safeDiv(new Vector3d(greatestPenetrationDepth).div(2.0), otherVelocity);
                    physicsObject.changePositionAccordingToPenetrationDepth(otherPenDepth);

                    unusedTime = java.lang.Math.max(determineTimeThroughVelocity(thisPenDepth), physicsObject.determineTimeThroughVelocity(otherPenDepth));
                }
            }
        }

        return unusedTime;
    }

    private void correctPenetrationDepth(PhysicsObject physicsObject, Vector3d penetrationDepth) {
        Vector3d relativeVelocity = new Vector3d(physicsObject.velocity).sub(this.velocity).normalize();
        Vector3d precisionVector = new Vector3d(0.0);

        for (int i = 0; i < 3; i++) {
            double precision;
            if (relativeVelocity.get(i) != 0.0) {
                precision = Math.abs(penetrationDepth.get(i) / relativeVelocity.get(i));
            } else {
                precision = -1.0;
            }
            precision = (precision > (1.0 + epsilon)) ? -1.0 : precision;
            precisionVector.setComponent(i, precision);
        }

        double approximateCollisionTime = (penetrationDepth.get(Transforms.getMaxComponent(precisionVector)) / relativeVelocity.get(Transforms.getMaxComponent(precisionVector)));
        for (int j = 0; j < 3; j++) {
            if (j != Transforms.getMaxComponent(precisionVector)) {
                penetrationDepth.setComponent(j, approximateCollisionTime * relativeVelocity.get(j));
            }
        }
    }

    protected boolean isVelPointingTowardsObject(PhysicsObject physicsObject) {
        Vector3d newPoint = new Vector3d(this.position).add(new Vector3d(this.velocity));
        newPoint.add(new Vector3d(angularVelocity).cross(newPoint));
        return newPoint.distance(new Vector3d(physicsObject.position)) < new Vector3d(this.position).distance(new Vector3d(physicsObject.position));
    }

    protected double distanceToObjectWithVel(PhysicsObject physicsObject) {
        Vector3d newPoint = new Vector3d(this.position).add(this.velocity);
        newPoint.add(new Vector3d(angularVelocity).cross(newPoint));
        return newPoint.distance(new Vector3d(physicsObject.position));
    }

    protected double determineTimeThroughVelocity(Vector3d thisPenDepth) {
        return Math.abs(thisPenDepth.get(thisPenDepth.maxComponent()));
    }

    protected List<Vector3d> determinePenetrationDepths(int startingIndex, Vector3f velocity, Vector3f angularVelocity) {
        List<Vector3d> thisPenetrationDepths = new ArrayList<>();
        Map<Double, List<Vector3d>> multiplePoints = new HashMap<>();
        for (int i = startingIndex; i < obbTree.getCollisionNodes().size(); i += 2) {
            Vector3d nearestPoint = new Vector3d(this.position).add(bigNumber, bigNumber, bigNumber);
            for (Vector3f point : new HashSet<>(obbTree.getCollisionNodes().get(i).getPoints())) {
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

            Set<Vector3d> vecList = new HashSet<>();
            Map<Double, List<Vector3d>> modifiedMultiplePoints = new HashMap<>(multiplePoints);
            if (multiplePoints.get(nearestPoint.distance(new Vector3d(this.position))).size() > 1) {
                vecList.addAll(multiplePoints.get(nearestPoint.distance(new Vector3d(this.position))));
                modifiedMultiplePoints.remove(nearestPoint.distance(new Vector3d(this.position)));

                Vector3d averagePoint = determineAveragePoint(multiplePoints.get(nearestPoint.distance(new Vector3d(this.position))));
                nearestPoint.set(averagePoint);
            } else {
                vecList.add(nearestPoint);
            }

            for (Double key : modifiedMultiplePoints.keySet()) {
                if (key <= (nearestPoint.distance(new Vector3d(this.position)) * factor)) {
                    Vector3d earlierAveragePoint = determineAveragePoint(vecList);
                    vecList.addAll(modifiedMultiplePoints.get(key));
                    Vector3d laterAveragePoint = determineAveragePoint(vecList);

                    if (laterAveragePoint.distance(new Vector3d(this.position)) >= earlierAveragePoint.distance(new Vector3d(this.position))) {
                        modifiedMultiplePoints.get(key).forEach(vecList::remove);
                    }
                }
            }
            nearestPoint.set(determineAveragePoint(vecList));

            thisPenetrationDepths.add(nearestPoint.add(new Vector3d(velocity).mul(MyGame.millisecondsPerSimulationFrame / 1000.0f).add(new Vector3d(angularVelocity).cross(nearestPoint).mul(MyGame.millisecondsPerSimulationFrame / 1000.0))));
        }
        return thisPenetrationDepths;
    }

    protected Vector3d determineAveragePoint(Collection<Vector3d> list) {
        Vector3d avgPoint = new Vector3d(0.0);
        for (Vector3d point : list) {
            avgPoint.add(point);
        }
        avgPoint.div(list.size());
        return avgPoint;
    }

    protected void changePositionAccordingToPenetrationDepth(Vector3d penetrationDepth) {
        addVelocityToPosition();
        this.position.add(new Vector3f().set(new Vector3d(this.velocity).mul(penetrationDepth.get(penetrationDepth.maxComponent()))));
        if (!angularVelocity.equals(new Vector3f(0.0f), epsilon)) {
            this.orientation.add(new Quaternionf().set(new Quaterniond().fromAxisAngleRad(new Vector3d(angularVelocity), new Vector3d(angularVelocity).length()).scale(penetrationDepth.get(penetrationDepth.maxComponent()))));
        }
    }

    protected void subVelocityFromPosition() {
        this.position.sub(new Vector3f().set(new Vector3d(this.velocity).mul(MyGame.millisecondsPerSimulationFrame / 1000.0)));
        if (!angularVelocity.equals(new Vector3f(0.0f), epsilon)) {
            this.orientation.add(new Quaternionf().set(new Quaterniond().fromAxisAngleRad(new Vector3d(angularVelocity), new Vector3d(angularVelocity).length()).scale(MyGame.millisecondsPerSimulationFrame / 1000.0)).invert());
        }
    }

    protected void addVelocityToPosition() {
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
