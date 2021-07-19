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
    private final List<Vector3f> originalPoints;

    private final Matrix4f rotationMatrix;
    private final Vector3f translation;

    public OBBTreeNode(List<Vector3f> points) {
        // Initialize the points list and generate the quickhull
        this.points = Transforms.copyVectorList(points);
        this.originalPoints = Transforms.copyVectorList(points);
        quickHull = new QuickHull(Transforms.copyVectorList(points));

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

        // Create the matrix and vector needed for collision tests
        rotationMatrix = new Matrix4f().identity().lookAlong(sideDirectionVectors[2], sideDirectionVectors[1]);
        translation = Transforms.mulVectorWithMatrix4(centerPoint, rotationMatrix).mul(-1.0f);

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
            if (Math.abs(R.m02) <= epsilon && Math.abs(R.m12) <= epsilon && Math.abs(R.m22) <= epsilon) {
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
        if (Math.abs(initialMatrix.m01) <= epsilon && Math.abs(initialMatrix.m02) <= epsilon && Math.abs(initialMatrix.m12) <= epsilon) {
            return new Matrix3d(initialMatrix);
        } else {
            List<Matrix3d> matrices = new ArrayList<>();
            matrices.add(new Matrix3d(initialMatrix));
            while (true) {
                Matrix3d currentMatrix = matrices.get(matrices.size() - 1);
                if (Math.abs(currentMatrix.m01) <= epsilon && Math.abs(currentMatrix.m02) <= epsilon && Math.abs(currentMatrix.m12) <= epsilon) {
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
        if (givensRotationMatrices.size() != 0) {
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
            return new Matrix3d[]{R, Q};
        } else {
            return new Matrix3d[] {matrix, null};
        }
    }

    private List<Matrix3d> givensRotation(Matrix3d inputMatrix) {
        List<Matrix3d> rotationMatrices = new ArrayList<>();
        Matrix3d matrix = new Matrix3d(inputMatrix);
        int[] columns = {0, 0, 1};
        int[] rows = {1, 2, 2};
        while (Math.abs(matrix.m01) > epsilon || Math.abs(matrix.m02) > epsilon || Math.abs(matrix.m12) > epsilon) {
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

    public boolean isColliding(OBBTreeNode otherObbTreeNode, Matrix4f thisWorldMatrix, Matrix4f otherWorldMatrix) {
        Matrix4f transformationMatrix = new Matrix4f().identity().translate(translation).translate(Transforms.getTranslation(thisWorldMatrix)).translate(Transforms.getTranslation(otherWorldMatrix)).rotate(Transforms.getRotation(rotationMatrix)).rotate(Transforms.getRotation(thisWorldMatrix)).rotate(Transforms.getRotation(otherWorldMatrix));
        Vector3f translation = otherObbTreeNode.getTransformedTranslation(transformationMatrix);
        Matrix4f rotation = otherObbTreeNode.getTransformedRotation(transformationMatrix);

        Vector3f[] thisHalfLengths = getTransformedHalfLengths(transformationMatrix);
        Vector3f[] otherHalfLengths = otherObbTreeNode.getTransformedHalfLengths(transformationMatrix);

        Vector3f[] axes = getPotentialSeparatingAxes(rotation);

        for (Vector3f axis : axes) {
            float da = 0;
            float db = 0;

            for (int j = 0; j < 3; j++) {
                da += thisHalfLengths[j].length() * Math.abs(axes[j].dot(axis));
                db += otherHalfLengths[j].length() * Math.abs(axes[j + 3].dot(axis));
            }

            if (Math.abs(translation.dot(axis)) > da + db) {
                return false;
            }
        }
        return true;
    }

    private Vector3f[] getPotentialSeparatingAxes(Matrix4f rotation) {
        Vector3f[] potentialSeparatingAxes = new Vector3f[15];

        potentialSeparatingAxes[0] = new Vector3f(1.0f, 0.0f, 0.0f);
        potentialSeparatingAxes[1] = new Vector3f(0.0f, 1.0f, 0.0f);
        potentialSeparatingAxes[2] = new Vector3f(0.0f, 0.0f, 1.0f);

        potentialSeparatingAxes[3] = Transforms.getColumn(rotation, 0);
        potentialSeparatingAxes[4] = Transforms.getColumn(rotation, 1);
        potentialSeparatingAxes[5] = Transforms.getColumn(rotation, 2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                potentialSeparatingAxes[3 * i + j + 6] = new Vector3f(potentialSeparatingAxes[i]).cross(potentialSeparatingAxes[j + 3]);
            }
        }

        return potentialSeparatingAxes;
    }

    public Vector3f getTransformedTranslation(Matrix4f transformationMatrix) {
        Vector3f transformedCenterPoint = new Vector3f(centerPoint.x, centerPoint.y, centerPoint.z);
        return Transforms.mulVectorWithMatrix4(transformedCenterPoint, transformationMatrix);
    }

    public Matrix4f getTransformedRotation(Matrix4f transformationMatrix) {
        Matrix4f onlyRotation = new Matrix4f().identity().rotate(Transforms.getRotation(transformationMatrix));
        Vector3f[] transformedHalfLengthVectors = Transforms.copyVectorArray(halfLengthVectors);
        Transforms.multiplyArrayWithMatrix(transformedHalfLengthVectors, onlyRotation);

        Matrix3f rotation = new Matrix3f(transformedHalfLengthVectors[0], transformedHalfLengthVectors[1], transformedHalfLengthVectors[2]);
        return new Matrix4f().identity().set(rotation);
    }

    public Vector3f[] getTransformedHalfLengths(Matrix4f transformationMatrix) {
        Vector3f[] transformedHalfLengths = {new Vector3f(0.0f), new Vector3f(0.0f), new Vector3f(0.0f)};
        Matrix4f mat = getTransformedRotation(transformationMatrix);

        for (int i = 0; i < 3; i++) {
            mat.getColumn(i, transformedHalfLengths[i]);
        }
        return transformedHalfLengths;
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

    public List<Vector3f> getOriginalPoints() {
        return originalPoints;
    }

}
