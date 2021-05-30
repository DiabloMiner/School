package com.diablominer.opengl.collisiondetection;

import org.joml.*;
import org.joml.Math;

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
        int size = 100;
        Matrix3d[] matrices = new Matrix3d[size];
        matrices[0] = new Matrix3d(initialMatrix);
        for (int i = 0; i < (size - 1); i++) {
            Matrix2d partialMatrix = new Matrix2d(matrices[i].m11, matrices[i].m12, matrices[i].m21, matrices[i].m22);
            double nearestEigenValue = determineNearestEigenValue(partialMatrix, partialMatrix.m11);
            Matrix3d toBeDecomposedMatrix = new Matrix3d(matrices[i]).sub(new Matrix3d().identity().scale(nearestEigenValue));
            Matrix3d[] qrMatrices = qrDecomposition(toBeDecomposedMatrix);
            matrices[i + 1] = new Matrix3d(qrMatrices[0]).mul(qrMatrices[1]).add(new Matrix3d().identity().scale(nearestEigenValue));
        }
        return new Matrix3f((float) matrices[size - 1].m00, (float) matrices[size - 1].m01, (float) matrices[size - 1].m02,
                            (float) matrices[size - 1].m10, (float) matrices[size - 1].m11, (float) matrices[size - 1].m12,
                            (float) matrices[size - 1].m20, (float) matrices[size - 1].m21, (float) matrices[size - 1].m22);
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
