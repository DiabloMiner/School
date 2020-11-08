package com.diablominer.opengl.render;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transforms {

    public static Vector3f vectorToUnitVector(Vector3f vector) {
        Vector3f unitVector = new Vector3f();
        vector.div(vector.length(), unitVector);
        return unitVector;
    }

    public static Matrix4f createProjectionMatrix(float fovy, boolean givenInDegrees, double width, double height, float zNear, float zFar) {
        // If givenInDegrees is true fovy has to be given in degrees else it has to be given in radians
        fovy = givenInDegrees ? Math.toRadians(fovy) : fovy;
        Matrix4f result = new Matrix4f().identity();
        result.perspective(fovy, (float) width / (float) height, zNear, zFar);
        return result;
    }

}
