package com.diablominer.opengl.utils;

import com.diablominer.opengl.io.Camera;
import org.joml.*;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;

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

    public static Vector3f createNormalVectorWithTwoComponents(Vector3f initialVector, float x, float y) {
        float z = (-1 * initialVector.x * x - initialVector.y * y) / initialVector.z;
        return new Vector3f(x, y, z);
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

    public static Vector3f getQuotientOf2Vectors(Vector3f vec1, Vector3f vec2) {
        Vector3f product = new Vector3f();
        vec1.div(vec2, product);
        return product;
    }

    public static Matrix4f createProjectionMatrix(float fovy, boolean givenInDegrees, float aspect, float zNear, float zFar) {
        // If givenInDegrees is true fovy has to be given in degrees else it has to be given in radians
        fovy = givenInDegrees ? Math.toRadians(fovy) : fovy;
        Matrix4f result = new Matrix4f().identity();
        result.perspective(fovy, aspect, zNear, zFar);
        return result;
    }

    public static Matrix4f createViewMatrix(Camera camera) {
        return new Matrix4f().identity().lookAt(camera.position, camera.getLookAtPosition(), camera.up);
    }

    public static Matrix4f getProductOf2Matrices(Matrix4f matrix1, Matrix4f matrix2) {
        Matrix4f result = new Matrix4f().identity();
        matrix1.mul(matrix2, result);
        return result;
    }

    public static double arithmeticMeanOfMatrix(Matrix3d matrix) {
        return (matrix.m00 + matrix.m01 + matrix.m02 + matrix.m10 + matrix.m11 + matrix.m12 + matrix.m20 + matrix.m21 + matrix.m22) / 9.0;
    }

    public static void checkForComponentsSmallerThanEpsilon(Vector3f vec, float epsilon) {
        if (Math.abs(vec.x) <= epsilon) {
            vec.set(0.0f, vec.y, vec.z);
        }
        if (Math.abs(vec.y) <= epsilon) {
            vec.set(vec.x, 0.0f, vec.z);
        }
        if (Math.abs(vec.z) <= epsilon) {
            vec.set(vec.x, vec.y, 0.0);
        }
    }

    public static void checkIfVecComponentIsNegativeAndNegate(Vector3f vec) {
        if (vec.x < 0.0f) {
            vec.set(-1.0f * vec.x, vec.y, vec.z);
        } else if (vec.y < 0.0f) {
            vec.set(vec.x, -1.0f * vec.y, vec.z);
        } else {
            vec.set(vec.x, vec.y, -1.0f * vec.z);
        }
    }

    public static void multiplyListWithMatrix(List<Vector3f> list, Matrix4f mat, float epsilon) {
        for (Vector3f vec : list) {
            Vector4f tempResult = new Vector4f(vec, 1.0f);
            tempResult.mul(mat);
            vec.set(tempResult.x, tempResult.y, tempResult.z);
            Transforms.checkForComponentsSmallerThanEpsilon(vec, epsilon);
        }
    }

    public static void multiplyArrayWithMatrix(Vector3f[] array, Matrix4f mat) {
        for (Vector3f vec : array) {
            Vector4f tempResult = new Vector4f(vec, 1.0f);
            tempResult.mul(mat);
            vec.set(tempResult.x, tempResult.y, tempResult.z);
        }
    }

    public static void multiplyArrayWithMatrixAndSetPositive(Vector3f[] array, Matrix4f mat, float epsilon) {
        for (Vector3f vec : array) {
            Vector4f tempResult = new Vector4f(vec, 1.0f);
            tempResult.mul(mat);
            vec.set(tempResult.x, tempResult.y, tempResult.z);
            Transforms.checkForComponentsSmallerThanEpsilon(vec, epsilon);
            checkIfVecComponentIsNegativeAndNegate(vec);
        }
    }

    public static Vector3f mulVectorWithMatrix4(Vector3f vec, Matrix4f mat) {
        Vector4f vec4 = new Vector4f(vec.x, vec.y, vec.z, 1.0f);
        vec4.mul(mat);
        return new Vector3f(vec4.x, vec4.y, vec4.z);
    }

    public static Vector3f copyVector(Vector3f vec) {
        return new Vector3f(vec.x, vec.y, vec.z);
    }

    public static List<Vector3f> copyVectorList(List<Vector3f> list) {
        List<Vector3f> result = new ArrayList<>();
        for (Vector3f vec : list) {
            result.add(copyVector(vec));
        }
        return result;
    }

    public static Vector3f[] copyVectorArray(Vector3f[] array) {
        Vector3f[] result = new Vector3f[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = copyVector(array[i]);
        }
        return result;
    }

    public static Quaternionf getRotation(Matrix4f mat) {
        Quaternionf result = new Quaternionf();
        mat.getUnnormalizedRotation(result);
        return result;
    }

    public static Vector3f getTranslation(Matrix4f mat) {
        Vector3f result = new Vector3f();
        mat.getTranslation(result);
        return result;
    }

    public static Quaternionf getInvRotation(Matrix4f mat) {
        Quaternionf result = new Quaternionf();
        new Matrix4f(mat).invert().getUnnormalizedRotation(result);
        return result;
    }

    public static Vector3f getInvTranslation(Matrix4f mat) {
        Vector3f result = new Vector3f();
        new Matrix4f(mat).invert().getTranslation(result);
        return result;
    }

    public static Vector3f getColumn(Matrix4f mat, int column) {
        Vector3f col = new Vector3f(0.0f);
        mat.getColumn(column, col);
        return col;
    }

}
