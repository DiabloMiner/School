package com.diablominer.opengl.collisiondetection;

import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

public interface BoundingVolume {

    Set<BoundingVolume> allBoundingVolumes = new HashSet<>();

    boolean isIntersecting(BoundingVolume bv);

    void update(Vector3f translation);

    Vector3f getCenter();

}
