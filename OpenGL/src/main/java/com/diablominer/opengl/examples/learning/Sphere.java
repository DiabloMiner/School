package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

public class Sphere implements CollisionShape {

    public Vector3d position;
    public double radius;

    public Sphere(Vector3d position, double radius) {
        this.position = new Vector3d(position);
        this.radius = radius;
    }

    @Override
    public void update(Vector3d deltaX) {
        position.add(deltaX);
    }

    @Override
    public boolean isColliding(CollisionShape shape) {
        if (shape instanceof Sphere) {
            return isColliding((Sphere) shape);
        } else if (shape instanceof AABB) {
            return isColliding((AABB) shape);
        } else {
            return false;
        }
    }

    public boolean isColliding(Sphere sphere) {
        return this.position.distance(sphere.position) <= (this.radius + sphere.radius);
    }

    public boolean isColliding(AABB aabb) {
        double x = Math.max(aabb.min.x, Math.min(this.position.x, aabb.max.x));
        double y = Math.max(aabb.min.y, Math.min(this.position.y, aabb.max.y));
        double z = Math.max(aabb.min.z, Math.min(this.position.z, aabb.max.z));
        return new Vector3d(x, y, z).distance(this.position) < this.radius;
    }

    @Override
    public Vector3d findPenetrationDepth(CollisionShape shape) {
        if (shape instanceof Sphere) {
            return findPenetrationDepth((Sphere) shape);
        } else {
            return new Vector3d(Double.NaN);
        }
    }

    public Vector3d findPenetrationDepth(Sphere sphere) {
        double distance = (this.radius + sphere.radius) - this.position.distance(sphere.position);
        return new Vector3d(this.position).sub(sphere.position).normalize().mul(distance);
    }

}
