package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.Objects;

public class AABB implements CollisionShape {

    public Vector3d max, min, localMax, localMin;

    public AABB(Matrix4d worldMatrix, Vector3d max, Vector3d min) {
        Matrix4d inverseWorldMatrix = worldMatrix.invert(new Matrix4d());
        this.max = max;
        this.min = min;
        this.localMax = Transforms.mulVectorWithMatrix4(max, inverseWorldMatrix);
        this.localMin = Transforms.mulVectorWithMatrix4(min, inverseWorldMatrix);
    }

    public AABB(Matrix4d worldMatrix, Vector3d centerOfMass, double edgeLength) {
        Matrix4d inverseWorldMatrix = worldMatrix.invert(new Matrix4d());
        this.max = new Vector3d(centerOfMass).add(new Vector3d(edgeLength / 2.0)).add(localMax);
        this.min = new Vector3d(centerOfMass).sub(new Vector3d(edgeLength / 2.0)).add(localMin);
        this.localMax = Transforms.mulVectorWithMatrix4(max, inverseWorldMatrix);
        this.localMin = Transforms.mulVectorWithMatrix4(min, inverseWorldMatrix);
    }

    @Override
    public void update(Matrix4d worldMatrix) {
        this.max.set(Transforms.mulVectorWithMatrix4(localMax, worldMatrix));
        this.min.set(Transforms.mulVectorWithMatrix4(localMin, worldMatrix));
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
        return new Vector3d(Double.NaN);
    }

    @Override
    public Vector3d[] findClosestPoints(CollisionShape shape) {
        return new Vector3d[0];
    }

    @Override
    public Vector3d getSupportingPoint(Vector3d direction) {
        Vector3d center = new Vector3d(max).add(min).div(2.0);
        double edgeLengthX = Math.abs(max.x - center.x);
        double edgeLengthY = Math.abs(max.y - center.y);
        double edgeLengthZ = Math.abs(max.z - center.z);
        return center.add(new Vector3d(Math.signum(direction.x) * edgeLengthX, Math.signum(direction.y) * edgeLengthY, Math.signum(direction.z) * edgeLengthZ));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AABB aabb = (AABB) o;
        return localMax.equals(aabb.localMax) && localMin.equals(aabb.localMin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(localMax, localMin);
    }

}
