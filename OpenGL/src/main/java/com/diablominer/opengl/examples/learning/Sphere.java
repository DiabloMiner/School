package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.Objects;

public class Sphere implements CollisionShape {

    public Vector3d position, localPosition;
    public double radius;

    public Sphere(Matrix4d worldMatrix, Vector3d position, double radius) {
        this.position = new Vector3d(position);
        this.localPosition = Transforms.mulVectorWithMatrix4(position, worldMatrix.invert(new Matrix4d()));
        this.radius = radius;
    }

    @Override
    public void update(Matrix4d worldMatrix) {
        position.set(Transforms.mulVectorWithMatrix4(localPosition, worldMatrix));
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

    @Override
    public Vector3d[] findClosestPoints(CollisionShape shape) {
        if (shape instanceof Sphere) {
            return getNearestPoints((Sphere) shape);
        } else {
            return new Vector3d[0];
        }
    }

    @Override
    public Vector3d getSupportingPoint(Vector3d direction) {
        return new Vector3d(position).add(Transforms.safeNormalize(direction).mul(radius));
    }

    public Vector3d[] getNearestPoints(Sphere sphere) {
        Vector3d dir = new Vector3d(sphere.position).sub(this.position).normalize();
        return new Vector3d[] {new Vector3d(this.position).add(new Vector3d(dir).mul(this.radius)), new Vector3d(sphere.position).sub(new Vector3d(dir).mul(sphere.radius))};
    }

    public Vector3d findPenetrationDepth(Sphere sphere) {
        double distance = Math.abs((this.radius + sphere.radius) - this.position.distance(sphere.position));
        return new Vector3d(this.position).sub(sphere.position).normalize().mul(distance);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sphere sphere = (Sphere) o;
        return Double.compare(sphere.radius, radius) == 0 && localPosition.equals(sphere.localPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localPosition, radius);
    }

}
