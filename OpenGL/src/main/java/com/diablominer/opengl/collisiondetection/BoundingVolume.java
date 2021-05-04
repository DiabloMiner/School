package com.diablominer.opengl.collisiondetection;

import java.util.HashSet;
import java.util.Set;

public interface BoundingVolume {

    Set<BoundingVolume> allBoundingVolumes = new HashSet<>();

    boolean isIntersecting(BoundingVolume bv);

}
