package com.diablominer.opengl.utils;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transforms {

    public static Vector3f vectorToUnitVector(Vector3f vector) {
        Vector3f unitVector = new Vector3f();
        vector.div(vector.length(), unitVector);
        return unitVector;
    }
    public static Vector3f vectorToUnitVector(float x, float y, float z) {
        Vector3f unitVector = new Vector3f(x, y, z);
        unitVector.div(unitVector.length(), unitVector);
        return unitVector;
    }

    public static Vector3f getSumOf2Vectors(Vector3f vec1, Vector3f vec2) {
        Vector3f sum = new Vector3f();
        vec1.add(vec2, sum);
        return sum;
    }

    public static Vector3f getProductOf2Vectors(Vector3f vec1, Vector3f vec2) {
        Vector3f product = new Vector3f();
        vec1.mul(vec2, product);
        return product;
    }

    public static Matrix4f createProjectionMatrix(float fovy, boolean givenInDegrees, float aspect, float zNear, float zFar) {
        // If givenInDegrees is true fovy has to be given in degrees else it has to be given in radians
        fovy = givenInDegrees ? Math.toRadians(fovy) : fovy;
        Matrix4f result = new Matrix4f().identity();
        result.perspective(fovy, aspect, zNear, zFar);
        return result;
    }

}
