package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

public class AABB implements CollisionShape {

    public Vector3d max, min;

    public AABB(Vector3d max, Vector3d min) {
        this.max = max;
        this.min = min;
    }

    public AABB(Vector3d centerOfMass, double edgeLength) {
        this.max = new Vector3d(centerOfMass).add(new Vector3d(edgeLength / 2.0));
        this.min = new Vector3d(centerOfMass).sub(new Vector3d(edgeLength / 2.0));
    }

    @Override
    public void update(Vector3d deltaX) {
        this.max.add(deltaX);
        this.min.add(deltaX);
    }

    @Override
    public boolean isColliding(CollisionShape shape) {
        if (shape instanceof AABB) {
            return isColliding((AABB) shape);
        } else if (shape instanceof Sphere) {
            return isColliding((Sphere) shape);
        } else {
            return false;
        }
    }

    public boolean isColliding(AABB aabb) {
        return (this.min.x <= aabb.max.x && this.max.x >= aabb.min.x) && (this.min.y <= aabb.max.y && this.max.y >= aabb.min.y) && (this.min.z <= aabb.max.z && this.max.z >= aabb.min.z);
    }

    public boolean isColliding(Sphere sphere) {
        double x = Math.max(this.min.x, Math.min(sphere.position.x, this.max.x));
        double y = Math.max(this.min.y, Math.min(sphere.position.y, this.max.y));
        double z = Math.max(this.min.z, Math.min(sphere.position.z, this.max.z));
        return new Vector3d(x, y, z).distance(sphere.position) < sphere.radius;
    }

    @Override
    public Vector3d findPenetrationDepth(CollisionShape shape) {
        return new Vector3d(-1.0);
    }

}
