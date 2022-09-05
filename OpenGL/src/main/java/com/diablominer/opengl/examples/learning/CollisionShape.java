package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4d;
import org.joml.Vector3d;

public interface CollisionShape {

    void update(Matrix4d worldMatrix);

    boolean isColliding(CollisionShape shape);

    Vector3d findPenetrationDepth(CollisionShape shape);

    Vector3d[] findClosestPoints(CollisionShape shape);

    Vector3d getSupportingPoint(Vector3d direction);

}
