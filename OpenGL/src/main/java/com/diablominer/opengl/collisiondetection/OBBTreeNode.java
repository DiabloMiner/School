package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.utils.Transforms;
import org.joml.*;
import org.joml.Math;

import java.util.*;

public class OBBTreeNode {

    private static final double epsilon = java.lang.Math.ulp(1.0f);

    private final Vector3f centerPoint;
    private final Vector3f[] sideDirectionVectors;
    private final Vector3f[] halfLengthVectors = new Vector3f[3];
    private final Vector3f[] orderedDirectionVectors = new Vector3f[3];

    private final QuickHull quickHull;
    private final List<Vector3f> points;

    private final Matrix4f rotationMatrix;
    private final Matrix4f translationMatrix;

    public OBBTreeNode(List<Vector3f> points) {
        // Initialize the points list and generate the quickhull
        this.points = points;
        quickHull = new QuickHull(points);

        // Compute the parameters of the OBB from the covariance matrix and the extremes along the axes
        Matrix3f covarianceMatrix = new Matrix3f(computeCovarianceMatrixValue(0, 0), computeCovarianceMatrixValue(0, 1), computeCovarianceMatrixValue(0, 2),
                                                 computeCovarianceMatrixValue(1, 0), computeCovarianceMatrixValue(1, 1), computeCovarianceMatrixValue(1, 2),
                                                 computeCovarianceMatrixValue(2, 0), computeCovarianceMatrixValue(2, 1), computeCovarianceMatrixValue(2, 2));
        Matrix3d qrMatrix = qrAlgorithm(covarianceMatrix);
        sideDirectionVectors = determineEigenVectors(covarianceMatrix, qrMatrix);
        for (Vector3f sideDirectionVector : sideDirectionVectors) {
            sideDirectionVector.normalize();
        }
        float[] extremes = computeExtremesAlongTheAxes(quickHull.getPoints());
        halfLengthVectors[0] = new Vector3f(sideDirectionVectors[0]).mul(extremes[1] - extremes[0]).mul(0.5f);
        halfLengthVectors[1] = new Vector3f(sideDirectionVectors[1]).mul(extremes[3] - extremes[2]).mul(0.5f);
        halfLengthVectors[2] = new Vector3f(sideDirectionVectors[2]).mul(extremes[5] - extremes[4]).mul(0.5f);
        centerPoint = new Vector3f(sideDirectionVectors[0]).mul(extremes[0] + extremes[1]).mul(0.5f).add(new Vector3f(sideDirectionVectors[1]).mul(extremes[2] + extremes[3]).mul(0.5f)).add(new Vector3f(sideDirectionVectors[2]).mul(extremes[4] + extremes[5]).mul(0.5f));

        // Transform all points into a coordinate system where the centerPoint is the origin
        rotationMatrix = new Matrix4f().identity().lookAlong(sideDirectionVectors[2], sideDirectionVectors[1]);
        Transforms.multiplyArrayWithMatrixAndSetPositive(sideDirectionVectors, rotationMatrix, (float) epsilon);
        Transforms.multiplyArrayWithMatrixAndSetPositive(halfLengthVectors, rotationMatrix, (float) epsilon);

        translationMatrix = new Matrix4f(rotationMatrix).translation(new Vector3f(centerPoint).mul(-1.0f));
        Transforms.multiplyListWithMatrix(points, translationMatrix, (float) epsilon);

        // TODO: Implement Obbtree
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

    private float[] computeExtremesAlongTheAxes(List<Vector3f> points) {
        return new float[] {computeMinAlongAAxis(points, sideDirectionVectors[0]), computeMaxAlongAAxis(points, sideDirectionVectors[0]),
                               computeMinAlongAAxis(points, sideDirectionVectors[1]), computeMaxAlongAAxis(points, sideDirectionVectors[1]),
                               computeMinAlongAAxis(points, sideDirectionVectors[2]), computeMaxAlongAAxis(points, sideDirectionVectors[2])};
    }

    private float computeMaxAlongAAxis(List<Vector3f> points, Vector3f sideDirectionVector) {
        Vector3f max = new Vector3f(sideDirectionVector).mul(-10000.0f);
        for (Vector3f point : points) {
            if (sideDirectionVector.dot(point) > sideDirectionVector.dot(max)) {
                max = point;
            }
        }
        return sideDirectionVector.dot(max);
    }

    private float computeMinAlongAAxis(List<Vector3f> points, Vector3f sideDirectionVector) {
        Vector3f min = new Vector3f(sideDirectionVector).mul(10000.0f);
        for (Vector3f point : points) {
            if (sideDirectionVector.dot(point) < sideDirectionVector.dot(min)) {
                min = point;
            }
        }
        return sideDirectionVector.dot(min);
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
            if (R.m02 <= epsilon && R.m12 <= epsilon && R.m22 <= epsilon) {
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
        if (initialMatrix.m01 <= epsilon && initialMatrix.m02 <= epsilon && initialMatrix.m12 <= epsilon) {
            return new Matrix3d(initialMatrix);
        } else {
            List<Matrix3d> matrices = new ArrayList<>();
            matrices.add(new Matrix3d(initialMatrix));
            while (true) {
                Matrix3d currentMatrix = matrices.get(matrices.size() - 1);
                if (currentMatrix.m01 <= epsilon && currentMatrix.m02 <= epsilon && currentMatrix.m12 <= epsilon) {
                    break;
                }

                Matrix2d partialMatrix = new Matrix2d(currentMatrix.m11, currentMatrix.m12, currentMatrix.m21, currentMatrix.m22);
                double nearestEigenValue = determineNearestEigenValue(partialMatrix, partialMatrix.m11);
                Matrix3d toBeDecomposedMatrix = new Matrix3d(currentMatrix).sub(new Matrix3d().identity().scale(nearestEigenValue));
                Matrix3d[] qrMatrices = qrDecomposition(toBeDecomposedMatrix);
                matrices.add(new Matrix3d(qrMatrices[0]).mul(qrMatrices[1]).add(new Matrix3d().identity().scale(nearestEigenValue)));
            }
            return matrices.get(matrices.size() - 1);
        }
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

    private List<Matrix3d> givensRotation(Matrix3d inputMatrix) {
        List<Matrix3d> rotationMatrices = new ArrayList<>();
        Matrix3d matrix = new Matrix3d(inputMatrix);
        int[] columns = {0, 0, 1};
        int[] rows = {1, 2, 2};
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

    public boolean isColliding(OBBTreeNode otherObbTreeNode) {
        return false;
    }

    public Vector3f getLongestAxis() {
        if (orderedDirectionVectors[0] == null) {
            Optional<Vector3f> result = Arrays.stream(halfLengthVectors).max((o1, o2) -> Float.compare(o1.length(), o2.length()));
            if (result.isPresent()) {
                orderedDirectionVectors[0] = result.get();
            } else {
                throw new Error("The optional storing the longest axis of the OBBTreeNode doesn't have a value.");
            }
        }
        return orderedDirectionVectors[0];
    }

    public Vector3f getMiddleAxis() {
        if (orderedDirectionVectors[1] == null) {
            ArrayList<Vector3f> remainingHalfLengthVectors = new ArrayList<>(Arrays.asList(halfLengthVectors));
            remainingHalfLengthVectors.remove(getLongestAxis());
            Optional<Vector3f> result = remainingHalfLengthVectors.stream().max((o1, o2) -> Float.compare(o1.length(), o2.length()));
            if (result.isPresent()) {
                orderedDirectionVectors[1] = result.get();
            } else {
                throw new Error("The optional storing the middle axis of the OBBTreeNode doesn't have a value.");
            }
        }
        return orderedDirectionVectors[1];
    }

    public Vector3f getCenterPoint() {
        return centerPoint;
    }

    public List<Vector3f> getPoints() {
        return points;
    }

}
