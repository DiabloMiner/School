package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.utils.Transforms;
import org.joml.*;
import org.joml.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        Matrix3f initialMatrix = new Matrix3f(1, 3, 0, 3, 2, 6, 0, 6, 5);
        Matrix3d matrix = qrAlgorithm(initialMatrix);
        System.out.println(matrix);
        System.out.println(Arrays.toString(determineEigenVectors(initialMatrix, matrix)));

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

    private Vector3f[] determineEigenVectors(Matrix3f initialMatrix, Matrix3d matrixFromQRAlgorithm) {
        Matrix3d[] matrices = {new Matrix3d(initialMatrix).sub(new Matrix3d().identity().scale(matrixFromQRAlgorithm.m00)),
                               new Matrix3d(initialMatrix).sub(new Matrix3d().identity().scale(matrixFromQRAlgorithm.m11)),
                               new Matrix3d(initialMatrix).sub(new Matrix3d().identity().scale(matrixFromQRAlgorithm.m22))};
        Vector3f[] eigenVectors = new Vector3f[3];
        for (int i = 0; i < 3; i++) {
            Matrix3d[] qrMatrices = qrDecomposition(matrices[i]);
            Matrix3d R = qrMatrices[0];
            Vector3d z = new Vector3d(0.0);
            double zComponent;
            if (R.m02 <= 1e-10 && R.m12 <= 1e-10 && R.m22 <= 1e-10) {
                zComponent = 1.0;
            } else {
                zComponent = z.get(2) / R.m22;
            }
            double yComponent = (z.get(1) - zComponent * R.m21) / R.m11;
            double xComponent = (z.get(0) - yComponent * R.m10 - zComponent * R.m20) / R.m00;
            eigenVectors[i] = new Vector3f((float) xComponent, (float) yComponent, (float) zComponent);
        }
        return eigenVectors;
    }

    private Matrix3d qrAlgorithm(Matrix3f initialMatrix) {
        List<Matrix3d> matrices = new ArrayList<>();
        matrices.add(new Matrix3d(initialMatrix));
        boolean hasConverged = false;
        while (!hasConverged) {
            Matrix3d currentMatrix = matrices.get(matrices.size() - 1);
            Matrix2d partialMatrix = new Matrix2d(currentMatrix.m11, currentMatrix.m12, currentMatrix.m21, currentMatrix.m22);
            double nearestEigenValue = determineNearestEigenValue(partialMatrix, partialMatrix.m11);
            Matrix3d toBeDecomposedMatrix = new Matrix3d(currentMatrix).sub(new Matrix3d().identity().scale(nearestEigenValue));
            Matrix3d[] qrMatrices = qrDecomposition(toBeDecomposedMatrix);
            matrices.add(new Matrix3d(qrMatrices[0]).mul(qrMatrices[1]).add(new Matrix3d().identity().scale(nearestEigenValue)));

            Matrix3d newestMatrix = matrices.get(matrices.size() - 1);
            double change = Math.abs(Transforms.arithmeticMeanOfMatrix(newestMatrix) - Transforms.arithmeticMeanOfMatrix(matrices.get(matrices.size() - 2)));
            if (change < 1e-10) {
                hasConverged = true;
            }
        }
        return matrices.get(matrices.size() - 1);
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
        List<Matrix3d> givensRotationMatrices = givensRotation(matrix);
        Matrix3d Q = new Matrix3d(givensRotationMatrices.get(0)).transpose();
        for (int i = 1; i < givensRotationMatrices.size(); i++) {
            Q.mul(new Matrix3d(givensRotationMatrices.get(i)).transpose());
        }
        Collections.reverse(givensRotationMatrices);
        Matrix3d R = new Matrix3d(givensRotationMatrices.get(0));
        for (int i = 1; i < givensRotationMatrices.size(); i++) {
            R.mul(givensRotationMatrices.get(i));
        }
        R.mul(matrix);
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

    private List<Matrix3d> givensRotation(Matrix3d inputMatrix) {
        List<Matrix3d> rotationMatrices = new ArrayList<>();
        Matrix3d matrix = new Matrix3d(inputMatrix);
        int[] columns = {0, 0, 1};
        int[] rows = {1, 2, 2};
        double epsilon = java.lang.Math.ulp(1.0);
        while (matrix.m01 > epsilon || matrix.m02 > epsilon || matrix.m12 > epsilon) {
            for (int i = 0; i < 3; i++) {
                if (matrix.get(columns[i], rows[i]) != 0) {
                    double r = java.lang.Math.hypot(matrix.get(columns[i], columns[i]), matrix.get(columns[i], rows[i]));
                    double c = matrix.get(columns[i], columns[i]) / r;
                    double s = -1 * matrix.get(columns[i], rows[i]) / r;
                    rotationMatrices.add(createGivensRotationMatrix(rows[i], columns[i], c, s));
                    rotationMatrices.get(rotationMatrices.size() - 1).mul(matrix, matrix);
                }
            }
        }
        return rotationMatrices;
    }

    private Matrix3d createGivensRotationMatrix(int i, int k, double c, double s) {
        Matrix3d result = new Matrix3d();
        for (int l = 0; l < 3; l++) {
            for (int j = 0; j < 3; j++) {
                if ((j == i && l == i) || (j == k && l == k)) {
                    result.set(l, j, c);
                } else if (j == i && l == k) {
                    result.set(l, j, s);
                } else if (j == k && l == i) {
                    result.set(l, j, (-1.0 * s));
                } else if (j == l && (l != i && j != i) && j != k) {
                    result.set(l, j, 1.0);
                } else {
                    result.set(l, j, 0.0);
                }
            }
        }
        return result;
    }


}
