package com.diablominer.opengl.render;

import java.util.HashSet;
import java.util.Set;

public interface BoundingVolume {

    Set<BoundingVolume> allBoundingVolumes = new HashSet<>();

    boolean isIntersecting(BoundingVolume bv);

}
