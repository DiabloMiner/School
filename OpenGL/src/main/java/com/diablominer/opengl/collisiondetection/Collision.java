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

        double coefficientOfRestitution = (normalObj.coefficientOfRestitution + otherObj.coefficientOfRestitution) / 2;

        double numerator = -(1 + coefficientOfRestitution) * (new Vector3d(normal).dot(new Vector3d(normalObj.velocity).sub(new Vector3d(otherObj.velocity))) + (new Vector3d(normalObj.angularVelocity).dot(kA)) - new Vector3d(otherObj.angularVelocity).dot(kB));
        double denominator = (1.0 / (double) normalObj.mass) + (1.0 / (double) otherObj.mass) + kA.dot(uA) + kB.dot(uB);
        double f = numerator / denominator;
        Vector3d impulse = new Vector3d(normal).mul(f);

        Vector3d normalObjFrictionImpulse = computeFrictionImpulse(normalObj);
        Vector3d otherObjFrictionImpulse = computeFrictionImpulse(otherObj);

        // TODO: Implement Coulomb friction correctly (min(vel, frictionVel))

        // Add impulses to velocities (Friction impulses are currently only added to translational velocity)
        normalObj.velocity.add(new Vector3f().set(new Vector3d(impulse).add(normalObjFrictionImpulse).div(normalObj.mass)));
        otherObj.velocity.sub(new Vector3f().set(new Vector3d(impulse).sub(otherObjFrictionImpulse).div(otherObj.mass)));
        normalObj.angularVelocity.add(new Vector3f().set(new Vector3d(uA).mul(f)));
        normalObj.angularVelocity.sub(new Vector3f().set(new Vector3d(uB).mul(f)));
    }

    private Vector3d computeFrictionImpulse(PhysicsObject physicsObject) {
        Vector3d vT = new Vector3d(physicsObject.velocity).sub(new Vector3d(physicsObject.velocity).normalize().mul(new Vector3d(normal).dot(new Vector3d(physicsObject.velocity))));
        double coefficientOfKineticFriction = (normalObj.coefficientOfKineticFriction + otherObj.coefficientOfKineticFriction) / 2;

        double frictionImpulse = new Vector3d(vT).mul(physicsObject.mass).length() * coefficientOfKineticFriction;
        Vector3d impulseByFriction = new Vector3d(vT).mul(-1.0).normalize();
        impulseByFriction.mul(frictionImpulse);
        return impulseByFriction;
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
        Vector3f posNorm = Transforms.positiveDir(normal);
        Vector3f otherPosNorm = Transforms.positiveDir(normal);
        return point.equals(collision.point) && posNorm.equals(otherPosNorm);
    }

    @Override
    public int hashCode() {
        fixZeros();
        Vector3f positiveNormal = Transforms.positiveDir(normal);
        return Objects.hash(point, positiveNormal);
    }
}
