package com.diablominer.opengl.examples.learning;

import java.util.HashMap;
import java.util.Map;

public enum Material {

    Inelastic(0),
    Elastic(1),
    Ball(2),
    Cloth(3),
    Rail(4);

    public static Map<Integer, Double> coefficientsOfRestitution = new HashMap<>();
    public static Map<Integer, Double> coefficientsOfStaticFriction = new HashMap<>();
    public static Map<Integer, Double> coefficientsOfKineticFriction = new HashMap<>();
    public static Map<Integer, Double> coefficientsOfRollingFriction = new HashMap<>();
    static {
        for (Material material : Material.values()) {
            coefficientsOfRestitution.putIfAbsent(hash(Inelastic, material), 0.0);
            coefficientsOfStaticFriction.putIfAbsent(hash(Inelastic, material), 0.0);
            coefficientsOfKineticFriction.putIfAbsent(hash(Inelastic, material), 0.0);
            coefficientsOfRollingFriction.putIfAbsent(hash(Inelastic, material), 0.0);

            coefficientsOfRestitution.putIfAbsent(hash(Elastic, material), 1.0);
            coefficientsOfStaticFriction.putIfAbsent(hash(Elastic, material), 0.0);
            coefficientsOfKineticFriction.putIfAbsent(hash(Elastic, material), 0.0);
            coefficientsOfRollingFriction.putIfAbsent(hash(Elastic, material), 0.0);
        }

        coefficientsOfRestitution.putIfAbsent(hash(Ball, Ball), 0.95);
        coefficientsOfStaticFriction.putIfAbsent(hash(Ball, Ball), 0.005);
        coefficientsOfKineticFriction.putIfAbsent(hash(Ball, Ball), 0.005);
        coefficientsOfRollingFriction.putIfAbsent(hash(Ball, Ball), 0.0);

        coefficientsOfRestitution.putIfAbsent(hash(Ball, Cloth), 0.5);
        coefficientsOfStaticFriction.putIfAbsent(hash(Ball, Cloth), 0.055);
        coefficientsOfKineticFriction.putIfAbsent(hash(Ball, Cloth), 0.2);
        coefficientsOfRollingFriction.putIfAbsent(hash(Ball, Cloth), 0.01);

        coefficientsOfRestitution.putIfAbsent(hash(Ball, Rail), 0.98);
        coefficientsOfStaticFriction.putIfAbsent(hash(Ball, Rail), 0.0);
        coefficientsOfKineticFriction.putIfAbsent(hash(Ball, Rail), 0.14);
        coefficientsOfRollingFriction.putIfAbsent(hash(Ball, Rail), 0.0);
    }

    public int id;

    Material(int id) { this.id = id; }

    public static int hash(Material mat0, Material mat1) {
        return (int) Math.round(Math.pow(2, Math.min(mat0.id, mat1.id)) * Math.pow(3, Math.max(mat0.id, mat1.id)));
    }

}
