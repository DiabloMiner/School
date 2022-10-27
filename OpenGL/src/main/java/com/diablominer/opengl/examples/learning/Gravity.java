package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

import java.util.AbstractMap;
import java.util.Map;

public class Gravity implements Force {

    public static final double accelerationConstant = 9.81;

    @Override
    public boolean isFulfilled(PhysicsComponent physicsComponent) {
        return true;
    }

    @Override
    public Map.Entry<Vector3d, Vector3d> applyForce(PhysicsComponent physicsComponent) {
        return new AbstractMap.SimpleEntry<>(new Vector3d(0.0, -accelerationConstant, 0.0).mul(physicsComponent.mass), new Vector3d(0.0));
    }

}
