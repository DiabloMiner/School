package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

import java.util.Map;

public interface Force {

    boolean isFulfilled(PhysicsComponent physicsComponent);

    Map.Entry<Vector3d, Vector3d> applyForce(PhysicsComponent physicsComponent);

}
