package com.diablominer.opengl.examples.learning;

import org.joml.Matrix3d;
import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Collection;

public class PhysicsBox extends StandardPhysicsComponent {

    /**
     * @param measurements Measurements is the vector containing the width, depth and height of the box in this order
     */
    public PhysicsBox(Matrix4d worldMatrix, Vector3d halfLengths, Vector3d measurements, Material material, ObjectType objectType, Vector3d velocity, Vector3d angularVelocity, Collection<Force> forces, double mass) {
        super(material, objectType, new OBB(worldMatrix, halfLengths), worldMatrix.getTranslation(new Vector3d()), velocity, worldMatrix.getNormalizedRotation(new Quaterniond()), angularVelocity, new Matrix3d().m00((1.0 / 12.0) * mass * (measurements.z * measurements.z + measurements.y * measurements.y)).m11((1.0 / 12.0) * mass * (measurements.x * measurements.x + measurements.z * measurements.z)).m22((1.0 / 12.0) * mass * (measurements.x * measurements.x + measurements.y * measurements.y)), forces, mass, halfLengths.length());
    }

}
