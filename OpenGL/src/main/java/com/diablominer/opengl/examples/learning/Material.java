package com.diablominer.opengl.examples.learning;

import java.util.HashMap;
import java.util.Map;

public enum Material {

    Ball(0),
    Cloth(1);

    public static Map<Integer, Double> coefficientsOfRestitution = new HashMap<>();
    public static Map<Integer, Double> coefficientsOfStaticFriction = new HashMap<>();
    public static Map<Integer, Double> coefficientsOfKineticFriction = new HashMap<>();
    public static Map<Integer, Double> coefficientsOfRollingFriction = new HashMap<>();
    static {
        coefficientsOfRestitution.putIfAbsent(hash(Ball, Ball), 0.95);
        coefficientsOfStaticFriction.putIfAbsent(hash(Ball, Ball), 0.055);
        coefficientsOfKineticFriction.putIfAbsent(hash(Ball, Ball), 0.055);
        coefficientsOfRollingFriction.putIfAbsent(hash(Ball, Ball), 0.0);

        coefficientsOfRestitution.putIfAbsent(hash(Ball, Cloth), 0.5);
        coefficientsOfStaticFriction.putIfAbsent(hash(Ball, Cloth), 0.055);
        coefficientsOfKineticFriction.putIfAbsent(hash(Ball, Cloth), 0.2);
        coefficientsOfRollingFriction.putIfAbsent(hash(Ball, Cloth), 0.01);
    }

    public int id;

    Material(int id) {
        this.id = id;
    }

    public static int hash(Material mat0, Material mat1) {
        return (int) Math.round(Math.pow(2, Math.min(mat0.id, mat1.id)) * Math.pow(3, Math.max(mat0.id, mat1.id)));
    }

}
