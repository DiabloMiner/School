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
        Vector3d scaling = modelMatrix.getScale(new Vector3d());
        modelMatrix.identity().translate(translation).rotate(rotation).scale(scaling);
    }

    public void update(Matrix4d matrix) {
        Vector3d scaling = modelMatrix.getScale(new Vector3d());
        modelMatrix.identity().translate(matrix.getTranslation(new Vector3d())).rotate(matrix.getUnnormalizedRotation(new Quaterniond())).scale(scaling);
    }

}
