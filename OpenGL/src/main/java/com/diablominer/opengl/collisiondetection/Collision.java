package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.lang.Math;
import java.util.Objects;


public class Collision {

    public static final float epsilon = Math.ulp(1.0f);

    private Vector3f point;
    private Vector3f normal;
    private PhysicsObject normalObj, otherObj;

    // TODO: Remove
    // Just for testing
    public CollisionType ct;
    public Face face;

    public Collision(Vector3f point, Vector3f normal, PhysicsObject normalObj, PhysicsObject otherObj, Face face) {
        this.point = new Vector3f(point);
        this.normal = new Vector3f(normal);
        this.normalObj = normalObj;
        this.otherObj = otherObj;
        this.face = face;

        Vector3f newPoint = new Vector3f(point).add(normal);
        if (normalObj.position.distance(newPoint) < normalObj.position.distance(point)) {
            this.normal.mul(-1.0f);
        }
    }

    public void collisionResponse(PhysicsObject thisPhObj, Vector3d dVA, Vector3d dVB, Vector3d dWA, Vector3d dWB) {
        if (determineCollisionType(otherObj).equals(CollisionType.Colliding)) {
            if (thisPhObj == normalObj) {
                collidingCollisionResponse(dVA, dVB, dWA, dWB);
            } else {
                collidingCollisionResponse(dVB, dVA, dWB, dWA);
            }
        }
    }

    private CollisionType determineCollisionType(PhysicsObject thisPhysObj) {
        if (normal.dot(thisPhysObj.velocity) < -epsilon) {
            ct = CollisionType.Colliding;
            return CollisionType.Colliding;
        } else if (normal.dot(thisPhysObj.velocity) > -epsilon && normal.dot(thisPhysObj.velocity) < epsilon) {
            ct = CollisionType.Resting;
            return CollisionType.Resting;
        } else {
            ct = CollisionType.Separating;
            return CollisionType.Separating;
        }
    }

    private void collidingCollisionResponse(Vector3d dVA, Vector3d dVB, Vector3d dWA, Vector3d dWB) {
        Vector3d rA = new Vector3d(point).sub(normalObj.position);
        Vector3d rB = new Vector3d(point).sub(otherObj.position);
        Vector3d kA = new Vector3d(rA).cross(new Vector3d(normal));
        Vector3d kB = new Vector3d(rB).cross(new Vector3d(normal));
        Vector3d uA = Transforms.round(Transforms.mulVectorWithMatrix4(kA, new Matrix4d().identity().set(new Matrix3d(normalObj.inertia)).invert()), 2);
        Vector3d uB = Transforms.round(Transforms.mulVectorWithMatrix4(kB, new Matrix4d().identity().set(new Matrix3d(otherObj.inertia)).invert()), 2);

        double numerator = -(1 + normalObj.coefficientOfRestitution) * (new Vector3d(normal).dot(new Vector3d(normalObj.velocity).sub(new Vector3d(otherObj.velocity))) + (new Vector3d(normalObj.angularVelocity).dot(kA)) - new Vector3d(otherObj.angularVelocity).dot(kB));
        double denominator = (1.0 / (double) normalObj.mass) + (1.0 / (double) otherObj.mass) + kA.dot(uA) + kB.dot(uB);
        double f = numerator / denominator;
        Vector3d impulse = new Vector3d(normal).mul(f);

        dVA.add(new Vector3d(impulse).div(normalObj.mass));
        dVB.sub(new Vector3d(impulse).div(otherObj.mass));
        dWA.add(new Vector3d(uA).mul(f));
        dWB.sub(new Vector3d(uB).mul(f));
    }

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getNormal() {
        return normal;
    }

    private void fixZeros() {
        Transforms.fixZeros(point);
        Transforms.fixZeros(normal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collision collision = (Collision) o;
        fixZeros();
        collision.fixZeros();
        return point.equals(collision.point) && normal.equals(collision.normal) && normalObj.equals(collision.normalObj) && otherObj.equals(collision.otherObj);
    }

    @Override
    public int hashCode() {
        fixZeros();
        return Objects.hash(point, normal, normalObj, otherObj);
    }
}
