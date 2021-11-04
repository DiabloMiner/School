package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;


public class Collision {

    public static final float epsilon = Math.ulp(1.0f);

    private Vector3f point;
    private Vector3f normal;
    private PhysicsObject normalObj, otherObj;
    public CollisionType ct;

    public Collision(Vector3f point, Vector3f normal, PhysicsObject normalObj, PhysicsObject otherObj) {
        this.point = new Vector3f(point);
        this.normal = new Vector3f(normal);
        this.normalObj = normalObj;
        this.otherObj = otherObj;

        Vector3f newPoint = new Vector3f(point).add(normal);
        if (normalObj.position.distance(newPoint) < normalObj.position.distance(point)) {
            this.normal.mul(-1.0f);
        }
    }

    public void collisionResponse(PhysicsObject thisPhysObj, PhysicsObject otherPhysObj) {
        if (determineCollisionType(otherObj).equals(CollisionType.Colliding)) {
            collidingCollisionResponse(normalObj, otherObj);
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

    private void collidingCollisionResponse(PhysicsObject thisPhysObj, PhysicsObject otherPhysObj) {
        Vector3f rA = new Vector3f(point).sub(thisPhysObj.position);
        Vector3f rB = new Vector3f(point).sub(otherPhysObj.position);
        Vector3f kA = new Vector3f(rA).cross(normal);
        Vector3f kB = new Vector3f(rB).cross(normal);
        Vector3f uA = Transforms.mulVectorWithMatrix4(kA, new Matrix4f().identity().set(thisPhysObj.inertia).invert());
        Vector3f uB = Transforms.mulVectorWithMatrix4(kB, new Matrix4f().identity().set(otherPhysObj.inertia).invert());

        double numerator = -(1 + thisPhysObj.coefficientOfRestitution) * (normal.dot(new Vector3f(thisPhysObj.velocity).sub(otherPhysObj.velocity)) + (thisPhysObj.angularVelocity.dot(kA)) - otherPhysObj.angularVelocity.dot(kB));
        double denominator = (1.0 / thisPhysObj.mass) + (1.0 / otherPhysObj.mass) + kA.dot(uA) + kB.dot(uB);
        double f = numerator / denominator;
        Vector3f impulse = new Vector3f(normal).mul((float) f);

        thisPhysObj.velocity.add(new Vector3f(impulse).div(thisPhysObj.mass));
        otherPhysObj.velocity.sub(new Vector3f(impulse).div(otherPhysObj.mass));
        thisPhysObj.angularVelocity.add(Transforms.mulVectorWithMatrix4(new Vector3f(rA).cross(impulse), new Matrix4f(thisPhysObj.getInertia()).invert()));
        otherPhysObj.angularVelocity.sub(Transforms.mulVectorWithMatrix4(new Vector3f(rB).cross(impulse), new Matrix4f(otherPhysObj.getInertia()).invert()));
    }

    public Vector3f getPoint() {
        return point;
    }

    public Vector3f getNormal() {
        return normal;
    }

}
