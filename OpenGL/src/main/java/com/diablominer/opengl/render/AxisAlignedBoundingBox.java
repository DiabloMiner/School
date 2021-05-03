package com.diablominer.opengl.render;

import org.joml.Math;
import org.joml.Vector3f;

public class AxisAlignedBoundingBox implements BoundingVolume {

    private Vector3f max;
    private Vector3f min;

    public AxisAlignedBoundingBox(Vector3f centerPoint, float size) {
        this.max = new Vector3f(centerPoint).add(new Vector3f(Math.sqrt(3 * size)));
        this.min = new Vector3f(centerPoint).sub(new Vector3f(Math.sqrt(3 * size)));
        BoundingVolume.allBoundingVolumes.add(this);
    }

    @Override
    public boolean isIntersecting(BoundingVolume bv) {
        if (bv.getClass() == AxisAlignedBoundingBox.class) {
            return isIntersecting(bv);
        } else {
            return false;
        }
    };

    public boolean isIntersecting(AxisAlignedBoundingBox aabb) {
        Vector3f min1 = determineMinVector();
        Vector3f max1 = determineMaxVector();
        Vector3f min2 = aabb.determineMinVector();
        Vector3f max2 = aabb.determineMaxVector();
        return (min1.x <= max2.x && max1.x >= min2.x) &&
               (min1.y <= max2.y && max1.y >= min2.y) &&
               (min1.z <= max2.z && max1.z >= min2.z);
    }

    private Vector3f determineMinVector() {
        return new Vector3f(Math.min(min.x, max.x), Math.min(min.y, max.y), Math.min(min.z, max.z));
    }

    private Vector3f determineMaxVector() {
        return new Vector3f(Math.max(min.x, max.x), Math.max(min.y, max.y), Math.max(min.z, max.z));
    }

    public Vector3f getMax() {
        return max;
    }

    public void setMax(Vector3f max) {
        this.max = max;
    }

    public Vector3f getMin() {
        return min;
    }

    public void setMin(Vector3f min) {
        this.min = min;
    }

    public void changeMax(Vector3f change) {
        this.max.add(change);
    }

    public void changeMin(Vector3f change) {
        this.min.add(change);
    }
}
