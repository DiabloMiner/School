package com.diablominer.opengl.examples.learning;

import org.joml.*;

import java.util.Set;

public class PhysicsSphere extends StandardPhysicsComponent {

    public PhysicsSphere(Material material, Vector3d position, Vector3d velocity, Quaterniond orientation, Vector3d angularVelocity, Set<Force> forces, double mass, double radius) {
        super(material, new Sphere(new Matrix4d().translate(position).rotate(orientation), position, radius), position, velocity, orientation, angularVelocity, new Matrix3d().identity().scaling((2.0 / 5.0) * mass * radius * radius), forces, mass, radius);
    }

}
