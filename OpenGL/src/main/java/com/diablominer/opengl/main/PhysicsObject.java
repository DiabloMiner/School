package com.diablominer.opengl.main;

import com.diablominer.opengl.collisiondetection.BoundingVolume;
import com.diablominer.opengl.collisiondetection.OBBTree;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Set;

public abstract class PhysicsObject implements GameObject {

    public PhysicsObject() {
        LogicalEngine.allPhysicsObjects.add(this);
    };

    public BoundingVolume bv;
    public OBBTree obbTree;

    public float coefficientOfRestitution;
    public float mass;
    public Matrix3f inertia;
    public Vector3f position;
    public Vector3f velocity;
    public Vector3f force;
    public Quaternionf orientation;
    public Vector3f angularVelocity;
    public Vector3f torque;
    public Matrix4f modelMatrix;

    public abstract void collide(Set<PhysicsObject> physicsObjects);

    public abstract Matrix3f getInertia();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicsObject that = (PhysicsObject) o;
        return Float.compare(that.coefficientOfRestitution, coefficientOfRestitution) == 0 && Float.compare(that.mass, mass) == 0 && bv.equals(that.bv) && obbTree.equals(that.obbTree) && inertia.equals(that.inertia) && position.equals(that.position) && velocity.equals(that.velocity) && force.equals(that.force) && orientation.equals(that.orientation) && angularVelocity.equals(that.angularVelocity) && torque.equals(that.torque) && modelMatrix.equals(that.modelMatrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bv, obbTree, coefficientOfRestitution, mass, inertia, position, velocity, force, orientation, angularVelocity, torque, modelMatrix);
    }
}
