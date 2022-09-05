package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

import java.util.AbstractMap;
import java.util.Map;

public class Gravity implements Force {

    public static final double accelerationConstant = 9.81;

    @Override
    public boolean isFulfilled(PhysicsObject physicsObject) {
        return true;
    }

    @Override
    public Map.Entry<Vector3d, Vector3d> applyForce(PhysicsObject physicsObject) {
        return new AbstractMap.SimpleEntry<>(new Vector3d(0.0, -accelerationConstant, 0.0).mul(physicsObject.mass), new Vector3d(0.0));
    }

}
