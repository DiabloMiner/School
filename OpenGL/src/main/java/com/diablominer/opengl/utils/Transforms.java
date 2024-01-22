package com.diablominer.opengl.utils;

import com.diablominer.opengl.io.Camera;
import org.jblas.Decompose;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.joml.*;
import org.joml.Math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Random;
import java.util.stream.Collectors;

public class Transforms {

    public static final double epsilon = 1e-16;

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

    public static void multiplyListWithMatrix(List<Vector3f> list, Matrix4f mat) {
        for (Vector3f vec : list) {
            Vector4f tempResult = new Vector4f(vec, 1.0f);
            tempResult.mul(mat);
            vec.set(tempResult.x, tempResult.y, tempResult.z);
        }
    }

    public static void multiplyListWithMatrix(List<Vector3d> list, Matrix4d mat) {
        for (Vector3d vec : list) {
            Vector4d tempResult = new Vector4d(vec, 1.0);
            tempResult.mul(mat);
            vec.set(tempResult.x, tempResult.y, tempResult.z);
        }
    }

    public static List<Vector3f> multiplyListWithMatrix2(List<Vector3f> list, Matrix4f mat) {
        for (Vector3f vec : list) {
            Vector4f tempResult = new Vector4f(vec, 1.0f);
            tempResult.mul(mat);
            vec.set(tempResult.x, tempResult.y, tempResult.z);
        }
        return list;
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

    public static Vector3d mulVectorWithMatrix4(Vector3d vec, Matrix4d mat) {
        Vector4d vec4 = new Vector4d(vec.x, vec.y, vec.z, 1.0);
        vec4.mul(mat);
        return new Vector3d(vec4.x, vec4.y, vec4.z);
    }

    public static Vector3d round(Vector3d vec, int digit) {
        for (int i = 0; i < 3; i++) {
            vec.setComponent(i, round(vec.get(i), digit));
        }
        return vec;
    }

    public static double round(double value, int digit) {
        if (value < epsilon && value > -epsilon) {
            return Math.round(value);
        } else {
            BigDecimal bigDecimal = BigDecimal.valueOf(value);
            return bigDecimal.round(new MathContext(digit, RoundingMode.HALF_DOWN)).doubleValue();
        }
        /*double result = Math.round(value * java.lang.Math.pow(10, digit));
        result /= java.lang.Math.pow(10, digit);
        return result;*/
    }

    public static double ceil(double value, int digit) {
        double result = Math.ceil(value * java.lang.Math.pow(10, digit));
        result /= java.lang.Math.pow(10, digit);
        return result;
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

    public static void swapTwoVectors(Vector3f vec1, Vector3f vec2) {
        Vector3f tempVec = new Vector3f(vec1);
        vec1.set(vec2);
        vec2.set(tempVec);
    }

    public static Vector3f safeDiv(Vector3f vec1, Vector3f vec2) {
        Vector3f result = new Vector3f(0.0f);
        for (int i = 0; i < 3; i++) {
            if (!Float.isNaN((vec1.get(i) / vec2.get(i)))) {
                result.setComponent(i, vec1.get(i) / vec2.get(i));
            }
        }
        return result;
    }

    public static Vector3d safeDiv(Vector3d vec1, Vector3f vec2) {
        Vector3d vec2d = new Vector3d(vec2);
        return Transforms.safeDiv(vec1, vec2d);
    }

    public static Vector3d safeDiv(Vector3d vec1, Vector3d vec2) {
        Vector3d result = new Vector3d(0.0);
        for (int i = 0; i < 3; i++) {
            if (!Double.isNaN((vec1.get(i) / vec2.get(i)))) {
                result.setComponent(i, vec1.get(i) / vec2.get(i));
            }
        }
        return result;
    }

    public static int getMaxComponent(Vector3d vec) {
        int maxComp = 0;
        for (int i = 0; i < 3; i++) {
            if (vec.get(i) > vec.get(maxComp)) {
                maxComp = i;
            }
        }
        return maxComp;
    }

    public static Vector3d safeNormalize(Vector3d vec) {
        Vector3d result = new Vector3d(vec);
        double invLength = Math.invsqrt(Math.fma(vec.x, vec.x, Math.fma(vec.y, vec.y, vec.z * vec.z)));
        if (Double.isNaN(invLength)) { invLength = 0.0; }
        for (int i = 0; i < 3; i++) {
            if (result.get(i) != 0.0) {
                result.setComponent(i, result.get(i) * invLength);
            }
        }
        return result;
    }

    public static double calculateDistance(Vector3d[] vecArray) {
        return calculateDistance(vecArray[0], vecArray[1]);
    }

    public static double calculateDistance(Vector3d vec1, Vector3d vec2) {
        return vec1.distance(vec2);
    }

    /**
     * Generates the vector with the highest possible amount of positive components out of the inputted vector.
     * If there is an equal number of positive and negative components, the maximum component of the result will be positive.
     */
    public static Vector3f positiveDir(Vector3f vec) {
        int numOfPos = 0;
        int numOfZeros = 0;
        int firstNonZeroComp = -1;
        for (int i = 0; i < 3; i++) {
            if (vec.get(i) > 0.0f) {
                numOfPos++;
            } else if (vec.get(i) == 0.0f) {
                numOfZeros++;
            }

            if (firstNonZeroComp == -1 && vec.get(i) != 0.0f) {
                firstNonZeroComp = i;
            }
        }

        Vector3f result = new Vector3f(vec);
        if (numOfPos == 1 && numOfZeros == 1) {
            if (vec.get(firstNonZeroComp) < 0.0f) {
                result.mul(-1.0f);
            }
        } else {
            if (numOfPos < (3 - numOfZeros)) {
                result.mul(-1.0f);
            }
        }
        return result;
    }

    /**
     * Sets -0.0fs to 0.0f for the purpose of comparing them
     * @param vec Vector which may have false zeros
     */
    public static void fixZeros(Vector3f vec) {
        for (int i = 0; i < 3; i++) {
            if (vec.get(i) == -0.0f) {
                vec.setComponent(i, 0.0f);
            }
        }
    }

    /**
     * Sets -0.0 to 0.0 for the purpose of comparing vectors/numbers
     * @param vec Vector which may have false zeros
     */
    public static Vector3d fixZeros(Vector3d vec) {
        Vector3d result = new Vector3d(vec);
        for (int i = 0; i < 3; i++) {
            if (result.get(i) == -0.0) {
                result.setComponent(i, 0.0);
            }
        }
        return result;
    }

    /**
     * Sets -0.0s to 0.0 for the purpose of comparing them
     * @param matrix Matrix which may have false zeros
     */
    public static DoubleMatrix fixZeros(DoubleMatrix matrix) {
        for (int i = 0; i < matrix.data.length; i++) {
            if (matrix.get(i) == -0.0) {
                matrix.put(i, 0.0);
            }
        }
        return matrix;
    }

    public static ArrayList<Integer> createPrefilledList(int numberOfElements, int value) {
        int[] array = new int[numberOfElements];
        Arrays.fill(array, value);
        return Arrays.stream(array).boxed().collect(Collectors.toCollection(ArrayList::new));
    }

    public static DoubleMatrix jomlMatrixToJBLASMatrix(Matrix4d mat) {
        return new DoubleMatrix(4, 4, mat.get(new double[16]));
    }

    public static DoubleMatrix jomlMatrixToJBLASMatrix(Matrix3d mat) {
        return new DoubleMatrix(3, 3, mat.get(new double[9]));
    }

    public static DoubleMatrix jomlVectorToJBLASVector(Vector3d vec) {
        return new DoubleMatrix(3, 1, vec.x, vec.y, vec.z);
    }

    public static DoubleMatrix jomlVectorToJBLASVector(Vector4d vec) {
        DoubleMatrix result = new DoubleMatrix(4, 1);
        for (int i = 0; i < 4; i++) {
            result.put(i, 0, vec.get(i));
        }
        return result;
    }

    public static DoubleMatrix jomlVectorsToJBLASVector(Vector3d[] vecs) {
        DoubleMatrix result = new DoubleMatrix(vecs.length * 3, 1);
        for (int i = 0; i < vecs.length; i++) {
            result.put(i * 3, 0, vecs[i].get(0));
            result.put(i * 3 + 1, 0, vecs[i].get(1));
            result.put(i * 3 + 2, 0, vecs[i].get(2));
        }
        return result;
    }

    public static DoubleMatrix jomlQuaternionToJBLASVector(Quaterniond q) {
        return new DoubleMatrix(4, 1, q.x(), q.y(), q.z(), q.w());
    }

    public static Vector3d jblasVectorToJomlVector(DoubleMatrix doubleMatrix) {
        assert doubleMatrix.length == 3 : "The JBLAS vector provided to Transforms is too long/short to be converted to a 3d Joml vector";
        return new Vector3d(doubleMatrix.get(0), doubleMatrix.get(1), doubleMatrix.get(2));
    }

    public static Quaterniond jblasVectorToJomlQuaternion(DoubleMatrix doubleMatrix) {
        assert doubleMatrix.length == 4 : "The JBLAS vector provided to Transforms is too long/short to be converted to a Joml quaternion";
        return new Quaterniond(doubleMatrix.get(0), doubleMatrix.get(1), doubleMatrix.get(2), doubleMatrix.get(3));
    }

    /**
     * Based on 'Contact and Friction Simulation for Computer Graphics' (SIGGRAPH 2022), p. 37.
     * Generates a matrix that is used to map angular velocity (vector) into the change of orientation (quaternion).
     */
    public static DoubleMatrix createHMatrix(Quaterniond q) {
        double[][] data = new double[][] {
                {-q.y(), -q.z(), -q.w()},
                {q.x(), q.w(), -q.z()},
                {-q.w(), q.x(), q.y()},
                {q.z(), -q.y(), q.x()}
        };
        DoubleMatrix result = new DoubleMatrix(data);
        result.muli(0.5);
        return result;
    }

    /**
     * Based on 'Contact and Friction Simulation for Computer Graphics' (SIGGRAPH 2022), p. 17.
     */
    public static Matrix3d crossProductMatrix(Vector3d r) {
        return new Matrix3d(0.0, r.z, -r.y, -r.z, 0.0, r.x, r.y, -r.x, 0.0);
    }

    public static Matrix3d createCrossProductMatrix(Vector3d vec) {
        Matrix3d result = new Matrix3d().scaling(0.0);
        result.set(1, 0, -vec.z).set(2, 0, vec.y).set(0, 1, vec.z).set(2, 1, -vec.x).set(0, 2, -vec.y).set(1, 2, vec.x);
        return result;
    }

    public static DoubleMatrix createCrossProductMatrix(DoubleMatrix vec) {
        double x = vec.get(0), y = vec.get(1), z = vec.get(2);
        return new DoubleMatrix(new double[][]{{0, -z, y}, {z, 0.0, -x}, {-y, x, 0.0}});
    }

    public static int[] createIndexArray(int size) {
        int[] indices = new int[size];
        for (int i = 0; i < size; i++) {
            indices[i] = i;
        }
        return indices;
    }

    public static int[] createIndexArray(int startingIndex, int size) {
        int[] indices = new int[size];
        for (int i = startingIndex; i < (startingIndex + size); i++) {
            indices[i - startingIndex] = i;
        }
        return indices;
    }

    public static DoubleMatrix createVectorFromArray(double[] values) {
        DoubleMatrix vec = new DoubleMatrix(values.length, 1);
        for (int i = 0; i < values.length; i++) {
            vec.put(i, 0, values[i]);
        }
        return vec;
    }

    public static DoubleMatrix strictLowerTriangular(DoubleMatrix mat) {
        DoubleMatrix lower = mat.dup();
        for (int i = 0; i < lower.rows; i++) {
            for (int j = 0; j < lower.columns; j++) {
                if (j >= i) {
                    lower.put(i, j, 0.0);
                }
            }
        }
        return lower;
    }

    public static DoubleMatrix strictUpperTriangular(DoubleMatrix mat) {
        DoubleMatrix upper = mat.dup();
        for (int i = 0; i < upper.rows; i++) {
            for (int j = 0; j < upper.columns; j++) {
                if (j <= i) {
                    upper.put(i, j, 0.0);
                }
            }
        }
        return upper;
    }

    public static void putVectorIndexed(DoubleMatrix vec1, DoubleMatrix vec2, List<Integer> indices) {
        int[] array = indices.stream().mapToInt(i -> i).toArray();
        vec1.put(array, new int[] {0}, Transforms.getVectorComponents(vec2, indices));
    }

    public static DoubleMatrix getVectorComponents(DoubleMatrix vec, List<Integer> indices) {
        DoubleMatrix result = new DoubleMatrix(indices.size(), 1);
        for (int i = 0; i < indices.size(); i++) {
            result.put(i, vec.get(indices.get(i)));
        }
        return result;
    }

    public static DoubleMatrix getMatrixComponents(DoubleMatrix mat, List<Integer> rowIndices, List<Integer> columnIndices) {
        DoubleMatrix result = new DoubleMatrix(rowIndices.size(), columnIndices.size());
        for (int i = 0; i < rowIndices.size(); i++) {
            for (int j = 0; j < columnIndices.size(); j++) {
                result.put(i, j, mat.get(rowIndices.get(i), columnIndices.get(j)));
            }
        }
        return result;
    }

    public static int kroneckerDelta(int i, int j) {
        return i == j ? 1 : 0;
    }

    public static DoubleMatrix round(DoubleMatrix mat, int digit) {
        for (int i = 0; i < mat.length; i++) {
            mat.put(i, Transforms.round(mat.get(i), digit));
        }
        return mat;
    }

    public static double[] solveQuadraticEquation(double a, double b, double c, int digitAccuracy, int roundingDigit) {
        double[] solutions = new double[2];
        MathContext context = new MathContext(digitAccuracy);
        BigDecimal bigA = BigDecimal.valueOf(a), bigB = BigDecimal.valueOf(b), bigC = BigDecimal.valueOf(c), bigTwo = BigDecimal.valueOf(2.0), bigFour = BigDecimal.valueOf(4.0);
        BigDecimal discriminant = bigB.multiply(bigB, context).subtract(bigA.multiply(bigC, context).multiply(bigFour, context), context);
        BigDecimal divisor = bigA.multiply(bigTwo, context);
        if (discriminant.doubleValue() > 0 && divisor.doubleValue() != 0.0) {
            BigDecimal x1 = (bigB.negate(context).add(discriminant.sqrt(context), context)).divide(divisor, context);
            BigDecimal x2 = (bigB.negate(context).subtract(discriminant.sqrt(context), context)).divide(divisor, context);
            solutions[0] = Transforms.round(x1.doubleValue(), roundingDigit);
            solutions[1] = Transforms.round(x2.doubleValue(), roundingDigit);
        } else if (discriminant.doubleValue() == 0 && divisor.doubleValue() != 0.0) {
            BigDecimal x1 = (bigB.negate(context).add(discriminant.sqrt(context), context)).divide(divisor, context);
            solutions[0] = Transforms.round(x1.doubleValue(), roundingDigit);
            solutions[1] = Transforms.round(x1.doubleValue(), roundingDigit);
        } else {
            solutions[0] = Double.NaN;
            solutions[1] = Double.NaN;
        }
        return solutions;
    }

    public static double[] solveQuadraticEquation(double a, double b, double c, int roundingDigit) {
        double[] solutions = new double[2];
        double discriminant = b * b - 4 * a * c;
        if (discriminant > 0.0) {
            double discriminantRoot = Math.sqrt(discriminant);
            solutions[0] = Transforms.round((-b + discriminantRoot) / (2.0 * a), roundingDigit);
            solutions[1] = Transforms.round((-b - discriminantRoot) / (2.0 * a), roundingDigit);
        } else if (discriminant == 0.0) {
            solutions[0] = Transforms.round((-b) / (2.0 * a), roundingDigit);
            solutions[0] = solutions[1];
        } else {
            solutions[0] = Double.NaN;
            solutions[1] = Double.NaN;
        }
        return solutions;
    }

    public static double chooseSuitableSolution(double min, double max, double standardReturnValue, double[] solutions) {
        for (double solution : solutions) {
            if (solution <= max && solution >= min) {
                return solution;
            }
        }
        return standardReturnValue;
    }

    public static float nonzeroSignum(float number) {
        float signum = Math.signum(number);
        return signum == 0.0f ? 1.0f : signum;
    }

}
