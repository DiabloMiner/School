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

}
