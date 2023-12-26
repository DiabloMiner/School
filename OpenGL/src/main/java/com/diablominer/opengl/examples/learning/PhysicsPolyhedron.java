package com.diablominer.opengl.examples.learning;
import org.joml.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PhysicsPolyhedron extends StandardPhysicsComponent {

    public PhysicsPolyhedron(AssimpModel model, Matrix4d worldMatrix, Material material, Vector3d velocity, Vector3d angularVelocity, Collection<Force> forces, double mass) {
        super(material, new Polyhedron(worldMatrix, model), new Vector3d(0.0), velocity, worldMatrix.getNormalizedRotation(new Quaterniond()), angularVelocity, new Matrix3d(), forces, mass, 0.0);
        Set<Vector3d> modelVertices = new HashSet<>(model.getAllVerticesInWorldCoordinates(worldMatrix));
        this.position.set(computeCenterOfMass(modelVertices));
        this.bodyFrameInertia.set(computeInertiaMatrix(modelVertices, mass));
        if (mass < massThreshold  && !Double.isInfinite(mass)) {
            Matrix3d rotationMatrix = worldMatrix.get3x3(new Matrix3d());
            this.worldFrameInertia.set(new Matrix3d(rotationMatrix).mul(bodyFrameInertia).mul(rotationMatrix.transpose(new Matrix3d())));
            this.worldFrameInertiaInv.set(worldFrameInertia.invert(new Matrix3d()));
        } else {
            this.worldFrameInertia.set(new Matrix3d(bodyFrameInertia));
            this.worldFrameInertiaInv.set(new Matrix3d().scale(0.0));
        }
        this.radius = findRadius(modelVertices);
    }

    public Vector3d computeCenterOfMass(Collection<Vector3d> modelVertices) {
        Vector3d centerOfMass = new Vector3d();
        modelVertices.forEach(centerOfMass::add);
        centerOfMass.div(modelVertices.size());
        return centerOfMass;
    }

    public Matrix3d computeInertiaMatrix(Set<Vector3d> modelVertices, double mass) {
        Matrix3d inertiaMatrix = new Matrix3d().zero();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double elementSum = 0.0;
                for (Vector3d vertex : modelVertices) {
                    double r = vertex.distance(position);
                    elementSum += (r * r * kroneckerDelta(i, j)) - vertex.get(i) * vertex.get(j);
                }
                inertiaMatrix.set(j, i, elementSum);
            }
        }
        inertiaMatrix.scale(mass / modelVertices.size());
        return inertiaMatrix;
    }

    private int kroneckerDelta(int i, int j) {
        return i == j ? 1 : 0;
    }

    public double findRadius(Collection<Vector3d> modelVertices) {
        double radius = 0.0;
        for (Vector3d vertex : modelVertices) {
            if (vertex.distance(position) > radius) {
                radius = vertex.distance(position);
            }
        }
        return radius;
    }

}
