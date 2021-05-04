package com.diablominer.opengl.main;

import com.diablominer.opengl.collisiondetection.BoundingVolume;
import org.joml.Vector3f;

import java.util.Set;

public abstract class PhysicsObject implements GameObject {

    public PhysicsObject() {
        LogicalEngine.allPhysicsObjects.add(this);
    };

    public BoundingVolume bv;
    public float mass;
    public Vector3f velocity;

    public abstract void collisionDetectionAndResponse(Set<PhysicsObject> physicsObjects);

}
