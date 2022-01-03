package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.lang.Math;
import java.util.Objects;


public class Collision {

    public static final float epsilon = Math.ulp(1.0f);

    private final Vector3f point;
    private final Vector3f normal;
    private final PhysicsObject normalObj, otherObj;
    private final Face face;

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

    public Collision(Vector3f point, Vector3f normal, PhysicsObject normalObj, PhysicsObject otherObj) {
        this.point = new Vector3f(point);
        this.normal = new Vector3f(normal);
        this.normalObj = normalObj;
        this.otherObj = otherObj;
        this.face = null;

        Vector3f newPoint = new Vector3f(point).add(normal);
        if (normalObj.position.distance(newPoint) < normalObj.position.distance(point)) {
            this.normal.mul(-1.0f);
        }
    }

    public void collisionResponse() {
        if (determineCollisionType().equals(CollisionType.Colliding)) {
            collidingCollisionResponse();
        }
    }

    private CollisionType determineCollisionType() {
        float relativeVelocity = normal.dot((new Vector3f(otherObj.velocity).add(new Vector3f(otherObj.angularVelocity).cross(new Vector3f(point).sub(otherObj.position)))).sub(new Vector3f(normalObj.velocity).add(new Vector3f(normalObj.angularVelocity).cross(new Vector3f(point).sub(normalObj.position)))));
        if (relativeVelocity < -epsilon) {
            return CollisionType.Colliding;
        } else if (relativeVelocity > -epsilon && relativeVelocity < epsilon) {
            return CollisionType.Resting;
        } else {
            return CollisionType.Separating;
        }
    }

    private void collidingCollisionResponse() {
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

        normalObj.velocity.add(new Vector3f().set(new Vector3d(impulse).div(normalObj.mass)));
        otherObj.velocity.sub(new Vector3f().set(new Vector3d(impulse).div(otherObj.mass)));
        normalObj.angularVelocity.add(new Vector3f().set(new Vector3d(uA).mul(f)));
        normalObj.angularVelocity.sub(new Vector3f().set(new Vector3d(uB).mul(f)));
    }

    public boolean isColliding() {
        return determineCollisionType().equals(CollisionType.Colliding);
    }

    public boolean isResting() {
        return determineCollisionType().equals(CollisionType.Resting);
    }

    public boolean isSeparating() {
        return determineCollisionType().equals(CollisionType.Separating);
    }

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public Face getFace() {
        return face;
    }

    public PhysicsObject getNormalObj() {
        return normalObj;
    }

    public PhysicsObject getOtherObj() {
        return otherObj;
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
