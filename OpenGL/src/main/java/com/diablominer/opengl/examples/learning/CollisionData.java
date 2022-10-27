package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

public class CollisionData {

    public Vector3d position, direction;
    public double timeStepTaken;

    public CollisionData(Vector3d position, Vector3d direction, double timeStepTaken) {
        this.position = position;
        this.direction = direction;
        this.timeStepTaken = timeStepTaken;
    }

    public Vector3d[] getTangentialDirections() {
        if (direction.cross(new Vector3d(0.0, 0.0, 1.0), new Vector3d()).isFinite()) {
            Vector3d tangentialDir = direction.cross(new Vector3d(0.0, 0.0, 1.0), new Vector3d()).normalize();
            Vector3d tangentialDir2 = direction.cross(tangentialDir, new Vector3d()).normalize();
            return new Vector3d[] {tangentialDir, tangentialDir2};
        } else {
            Vector3d tangentialDir = direction.cross(new Vector3d(1.0), new Vector3d()).normalize();
            Vector3d tangentialDir2 = direction.cross(tangentialDir, new Vector3d()).normalize();
            return new Vector3d[] {tangentialDir, tangentialDir2};
        }
    }

}
