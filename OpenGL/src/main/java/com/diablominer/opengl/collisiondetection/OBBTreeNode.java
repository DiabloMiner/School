package com.diablominer.opengl.collisiondetection;

import org.joml.*;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;

public class OBBTreeNode {

    private Vector3f centerPoint;
    private Vector3f[] sideDirectionVectors = new Vector3f[3];
    private Vector3f[] halfLengthVectors = new Vector3f[3];

    private QuickHull quickHull;
    private List<Vector3f> points;

    public static void main(String[] args) {
        new OBBTreeNode(null);
    }

    public OBBTreeNode(List<Vector3f> points) {
        /*this.points = points;
        quickHull = new QuickHull(points);

        Matrix3f covarianceMatrix = new Matrix3f(computeCovarianceMatrixValue(0, 0), computeCovarianceMatrixValue(1, 0), computeCovarianceMatrixValue(2, 0),
                                                 computeCovarianceMatrixValue(0, 1), computeCovarianceMatrixValue(1, 1), computeCovarianceMatrixValue(2, 1),
                                                 computeCovarianceMatrixValue(0, 2), computeCovarianceMatrixValue(1, 2), computeCovarianceMatrixValue(2, 2));*/
        System.out.println(qrAlgorithm(new Matrix3f(1, 3, 0, 3, 2, 6, 0, 6, 5)));
        // TODO: Implement Obbtree, see if covariance matrix is actually symmetric and remove the main in this class
    }

    private float computeCovarianceMatrixValue(int i, int j) {
        float finalValue = 0.0f;
        for (Face face : quickHull.getFaces()) {
            Vector3f centroid = face.getCentroid();
            Vector3f vertex0 = face.getDefiningVertices().get(0);
            Vector3f vertex1 = face.getDefiningVertices().get(1);
            Vector3f vertex2 = face.getDefiningVertices().get(2);
            finalValue += (face.getArea() / 12.0f) * ((9.0f * centroid.get(i) * centroid.get(j) +
                    vertex0.get(i) * vertex0.get(j) + vertex1.get(i) * vertex1.get(j) + vertex2.get(i) * vertex2.get(j))
                    - quickHull.getCentroid().get(i) * quickHull.getCentroid().get(j));
        }
        return (finalValue / quickHull.getArea());
    }

    private Matrix3f qrAlgorithm(Matrix3f initialMatrix) {
        List<Matrix3d> matrices = new ArrayList<>();
        matrices.add(new Matrix3d(initialMatrix));
        boolean hasConverged = false;
        int iteration = 0;
        while (!hasConverged) {
            Matrix3d currentMatrix = matrices.get(matrices.size() - 1);
            Matrix2d partialMatrix = new Matrix2d(currentMatrix.m11, currentMatrix.m12, currentMatrix.m21, currentMatrix.m22);
            double nearestEigenValue = determineNearestEigenValue(partialMatrix, partialMatrix.m11);
            Matrix3d toBeDecomposedMatrix = new Matrix3d(currentMatrix).sub(new Matrix3d().identity().scale(nearestEigenValue));
            Matrix3d[] qrMatrices = qrDecomposition(toBeDecomposedMatrix);
            matrices.add(new Matrix3d(qrMatrices[0]).mul(qrMatrices[1]).add(new Matrix3d().identity().scale(nearestEigenValue)));

            Matrix3d newestMatrix = matrices.get(matrices.size() - 1);
            if (newestMatrix.m01 <= 1.0e-10 && newestMatrix.m02 <= 1.0e-10 && newestMatrix.m10 <= 1.0e-10 && newestMatrix.m12 <= 1.0e-10 && newestMatrix.m20 <= 1.0e-10 && newestMatrix.m21 <= 1.0e-10) {
                hasConverged = true;
            }
            iteration++;
        }
        Matrix3d lastMatrix = matrices.get(matrices.size() - 1);
        return new Matrix3f((float) lastMatrix.m00, (float) lastMatrix.m01, (float) lastMatrix.m02,
                            (float) lastMatrix.m10, (float) lastMatrix.m11, (float) lastMatrix.m12,
                            (float) lastMatrix.m20, (float) lastMatrix.m21, (float) lastMatrix.m22);
    }

    private double determineNearestEigenValue(Matrix2d matrix, double referenceValue) {
        double[] eigenValues = determineEigenValues(matrix);
        double nearestValue = referenceValue + 10000.0;
        for (double eigenValue : eigenValues) {
            if (Math.abs(referenceValue - eigenValue) < Math.abs(referenceValue - nearestValue) ) {
                nearestValue = eigenValue;
            }
        }
        return nearestValue;
    }

    private double[] determineEigenValues(Matrix2d matrix) {
        double[] result = new double[2];
        double constantTerm = Math.sqrt(java.lang.Math.pow(matrix.m00, 2) - 2.0 * matrix.m00 * matrix.m11 + 4.0 * matrix.m01 * matrix.m10 + java.lang.Math.pow(matrix.m11, 2));
        result[0] = 0.5 * (-1.0 * constantTerm + matrix.m00 + matrix.m11);
        result[1] = 0.5 * (constantTerm + matrix.m00 + matrix.m11);
        return result;
    }

    private Matrix3d[] qrDecomposition(Matrix3d matrix) {
        Vector3d[] a = {new Vector3d(0.0), new Vector3d(0.0), new Vector3d(0.0)};
        matrix.getColumn(0, a[0]);
        matrix.getColumn(1, a[1]);
        matrix.getColumn(2, a[2]);
        Vector3d[] u = {new Vector3d(0.0), new Vector3d(0.0), new Vector3d(0.0)};
        Vector3d[] e = new Vector3d[3];
        for (int i = 0; i < 3; i++) {
            u[i] = calculateU(a, u, i);
            if (!u[i].equals(0.0, 0.0, 0.0)) {
                e[i] = new Vector3d(u[i]).normalize();
            } else {
                e[i] = u[i];
            }
        }
        Matrix3d Q = new Matrix3d(e[0], e[1], e[2]);
        Matrix3d R = new Matrix3d(e[0].dot(a[0]), 0.0, 0.0,
                                  e[0].dot(a[1]), e[1].dot(a[1]), 0.0,
                                  e[0].dot(a[2]), e[1].dot(a[2]), e[2].dot(a[2]));
        return new Matrix3d[] {R, Q};
    }

    private Vector3d project(Vector3d a, Vector3d u) {
        double dotProduct = new Vector3d(u).dot(a) / new Vector3d(u).dot(u);
        return new Vector3d(u).mul(dotProduct);
    }

    private Vector3d calculateU(Vector3d[] a, Vector3d[] u, int k) {
        Vector3d result = new Vector3d(0.0);
        for (int j = 0; j <= (k - 1); j++) {
            result.add(project(a[k], u[j]));
        }
        return new Vector3d(a[k]).sub(result);
    }


}
