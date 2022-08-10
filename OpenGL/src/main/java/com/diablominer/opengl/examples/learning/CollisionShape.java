package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

public interface CollisionShape {

    // TODO: Add rotation support
    void update(Vector3d deltaX);

    boolean isColliding(CollisionShape shape);

    Vector3d findPenetrationDepth(CollisionShape shape);

}
