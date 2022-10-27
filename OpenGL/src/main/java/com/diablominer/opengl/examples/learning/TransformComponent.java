package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4d;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class TransformComponent implements Component {

    public Matrix4d modelMatrix;

    public TransformComponent() {
        modelMatrix = new Matrix4d();
    }

    public TransformComponent(Vector3d translation, Quaterniond rotation) {
        modelMatrix = new Matrix4d().translate(translation).rotate(rotation);
    }

    public TransformComponent(Matrix4d matrix) {
        modelMatrix = new Matrix4d(matrix);
    }

    public void update(Vector3d translation, Quaterniond rotation) {
        modelMatrix.translate(translation).rotate(rotation);
    }

    public void update(Matrix4d matrix) {
        modelMatrix.set(matrix);
    }

}
