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
        Matrix3d initialMatrix = new Matrix3d(12, 6, -4, -51, 167, 24, 4, -68, -41);
        List<Matrix3d> matrices = givensRotation(initialMatrix);
        Matrix3d Q = new Matrix3d(matrices.get(0)).transpose();
        for (int i = 1; i < matrices.size(); i++) {
            Q.mul(new Matrix3d(matrices.get(i)).transpose());
        }
        Collections.reverse(matrices);
        Matrix3d R = new Matrix3d(matrices.get(0));
        for (int i = 1; i < matrices.size(); i++) {
            R.mul(matrices.get(i));
        }
        R.mul(initialMatrix);
        System.out.println(Q);
        System.out.println(R);
        /*Matrix3f initialMatrix = new Matrix3f(1, 3, 0, 3, 2, 6, 0, 6, 5);
        Matrix3d matrix = qrAlgorithm(initialMatrix);
        System.out.println(matrix);
        System.out.println(Arrays.toString(determineEigenVectors(initialMatrix, matrix)));*/

        // Debug givens rotations
        // Make eigenvectors work: Try to implement gaussian elimination that might work
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
            Matrix3d Q = qrMatrices[1];
            Vector3d z = new Vector3d(0.0).mul(new Matrix3d(Q).transpose());
            double zComponent = z.get(2) / R.m22;
            double yComponent = (z.get(1) - zComponent * R.m21) / R.m11;
            double xComponent = (z.get(0) - zComponent * R.m10 - yComponent * R.m20) / R.m00;
            eigenVectors[i] = new Vector3f((float) zComponent, (float) yComponent, (float) xComponent);
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
        Vector3d[] a = {new Vector3d(0.0), new Vector3d(0.0), new Vector3d(0.0)};
        matrix.getColumn(0, a[0]);
        matrix.getColumn(1, a[1]);
        matrix.getColumn(2, a[2]);
        Vector3d[] u = {new Vector3d(a[0]), new Vector3d(0.0), new Vector3d(0.0)};
        Vector3d[] e = new Vector3d[3];
        for (int i = 0; i < 3; i++) {
            u[i] = calculateU(a, u, i);
            if (!u[i].equals(0.0, 0.0, 0.0)) {
                u[i] = new Vector3d(u[i]).normalize();
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

    /*private Matrix3d givensRotation(Matrix3d matrix) {
        Matrix3d[] matrices = new Matrix3d[3];
        int[] indices = {0, 1, 0, 2, 1, 2};
        for (int i = 0; i < 6; i += 2) {
            if (matrix.get(indices[i], indices[i + 1]) != 0.0) {
                int column = indices[i];
                int row = indices[i + 1];
                if (column == 0 && row == 2) {
                    double r = java.lang.Math.hypot(matrix.m00, matrix.m02);
                    double c = matrix.m00 / r;
                    double s = -(matrix.m02 / r);
                    matrices[i / 2] = new Matrix3d(c, 0.0, s, 0.0, 1.0, 0.0, -s, 0.0, c);
                    matrix.mulLocal(matrices[i / 2]);
                } else if (column == 0 && row == 1) {
                    double r = java.lang.Math.hypot(matrix.m00, matrix.m01);
                    double c = matrix.m00 / r;
                    double s = -(matrix.m01 / r);
                    matrices[i / 2] = new Matrix3d(c, s, 0.0, -s, c, 0.0, 0.0, 0.0, 0.0);
                    matrix.mulLocal(matrices[i / 2]);
                } else if (column == 1 && row == 2) {
                    double r = java.lang.Math.hypot(matrix.m11, matrix.m12);
                    double c = matrix.m11 / r;
                    double s = -(matrix.m12 / r);
                    matrices[i / 2] = new Matrix3d(c, s, 0.0, -s, c, 0.0, 0.0, 0.0, 0.0);
                    matrix.mulLocal(matrices[i / 2]);
                }
            }
        }
        return matrix;
    }*/

    private List<Matrix3d> givensRotation(Matrix3d inputMatrix) {
        List<Matrix3d> rotationMatrices = new ArrayList<>();
        Matrix3d matrix = new Matrix3d(inputMatrix);
        int[] columns = {0, 0, 1};
        int[] rows = {1, 2, 2};
        while (matrix.m01 != 0 || matrix.m02 != 0 || matrix.m12 != 0) {
            for (int i = 0; i < 3; i++) {
                if (matrix.get(columns[i], rows[i]) != 0) {
                    double r = java.lang.Math.hypot(matrix.get(columns[i], columns[i]), matrix.get(columns[i], rows[i]));
                    double c = matrix.get(columns[i], columns[i]) / r;
                    double s = -1 * matrix.get(columns[i], rows[i]) / r;
                    rotationMatrices.add(createGivensRotationMatrix(rows[i], columns[i], c, s));
                    rotationMatrices.get(rotationMatrices.size() - 1).mul(matrix, matrix);
                    for (int column = 0; column < 3; column++) {
                        for (int row = 0; row < 3; row++) {
                            if (java.lang.Math.abs(matrix.get(column, row)) <= java.lang.Math.ulp(1.0)) {
                                matrix.set(column, row, 0.0);
                            }
                        }
                    }
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
