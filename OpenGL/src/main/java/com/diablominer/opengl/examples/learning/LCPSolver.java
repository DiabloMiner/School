package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;
import org.jblas.Solve;
import org.joml.Matrix3d;
import org.joml.Vector3d;

import java.util.*;

public class LCPSolver {

    public static Map<Integer, DoubleMatrix> solvedCollisions = new HashMap<>();

    private LCPSolver() {}

    public static void gaussSeidel(DoubleMatrix A, DoubleMatrix b, DoubleMatrix x, DoubleMatrix lo, DoubleMatrix hi, int maxIter) {
        int n = x.getRows();

        double sum;
        while (maxIter > 0) {
            for (int i = 0; i < n; i++) {
                sum = b.get(i);
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum = sum - (A.get(i, j)) * x.get(j);
                    }
                }
                if (A.get(i, i) != 0.0) {
                    x.put(i, sum / A.get(i, i));
                } else {
                    // This is a hacky solution to solve ill conditioned systems
                    x.put(i, 0.0);
                }
            }

            for (int i = 0; i < n; i++) {
                if (!lo.isEmpty()) {
                    assert lo.getColumns() ==1;
                    assert lo.getRows() == n;
                    if (x.get(i) < lo.get(i)) x.put(i, lo.get(i));
                }
                if (!hi.isEmpty()) {
                    assert hi.getColumns() ==1;
                    assert hi.getRows() == n;
                    if (x.get(i) > hi.get(i)) x.put(i, hi.get(i));
                }
            }


            maxIter--;
        }
    }

    /*private DoubleMatrix initializeX(int rows) {
        DoubleMatrix x0 = new DoubleMatrix(rows, 1);
        for (int i = 0; i < rows; i++) {
            x0.put(i, 0, Math.random() * 10.0);
        }
        return x0;
    }

    private DoubleMatrix initializeX(int rows, double value) {
        DoubleMatrix x0 = new DoubleMatrix(rows, 1);
        for (int i = 0; i < rows; i++) {
            x0.put(i, 0, value);
        }
        return x0;
    }

    private DoubleMatrix initializeX(DoubleMatrix b) {
        return b.dup();
    }

    private DoubleMatrix constructH(DoubleMatrix x, DoubleMatrix y, DoubleMatrix u, DoubleMatrix l) {
        DoubleMatrix H = new DoubleMatrix(x.rows, 1);
        for (int i = 0; i < x.rows; i++) {
            H.put(i, Math.min(u.get(i) - x.get(i), Math.max(-y.get(i), l.get(i) - x.get(i))));
        }
        return H;
    }

    private DoubleMatrix constructH(DoubleMatrix x, DoubleMatrix y) {
        DoubleMatrix H = new DoubleMatrix(x.rows, 1);
        for (int i = 0; i < x.rows; i++) {
            H.put(i, 0, Math.min(x.get(i, 0), y.get(i, 0)));
        }
        return H;
    }

    private DoubleMatrix constructHAndIntegerSets(DoubleMatrix x, DoubleMatrix y, DoubleMatrix u, DoubleMatrix l, Collection<Integer> A, Collection<Integer> J) {
        DoubleMatrix H = new DoubleMatrix(x.rows, 1);
        for (int i = 0; i < x.rows; i++) {
            H.put(i, Math.max(x.get(i) - u.get(i), Math.min(y.get(i), x.get(i) - l.get(i))));
            if (x.get(i) - l.get(i) >= y.get(i) && y.get(i) >= x.get(i) - u.get(i)) {
                A.add(i);
            } else {
                J.add(i);
            }
        }
        return H;
    }

    private DoubleMatrix constructHAndIntegerSets(DoubleMatrix x, DoubleMatrix y, Collection<Integer> A, Collection<Integer> F) {
        DoubleMatrix H = new DoubleMatrix(x.rows, 1);
        for (int i = 0; i < x.rows; i++) {
            H.put(i, 0, Math.min(x.get(i, 0), y.get(i, 0)));
            if (y.get(i) < x.get(i)) {
                A.add(i);
            } else {
                F.add(i);
            }
        }
        return H;
    }

    private DoubleMatrix[] constructBHParts(DoubleMatrix A, DoubleMatrix x, DoubleMatrix l, DoubleMatrix u, DoubleMatrix y, DoubleMatrix mu, List<Integer> setA, List<Integer> setJ) {
        DoubleMatrix C = constructC(A, x, l, u, y, mu, setA, setJ);
        DoubleMatrix D = constructD(x, l, y, mu, setJ);
        DoubleMatrix DInv = D.dup().sub(DoubleMatrix.diag(D.diag())).neg().add(DoubleMatrix.diag(D.diag()));
        return new DoubleMatrix[] {C, D, DInv};
    }

    private DoubleMatrix constructC(DoubleMatrix A, DoubleMatrix x, DoubleMatrix l, DoubleMatrix u, DoubleMatrix y, DoubleMatrix mu, List<Integer> setA, List<Integer> setJ) {
        DoubleMatrix C = new DoubleMatrix(setJ.size(), setA.size());
        for (int i = 0; i < setJ.size(); i++) {
            for (int j = 0; j < setA.size(); j++) {
                if (y.get(i) > (x.get(i) - l.get(i))) {
                    C.put(i, j, Transforms.kroneckerDelta(setJ.get(i), setA.get(j)) + mu.get(i) * Transforms.kroneckerDelta(0, setA.get(j)));
                } else if ((x.get(i) - u.get(i)) > y.get(i)) {
                    C.put(i, j, Transforms.kroneckerDelta(setJ.get(i), setA.get(j)) - mu.get(i) * Transforms.kroneckerDelta(0, setA.get(j)));
                } else {
                    C.put(i, j, A.get(i, j));
                }
            }
        }
        return C;
    }

    private DoubleMatrix constructD(DoubleMatrix x, DoubleMatrix l, DoubleMatrix y, DoubleMatrix mu, List<Integer> setJ) {
        DoubleMatrix D = new DoubleMatrix(setJ.size(), setJ.size());
        for (int i = 0; i < setJ.size(); i++) {
            for (int j = 0; j < setJ.size(); j++) {
                if (y.get(i) > x.get(i) - l.get(i)) {
                    D.put(i, j, Transforms.kroneckerDelta(setJ.get(i), setJ.get(j)) + mu.get(i) * Transforms.kroneckerDelta(0, setJ.get(j)));
                } else {
                    D.put(i, j, Transforms.kroneckerDelta(setJ.get(i), setJ.get(j)) - mu.get(i) * Transforms.kroneckerDelta(0, setJ.get(j)));
                }
            }
        }
        return D;
    }

    private DoubleMatrix constructDInv(DoubleMatrix x, DoubleMatrix l, DoubleMatrix y, DoubleMatrix mu, List<Integer> setJ) {
        DoubleMatrix D = constructD(x, l, y, mu, setJ);
        return D.dup().sub(DoubleMatrix.diag(D.diag())).neg().add(DoubleMatrix.diag(D.diag()));
    }

    private double computePhi(DoubleMatrix H) {
        return H.dot(H) * 0.5;
    }

    private double computeDeltaPhi(DoubleMatrix H) {
        return H.dot(H) * -1.0;
    }

    private double computePhi(DoubleMatrix A, DoubleMatrix b, DoubleMatrix x) {
        DoubleMatrix H = constructH(x, A.mmul(x).add(b));
        return H.dot(H) * 0.5;
    }

    private double computePhi(DoubleMatrix A, DoubleMatrix b, DoubleMatrix x, DoubleMatrix u, DoubleMatrix l) {
        DoubleMatrix H = constructH(x, A.mmul(x).add(b), u, l);
        return H.dot(H) * 0.5;
    }

    private double projectedLineSearch(DoubleMatrix A, DoubleMatrix b, DoubleMatrix H, DoubleMatrix x, DoubleMatrix deltaX, double alpha, double beta, double delta, int iterations) {
        double meritValue0 = computePhi(H);
        double deltaMeritValue0 = computeDeltaPhi(H);
        double tau = 1;
        DoubleMatrix xTau = new DoubleMatrix(x.rows, 1);
        for (int j = 0; j < iterations; j++) {
            DoubleMatrix precomputedX = x.dup().add(deltaX.dup().mul(tau));
            for (int i = 0; i < xTau.rows; i++) {
                xTau.put(i, Math.max(0.0, precomputedX.get(i)));
            }
            double meritValue = computePhi(A, b, xTau);
            if (meritValue <= (meritValue0 + alpha * tau * deltaMeritValue0)) {
                break;
            }
            if (tau <= delta) {
                break;
            }
            tau *= beta;
        }
        return tau;
    }

    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    private double projectedLineSearch(DoubleMatrix A, DoubleMatrix b, DoubleMatrix H, DoubleMatrix x, DoubleMatrix deltaX, DoubleMatrix u, DoubleMatrix l, double mu, double muR, double alpha, double beta, double delta, int iterations) {
        double meritValue0 = computePhi(H);
        double deltaMeritValue0 = computeDeltaPhi(H);
        double tau = 1;
        DoubleMatrix xTau = new DoubleMatrix(x.rows, 1), precomputedX = x.dup().add(deltaX), uTau = u.dup(), lTau = l.dup();
        for (int j = 0; j < iterations; j++) {
            for (int i = 0; i < xTau.rows; i++) {
                xTau.put(i, Math.max(0.0, precomputedX.get(i) - (1 - tau) * deltaX.get(i)));
            }
            setBounds(uTau, lTau, xTau.get(0), mu, muR);
            double meritValue = computePhi(A, b, xTau, uTau, lTau);
            if (meritValue <= (meritValue0 + alpha * tau * deltaMeritValue0)) {
                break;
            }
            if (tau <= delta) {
                break;
            }
            tau *= beta;
        }
        return tau;
    }

    private double computeNewtonMeritValue(DoubleMatrix H) {
        return H.dot(H) * 0.5;
    }

    public LCPSolverResult solveLCPWithMinimumMapNewton(DoubleMatrix A, DoubleMatrix b, double alpha, double beta, double delta, double epsilonAbsolute, double epsilonRelative, int iterations, int lineSearchIterations, int roundingDigit) {
        return solveLCPWithMinimumMapNewton(new DoubleMatrix(b.rows, 1).fill(0.0), A, b, alpha, beta, delta, epsilonAbsolute, epsilonRelative, iterations, lineSearchIterations, roundingDigit);
    }

    public LCPSolverResult solveLCPWithMinimumMapNewton(DoubleMatrix x0, DoubleMatrix A, DoubleMatrix b, double alpha, double beta, double delta, double epsilonAbsolute, double epsilonRelative, int iterations, int lineSearchIterations, int roundingDigit) {
        DoubleMatrix x = x0.dup();
        DoubleMatrix y, H = constructH(x, A.mmul(x).add(b)), deltaX = new DoubleMatrix(b.rows, 1);
        List<Integer> setA = new ArrayList<>(), setF = new ArrayList<>();
        double currentMeritValue, previousMeritValue = computeNewtonMeritValue(H);
        for (int i = 0; i < iterations; i++) {
            // y is rounded to prevent numerical imprecision from changing the result
            y = Transforms.round(A.mmul(x).add(b), roundingDigit);
            H = constructHAndIntegerSets(x, y, setA, setF);
            Transforms.putVectorIndexed(deltaX, H.neg(), setF);
            if (!setA.isEmpty()) {
                DoubleMatrix deltaXA = Solve.pinv(Transforms.getMatrixComponents(A, setA, setA)).mmul(Transforms.getMatrixComponents(A, setA, setF).mmul(Transforms.getVectorComponents(H, setF)).sub(Transforms.getVectorComponents(H, setA)));
                deltaX.put(setA.stream().mapToInt(integer -> integer).toArray(), new int[] {0}, deltaXA);
            }
            double tau = projectedLineSearch(A, b, H, x, deltaX, alpha, beta, delta, lineSearchIterations);
            x.addi(deltaX.dup().mul(tau));

            currentMeritValue = computeNewtonMeritValue(H);
            if (currentMeritValue < epsilonAbsolute) {
                return new LCPSolverResult(x, LCPResultFlag.ABSOLUTE_CONVERGENCE);
            }
            if (Math.abs(currentMeritValue - previousMeritValue) < epsilonRelative * Math.abs(previousMeritValue)) {
                return new LCPSolverResult(x, LCPResultFlag.RELATIVE_CONVERGENCE);
            }
            previousMeritValue = currentMeritValue;
            setA.clear();
            setF.clear();
        }
        return new LCPSolverResult(x, LCPResultFlag.MAX_ITERATIONS_REACHED);
    }

    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    private void setBounds(DoubleMatrix u, DoubleMatrix l, double normalImpulse, double mu, double muR) {
        u.put(new int[] {1, 2}, new int[] {0}, mu * Math.abs(normalImpulse));
        u.put(new int[] {3, 4, 5}, new int[] {0}, muR * Math.abs(normalImpulse));
        l.put(new int[] {1, 2}, new int[] {0}, -1.0 * mu * Math.abs(normalImpulse));
        l.put(new int[] {3, 4, 5}, new int[] {0}, -1.0 * muR * Math.abs(normalImpulse));
    }


    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    public LCPSolverResult solveBLCPWithMinimumMapNewton(DoubleMatrix A, DoubleMatrix b, double mu, double muR, double alpha, double beta, double delta, double epsilonAbsolute, double epsilonRelative, int iterations, int lineSearchIterations, int roundingDigit) {
        return solveBLCPWithMinimumMapNewton(solveBLCPWithPGS(A, b, mu, muR, epsilonRelative, 2, roundingDigit).x, A, b, mu, muR, alpha, beta, delta, epsilonAbsolute, epsilonRelative, iterations, lineSearchIterations, roundingDigit);
    }


    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    public LCPSolverResult solveBLCPWithMinimumMapNewton(DoubleMatrix x0, DoubleMatrix A, DoubleMatrix b, double mu, double muR, double alpha, double beta, double delta, double epsilonAbsolute, double epsilonRelative, int iterations, int lineSearchIterations, int roundingDigit) {
        DoubleMatrix x = x0.dup(), u = new DoubleMatrix(b.rows, 1).fill(Double.POSITIVE_INFINITY), l = new DoubleMatrix(b.rows, 1).fill(0.0);
        setBounds(u, l, x.get(0), mu, muR);
        List<Integer> setA = new ArrayList<>(), setJ = new ArrayList<>();
        DoubleMatrix y = Transforms.round(A.mmul(x).add(b), roundingDigit), H = constructHAndIntegerSets(x, y, u, l, setA, setJ), deltaX = new DoubleMatrix(b.rows, 1);
        double currentMeritValue = computeNewtonMeritValue(H), previousMeritValue = 0.0;

        if (currentMeritValue < epsilonAbsolute) { return new LCPSolverResult(x, LCPResultFlag.ABSOLUTE_CONVERGENCE); }
        if (Math.abs(currentMeritValue - previousMeritValue) < epsilonRelative * Math.abs(previousMeritValue)) { return new LCPSolverResult(x, LCPResultFlag.RELATIVE_CONVERGENCE); }
        for (int i = 0; i < iterations; i++) {
            if (i != 0) {
                y = A.mmul(x).add(b);
                H = constructHAndIntegerSets(x, y, u, l, setA, setJ);
            }

            if (!setA.isEmpty() && !setJ.isEmpty()) {
                DoubleMatrix[] bhParts = constructBHParts(A, x, l, u, y, new DoubleMatrix(new double[] {0.0, mu, mu, muR, muR, muR}), setA, setJ);
                DoubleMatrix C = bhParts[0], DInv = bhParts[2];

                DoubleMatrix S = Transforms.getMatrixComponents(A, setA, setA).sub(Transforms.getMatrixComponents(A, setA, setJ).mmul(DInv).mmul(C));
                DoubleMatrix deltaXA = Solve.pinv(S).mmul(Transforms.getVectorComponents(H, setA).sub(Transforms.getMatrixComponents(A, setA, setJ).mmul(DInv).mmul(Transforms.getVectorComponents(H, setJ))).neg());
                DoubleMatrix deltaXJ = DInv.mmul(Transforms.getVectorComponents(H, setJ)).neg().sub(DInv.mmul(C).mmul(deltaXA));
                deltaX.put(setA.stream().mapToInt(integer -> integer).toArray(), new int[] {0}, deltaXA);
                deltaX.put(setJ.stream().mapToInt(integer -> integer).toArray(), new int[] {0}, deltaXJ);
            } else if (!setA.isEmpty()) {
                DoubleMatrix S = Transforms.getMatrixComponents(A, setA, setA);
                DoubleMatrix deltaXA = Solve.pinv(S).mmul(Transforms.getVectorComponents(H, setA).neg());
                deltaX.put(setA.stream().mapToInt(integer -> integer).toArray(), new int[] {0}, deltaXA);
            } else if (!setJ.isEmpty()) {
                DoubleMatrix DInv = constructDInv(x, l, y, new DoubleMatrix(new double[] {0.0, mu, mu, muR, muR, muR}), setJ);
                DoubleMatrix deltaXJ = DInv.mmul(Transforms.getVectorComponents(H, setJ)).neg();
                deltaX.put(setJ.stream().mapToInt(integer -> integer).toArray(), new int[] {0}, deltaXJ);
            }

            double tau = projectedLineSearch(A, b, H, x, deltaX, u, l, mu, muR, alpha, beta, delta, lineSearchIterations);
            // x is rounded to prevent numerical imprecision from changing the result
            Transforms.round(x.addi(deltaX.dup().mul(tau)), roundingDigit);
            setBounds(u, l, x.get(0), mu, muR);

            currentMeritValue = computeNewtonMeritValue(H);
            if (currentMeritValue < epsilonAbsolute) {
                return new LCPSolverResult(x, LCPResultFlag.ABSOLUTE_CONVERGENCE);
            }
            if (Math.abs(currentMeritValue - previousMeritValue) < epsilonRelative * Math.abs(previousMeritValue)) {
                return new LCPSolverResult(x, LCPResultFlag.RELATIVE_CONVERGENCE);
            }
            previousMeritValue = currentMeritValue;
            setA.clear();
            setJ.clear();
        }
        return new LCPSolverResult(x, LCPResultFlag.MAX_ITERATIONS_REACHED);
    }


    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    public LCPSolverResult solveBLCPWithNNCG(DoubleMatrix A, DoubleMatrix b, double mu, double muR, int iterations, int roundingDigit) {
        return solveBLCPWithNNCG(initializeX(b), A, b, mu, muR, iterations, roundingDigit);
    }


    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    public LCPSolverResult solveBLCPWithNNCG(DoubleMatrix x0, DoubleMatrix A, DoubleMatrix b, double mu, double muR, int iterations, int roundingDigit) {
        DoubleMatrix APrime = computeAPrime(A, roundingDigit), bPrime = computeBPrime(A, b, roundingDigit);
        DoubleMatrix x1 = solveBLCPWithPGS(APrime, bPrime, b, mu, muR, 10e-10, 1).x;
        DoubleMatrix grad0 = x1.sub(x0.dup()).neg(), p0 = grad0.neg();

        DoubleMatrix xOld = x1.dup(), xNew = x1.dup(), gradNew, gradOld = grad0.dup(), p = p0.dup();
        for (int k = 1; k <= iterations; k++) {
            xNew = solveBLCPWithPGS(APrime, bPrime, b, mu, muR, 10e-10, 1).x;
            gradNew = (xNew.sub(xOld)).neg();
            double beta = (gradNew.norm2() * gradNew.norm2()) / (gradOld.norm2() * gradOld.norm2());
            if (beta > 1.0) {
                p.fill(0.0);
            } else if (Double.isNaN(beta)) {
                return new LCPSolverResult(xNew, LCPResultFlag.INVALID_VALUES);
            } else {
                xNew.add(p.dup().mul(beta));
                DoubleMatrix newP = p.dup().mul(beta).sub(gradNew);
                p.put(Transforms.createIndexArray(p.rows), Transforms.createIndexArray(p.columns), newP);
            }

            xOld = xNew.dup();
            gradOld = gradNew.dup();
        }
        return new LCPSolverResult(xNew, LCPResultFlag.MAX_ITERATIONS_REACHED);
    }

    public LCPSolverResult solveBLCPWithPGS(DoubleMatrix APrime, DoubleMatrix bPrime, DoubleMatrix b, double mu, double muR, double epsilon, int iterations) {
        DoubleMatrix x = initializeX(b), u = new DoubleMatrix(x.rows, 1).fill(Double.POSITIVE_INFINITY), l = new DoubleMatrix(x.rows, 1).fill(0.0);
        setBounds(u, l, x.get(0), mu, muR);
        double gamma, delta = Double.POSITIVE_INFINITY;

        for (int j = 0; j < iterations; j++) {
            gamma = delta;
            delta = 0;

            x.subi(bPrime.add(APrime.mmul(x)));
            setBounds(u, l, x.get(0), mu, muR);
            x = u.min(l.max(x));
            delta = Math.max(delta, x.max());

            if (delta > gamma) {
                return new LCPSolverResult(x, LCPResultFlag.DIVERGENCE);
            }
            if ((delta-gamma) / gamma < epsilon) {
                return new LCPSolverResult(x, LCPResultFlag.RELATIVE_CONVERGENCE);
            }
        }
        return new LCPSolverResult(x, LCPResultFlag.MAX_ITERATIONS_REACHED);
    }

    public LCPSolverResult solveBLCPWithPGS(DoubleMatrix A, DoubleMatrix b, double mu, double muR, double epsilon, int iterations, int roundingDigit) {
        return solveBLCPWithPGS(initializeX(b), A, b, mu, muR, epsilon, iterations, roundingDigit);
    }


    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    public LCPSolverResult solveBLCPWithPGS(DoubleMatrix x0, DoubleMatrix A, DoubleMatrix b, double mu, double muR, double epsilon, int iterations, int roundingDigit) {
        DoubleMatrix APrime = computeAPrime(A, roundingDigit), bPrime = computeBPrime(A, b, roundingDigit);
        DoubleMatrix x = x0.dup(), u = new DoubleMatrix(x.rows, 1).fill(Double.POSITIVE_INFINITY), l = new DoubleMatrix(x.rows, 1).fill(0.0);
        setBounds(u, l, x.get(0), mu, muR);
        double gamma, delta = Double.POSITIVE_INFINITY;

        for (int j = 0; j < iterations; j++) {
            gamma = delta;
            delta = 0;

            x.subi(bPrime.add(APrime.mmul(x)));
            setBounds(u, l, x.get(0), mu, muR);
            x = u.min(l.max(x));
            delta = Math.max(delta, x.max());

            if (delta > gamma) {
                return new LCPSolverResult(x, LCPResultFlag.DIVERGENCE);
            }
            if ((delta - gamma) / gamma < epsilon) {
                return new LCPSolverResult(x, LCPResultFlag.RELATIVE_CONVERGENCE);
            }
        }
        return new LCPSolverResult(x, LCPResultFlag.MAX_ITERATIONS_REACHED);
    }

    private DoubleMatrix computeAPrime(DoubleMatrix A, int roundingDigit) {
        DoubleMatrix result = A.dup();
        for (int i = 0; i < A.rows; i++) {
            result.putRow(i, Transforms.round(result.getRow(i).div(result.get(i, i)), roundingDigit));
        }
        return result;
    }

    private DoubleMatrix computeBPrime(DoubleMatrix A, DoubleMatrix b, int roundingDigit) {
        DoubleMatrix result = b.dup();
        for (int i = 0; i < b.rows; i++) {
            result.put(i, Transforms.round(b.get(i) / A.get(i, i), roundingDigit));
        }
        return result;
    }


    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    public LCPSolverResult solveBLCPWithPSOR(DoubleMatrix A, DoubleMatrix b, double mu, double muR, double omega, double epsilon, int iterations, int roundingDigit) {
        return solveBLCPWithPSOR(initializeX(b), A, b, mu, muR, omega, epsilon, iterations, roundingDigit);
    }


    *//**
     * @param mu The "normal"/Coulomb friction coefficient
     * @param muR The rolling friction coefficient
     *//*
    public LCPSolverResult solveBLCPWithPSOR(DoubleMatrix x0, DoubleMatrix A, DoubleMatrix b, double mu, double muR, double omega, double epsilon, int iterations, int roundingDigit) {
        DoubleMatrix U = Transforms.strictUpperTriangular(A), L = Transforms.strictLowerTriangular(A), D = DoubleMatrix.diag(A.diag());

        DoubleMatrix M = D.dup().add(L.dup().mul(omega)), MInv = Transforms.round(Solve.pinv(M), roundingDigit), N = Transforms.round(D.dup().mul(omega - 1.0).add(U.dup().mul(omega)), roundingDigit);
        DoubleMatrix x = x0.dup(), u = new DoubleMatrix(x.rows, 1).fill(Double.POSITIVE_INFINITY), l = new DoubleMatrix(x.rows, 1).fill(0.0);
        setBounds(u, l, x.get(0, 0), mu, muR);
        double gamma, delta = Double.POSITIVE_INFINITY;
        for (int i = 0; i < iterations; i++) {
            gamma = delta;
            delta = 0;
            for (int j = 0; j < x.rows; j++) {
                DoubleMatrix xPrime = MInv.mmul(N.mmul(x).sub(b));
                delta = Math.max(delta, xPrime.get(j));
                x.put(j, Math.min(u.get(j), Math.max(l.get(j), xPrime.get(j))));
                if (j == 0) { setBounds(u, l, x.get(0, 0), mu, muR); }
            }

            if (delta > gamma) {
                return new LCPSolverResult(x, LCPResultFlag.DIVERGENCE);
            }
            if ((delta - gamma) / gamma < epsilon) {
                return new LCPSolverResult(x, LCPResultFlag.RELATIVE_CONVERGENCE);
            }
        }
        return new LCPSolverResult(x, LCPResultFlag.MAX_ITERATIONS_REACHED);
    }

    public static DoubleMatrix[] constructBLCPMatrices(Collision[] collisions, double timeStep) {
        DoubleMatrix MInv = constructInverseMassMatrix(collisions);
        DoubleMatrix J = constructBLCPJacobian(collisions);
        DoubleMatrix u = constructUVector(collisions);
        DoubleMatrix f = constructFVector(collisions);

        DoubleMatrix A = J.mmul(MInv).mmul(J.transpose());
        DoubleMatrix b = applyCoefficientsOfRestitution(J.mmul(u).mul(-1.0), collisions, 6).subi(J.mmul(MInv).mmul(f).mul(timeStep));
        return new DoubleMatrix[] {A, b};
    }

    public static DoubleMatrix[] constructBLCPMatrices(double timeStep, double[] invMasses, double[] coefficientsOfRestitution, Vector3d[] contactPoints, Vector3d[] centerOfMassesA, Vector3d[] centerOfMassesB, Vector3d[] normals, Vector3d[][] tangentialDirectionsArrays, Vector3d[] velocities, Vector3d[] angularVelocities, Vector3d[] forces, Vector3d[] torques, Matrix3d[] inertiaMatrices, Matrix3d[] invInertiaMatrices) {
        DoubleMatrix MInv = constructMassMatrix(invMasses, invInertiaMatrices);
        DoubleMatrix J = constructBLCPJacobian(normals, tangentialDirectionsArrays, contactPoints, centerOfMassesA, centerOfMassesB);
        DoubleMatrix u = constructUVector(velocities, angularVelocities);
        DoubleMatrix f = constructFVector(forces, torques, angularVelocities, inertiaMatrices);

        DoubleMatrix A = J.mmul(MInv).mmul(J.transpose());
        DoubleMatrix b = applyCoefficientsOfRestitution(J, u, J.neg().mmul(u).sub(J.dup().mul(timeStep).mmul(MInv).mmul(f)), coefficientsOfRestitution, 5);
        return new DoubleMatrix[] {A, b};
    }

    public static DoubleMatrix applyCoefficientsOfRestitution(DoubleMatrix b, Collision[] collisions, int indicesBetweenCollisions) {
        DoubleMatrix restitutionVector = new DoubleMatrix(b.rows, b.columns);
        for (int i = 0; i < collisions.length; i++) {
            if (b.get(indicesBetweenCollisions * i) < 0) {
                restitutionVector.put(indicesBetweenCollisions * i, 0, collisions[i].coefficientOfRestitution * b.get(indicesBetweenCollisions * i));
            }
        }
        b.addi(restitutionVector);
        return b;
    }

    public static DoubleMatrix applyCoefficientsOfRestitution(DoubleMatrix J, DoubleMatrix u, DoubleMatrix b, double[] coefficientsOfRestitution, int indicesBetweenCollisions) {
        DoubleMatrix restitutionVector = new DoubleMatrix(b.rows, b.columns);
        for (int i = 0; i < coefficientsOfRestitution.length; i++) {
            // The minus is there because b is negative in the SIGGRAPH 2021 paper used as the basis for this implementation
            restitutionVector.put(indicesBetweenCollisions * i, 0, -coefficientsOfRestitution[i] * J.getColumnRange(0, 12 * i, 12 * i + 12).mmul(u.getRowRange(i * 12, i * 12 + 12, 0)).get(0));
        }
        b.addi(restitutionVector);
        return b;
    }

    public static DoubleMatrix constructBLCPJacobian(Collision[] collisions) {
        // This function implicitly assumes that all collisions have 2 tangential directions
        DoubleMatrix jacobian = new DoubleMatrix(6 * collisions.length, 12 * collisions.length);
        for (int i = 0; i < collisions.length; i++) {
            DoubleMatrix interpenetrationJacobian = constructInterpenetrationJacobian(collisions[i].normal, collisions[i].point, collisions[i].A.position, collisions[i].B.position);
            DoubleMatrix frictionJacobian = constructRotationalFrictionJacobian(collisions[i].normal, collisions[i].tangentialDirections, collisions[i].point, collisions[i].A.position, collisions[i].B.position);
            jacobian.put(new int[] {6 * i}, Transforms.createIndexArray(i * 12, 12), interpenetrationJacobian);
            jacobian.put(Transforms.createIndexArray(1 + 6 * i, 5), Transforms.createIndexArray(i * 12, 12), frictionJacobian);
        }
        return jacobian;
    }

    public static DoubleMatrix constructBLCPJacobian(Vector3d[] normals, Vector3d[][] tangentialDirectionsArrays, Vector3d[] contactPoints, Vector3d[] centerOfMassesA, Vector3d[] centerOfMassesB) {
        // This function implicitly assumes that all collisions have 2 tangential directions
        DoubleMatrix jacobian = new DoubleMatrix(12, 6 * contactPoints.length);
        for (int i = 0; i < contactPoints.length; i++) {
            DoubleMatrix interpenetrationJacobian = constructInterpenetrationJacobian(normals[i], contactPoints[i], centerOfMassesA[i], centerOfMassesB[i]);
            DoubleMatrix frictionJacobian = constructRotationalFrictionJacobian(normals[i], tangentialDirectionsArrays[i], contactPoints[i], centerOfMassesA[i], centerOfMassesB[i]);
            jacobian.put(Transforms.createIndexArray(12), new int[] {6 * i}, interpenetrationJacobian.transpose());
            jacobian.put(Transforms.createIndexArray(12), Transforms.createIndexArray(1 + 6 * i, 5), frictionJacobian.transpose());
        }
        jacobian = jacobian.transpose();
        return jacobian;
    }

    public static DoubleMatrix[] constructLCPMatrices(Collision[] collisions, double timeStep) {
        DoubleMatrix M = constructMassMatrix(collisions);
        DoubleMatrix MInv = constructInverseMassMatrix(collisions);
        DoubleMatrix C = constructCMatrix(collisions);
        DoubleMatrix G = constructGMatrix(collisions);
        DoubleMatrix u = constructUVector(collisions);
        DoubleMatrix f = constructFVector(collisions);

        DoubleMatrix A = G.mmul(MInv.mmul(G.transpose())).add(C);
        DoubleMatrix b = G.mmul(MInv).mmul(M.mmul(u).add(f.mmul(timeStep)));
        return new DoubleMatrix[] {A, b};
    }

    public static DoubleMatrix[] constructLCPMatrices(double timeStep, double[] masses, double[] frictionCoefficients, double[] coefficientsOfRestitution,  int numOfTangentialDirections, Vector3d[] contactPoints, Vector3d[] centerOfMassesA, Vector3d[] centerOfMassesB, Vector3d[] normals, Vector3d[][] tangentialDirectionsArrays, Vector3d[] velocities, Vector3d[] angularVelocities, Vector3d[] forces, Vector3d[] torques, Matrix3d[] inertiaMatrices) {
        DoubleMatrix M = constructMassMatrix(masses, inertiaMatrices);
        DoubleMatrix MInv = Solve.solve(M, DoubleMatrix.eye(M.rows));
        DoubleMatrix C = constructCMatrix(frictionCoefficients, contactPoints.length, numOfTangentialDirections);
        DoubleMatrix G = constructGMatrix(normals, tangentialDirectionsArrays, numOfTangentialDirections, contactPoints, centerOfMassesA, centerOfMassesB);
        DoubleMatrix u = constructUVector(velocities, angularVelocities);
        DoubleMatrix f = constructFVector(forces, torques, angularVelocities, inertiaMatrices);

        DoubleMatrix A = G.mmul(MInv.mmul(G.transpose())).add(C);
        DoubleMatrix b = G.mmul(MInv).mmul(M.mmul(u).add(f.mmul(timeStep)));
        return new DoubleMatrix[] {A, b};
    }

    public static DoubleMatrix constructCMatrix(Collision[] collisions) {
        int numOfContacts = collisions.length, numOfTangentialDirections = collisions[0].tangentialDirections.length;
        DoubleMatrix muMatrix = constructDiagonalMatrix(collisions);
        DoubleMatrix E = constuctEMatrix(numOfContacts, numOfTangentialDirections);

        DoubleMatrix C = new DoubleMatrix(numOfContacts * (2 * numOfTangentialDirections + 1) + muMatrix.rows + 1, numOfContacts * (2 * numOfTangentialDirections + 1) + muMatrix.columns + 1).fill(0.0);
        C.put(Transforms.createIndexArray(1 + E.rows, muMatrix.rows), Transforms.createIndexArray(muMatrix.columns), muMatrix);
        C.put(Transforms.createIndexArray(C.rows - numOfContacts, numOfContacts), Transforms.createIndexArray(muMatrix.columns, numOfContacts * numOfTangentialDirections), E.transpose().mul(-1.0));
        C.put(Transforms.createIndexArray(1, E.rows), Transforms.createIndexArray(C.columns - numOfContacts, E.columns), E);
        return C;
    }

    public static DoubleMatrix constructCMatrix(double[] frictionCoefficients, int numOfContacts, int numOfTangentialDirections) {
        DoubleMatrix muMatrix = constructDiagonalMatrix(frictionCoefficients);
        DoubleMatrix E = constuctEMatrix(numOfContacts, numOfTangentialDirections);

        DoubleMatrix C = new DoubleMatrix(numOfContacts * numOfTangentialDirections + muMatrix.rows + 1, numOfContacts * numOfTangentialDirections + muMatrix.columns + 1).fill(0.0);
        C.put(Transforms.createIndexArray(1 + E.rows, muMatrix.rows), Transforms.createIndexArray(muMatrix.columns), muMatrix);
        C.put(Transforms.createIndexArray(C.rows - numOfContacts, numOfContacts), Transforms.createIndexArray(muMatrix.columns, numOfContacts * numOfTangentialDirections), E.transpose().mul(-1.0));
        C.put(Transforms.createIndexArray(1, E.rows), Transforms.createIndexArray(C.columns - numOfContacts, E.columns), E);
        return C;
    }

    public static DoubleMatrix constuctEMatrix(int numOfContacts, int numOfTangentialDirections) {
        DoubleMatrix E = new DoubleMatrix(numOfContacts * numOfTangentialDirections, numOfContacts);
        for (int i = 0; i < numOfContacts; i++) {
            E.put(Transforms.createIndexArray(i * numOfTangentialDirections, numOfTangentialDirections), new int[] {i}, DoubleMatrix.ones(numOfTangentialDirections, 1));
        }
        return E;
    }

    public static DoubleMatrix constructDiagonalMatrix(Collision[] collisions) {
        DoubleMatrix vec = new DoubleMatrix(collisions.length, 1);
        for (int i = 0; i < collisions.length; i++) {
            vec.put(i, collisions[i].getFrictionCoefficient());
        }
        return DoubleMatrix.diag(vec);
    }

    public static DoubleMatrix constructDiagonalMatrix(double[] values) {
        return DoubleMatrix.diag(Transforms.createVectorFromArray(values));
    }

    public static DoubleMatrix constructGMatrix(Collision[] collisions) {
        DoubleMatrix interpenetrationJacobian = new DoubleMatrix(12, collisions.length);
        for (int i = 0; i < collisions.length; i++) {
            DoubleMatrix jacobian = constructInterpenetrationJacobian(collisions[i].normal, collisions[i].point, collisions[i].A.position, collisions[i].B.position);
            interpenetrationJacobian.put(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, new int[] {i}, jacobian.transpose());
        }

        int numOfTangentialDirections = collisions[0].tangentialDirections.length;
        DoubleMatrix frictionJacobian = new DoubleMatrix(12, collisions.length * (2 * numOfTangentialDirections + 1));
        for (int i = 0; i < collisions.length; i++) {
            DoubleMatrix jacobian = constructRotationalFrictionJacobian(collisions[i].normal, collisions[i].tangentialDirections, collisions[i].point, collisions[i].A.position, collisions[i].B.position);
            int rowBaseIndex = i * jacobian.rows;
            frictionJacobian.put(Transforms.createIndexArray(0, jacobian.columns), Transforms.createIndexArray(rowBaseIndex, jacobian.rows), jacobian.transpose());
        }

        DoubleMatrix G = new DoubleMatrix(interpenetrationJacobian.rows, interpenetrationJacobian.columns + frictionJacobian.columns + 1).fill(0.0);
        G.put(Transforms.createIndexArray(frictionJacobian.rows), Transforms.createIndexArray(frictionJacobian.columns), frictionJacobian);
        G.put(Transforms.createIndexArray(interpenetrationJacobian.rows), Transforms.createIndexArray(frictionJacobian.columns, interpenetrationJacobian.columns), interpenetrationJacobian);
        G = G.transpose();
        return G;
    }

    public static DoubleMatrix constructGMatrix(Vector3d[] normals, Vector3d[][] tangentialDirectionsArrays, int numOfTangentialDirections, Vector3d[] contactPoints, Vector3d[] centerOfMassesA, Vector3d[] centerOfMassesB) {
        DoubleMatrix interpenetrationJacobian = new DoubleMatrix(12, normals.length);
        for (int i = 0; i < normals.length; i++) {
            DoubleMatrix jacobian = constructInterpenetrationJacobian(normals[i], contactPoints[i], centerOfMassesA[i], centerOfMassesB[i]);
            interpenetrationJacobian.put(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, new int[] {i}, jacobian.transpose());
        }

        DoubleMatrix frictionJacobian = new DoubleMatrix(12, tangentialDirectionsArrays.length * numOfTangentialDirections);
        for (int i = 0; i < tangentialDirectionsArrays.length; i++) {
            DoubleMatrix jacobian = constructFrictionJacobian(tangentialDirectionsArrays[i], contactPoints[i], centerOfMassesA[i], centerOfMassesB[i]);
            int rowBaseIndex = i * numOfTangentialDirections;
            frictionJacobian.put(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, new int[] {rowBaseIndex, rowBaseIndex + 1, rowBaseIndex + 2, rowBaseIndex + 3}, jacobian.transpose());
        }

        DoubleMatrix G = new DoubleMatrix(interpenetrationJacobian.rows, interpenetrationJacobian.columns + frictionJacobian.columns + 1).fill(0.0);
        G.put(Transforms.createIndexArray(frictionJacobian.rows), Transforms.createIndexArray(frictionJacobian.columns), frictionJacobian);
        G.put(Transforms.createIndexArray(interpenetrationJacobian.rows), Transforms.createIndexArray(frictionJacobian.columns, interpenetrationJacobian.columns), interpenetrationJacobian);
        G = G.transpose();
        return G;
    }

    public static DoubleMatrix constructFrictionJacobian(Vector3d[] tangentialDirections, Vector3d contactPoint, Vector3d centerOfMassA, Vector3d centerOfMassB) {
        // (2 * tangential directions + 1) is needed as a size, because the normal tangential friction directions
        // and then the rotational friction directions which consist of the tangential directions and the normal direction are needed
        DoubleMatrix jacobian = new DoubleMatrix(tangentialDirections.length, 12);
        for (int i = 0; i < tangentialDirections.length; i++) {
            DoubleMatrix tD = Transforms.jomlVectorToJBLASVector(tangentialDirections[i]);
            DoubleMatrix rA = Transforms.jomlVectorToJBLASVector(new Vector3d(contactPoint).sub(centerOfMassA));
            DoubleMatrix rB = Transforms.jomlVectorToJBLASVector(new Vector3d(contactPoint).sub(centerOfMassB));
            DoubleMatrix tDrAProduct = tD.transpose().mmul(Transforms.createCrossProductMatrix(rA));
            DoubleMatrix tDrBProduct = tD.transpose().mul(-1.0).mmul(Transforms.createCrossProductMatrix(rB));

            jacobian.put(new int[] {i}, new int[] {0, 1, 2}, tD.transpose().mul(-1.0));
            jacobian.put(new int[] {i}, new int[] {3, 4, 5}, tDrAProduct);
            jacobian.put(new int[] {i}, new int[] {6, 7, 8}, tD.transpose());
            jacobian.put(new int[] {i}, new int[] {9, 10, 11}, tDrBProduct);
        }
        return jacobian;
    }

    public static DoubleMatrix constructRotationalFrictionJacobian(Vector3d normal, Vector3d[] tangentialDirections, Vector3d contactPoint, Vector3d centerOfMassA, Vector3d centerOfMassB) {
        // (2 * tangential directions + 1) is needed as a size, because the normal tangential friction directions
        // and then the rotational friction directions which consist of the tangential directions and the normal direction are needed
        DoubleMatrix jacobian = new DoubleMatrix(2 * tangentialDirections.length + 1, 12);
        for (int i = 0; i < tangentialDirections.length; i++) {
            DoubleMatrix tD = Transforms.jomlVectorToJBLASVector(tangentialDirections[i]);
            DoubleMatrix rA = Transforms.jomlVectorToJBLASVector(new Vector3d(contactPoint).sub(centerOfMassA));
            DoubleMatrix rB = Transforms.jomlVectorToJBLASVector(new Vector3d(contactPoint).sub(centerOfMassB));
            DoubleMatrix tDrAProduct = tD.transpose().mmul(Transforms.createCrossProductMatrix(rA));
            DoubleMatrix tDrBProduct = tD.transpose().mul(-1.0).mmul(Transforms.createCrossProductMatrix(rB));

            jacobian.put(new int[] {i}, new int[] {0, 1, 2}, tD.transpose().mul(-1.0));
            jacobian.put(new int[] {i}, new int[] {3, 4, 5}, tDrAProduct);
            jacobian.put(new int[] {i}, new int[] {6, 7, 8}, tD.transpose());
            jacobian.put(new int[] {i}, new int[] {9, 10, 11}, tDrBProduct);
            jacobian.put(new int[] {i + tangentialDirections.length + 1}, new int[] {3, 4, 5}, tD.transpose().mul(-1.0));
            jacobian.put(new int[] {i + tangentialDirections.length + 1}, new int[] {9, 10, 11}, tD.transpose());
        }
        jacobian.put(new int[] {2}, new int[] {3, 4, 5}, Transforms.jomlVectorToJBLASVector(normal).transpose().mul(-1.0));
        jacobian.put(new int[] {2}, new int[] {9, 10, 11}, Transforms.jomlVectorToJBLASVector(normal).transpose());
        return jacobian;
    }

    public static DoubleMatrix constructInterpenetrationJacobian(Vector3d normal, Vector3d contactPoint, Vector3d centerOfMassA, Vector3d centerOfMassB) {
        DoubleMatrix n = Transforms.jomlVectorToJBLASVector(normal);
        DoubleMatrix rA = Transforms.jomlVectorToJBLASVector(new Vector3d(contactPoint).sub(centerOfMassA));
        DoubleMatrix rB = Transforms.jomlVectorToJBLASVector(new Vector3d(contactPoint).sub(centerOfMassB));
        DoubleMatrix nrAProduct = n.transpose().mmul(Transforms.createCrossProductMatrix(rA));
        DoubleMatrix nrBProduct = n.transpose().mul(-1.0).mmul(Transforms.createCrossProductMatrix(rB));

        DoubleMatrix jacobian = new DoubleMatrix(1, 12);
        jacobian.put(new int[] {0}, new int[] {0, 1, 2}, n.transpose().mul(-1.0));
        jacobian.put(new int[] {0}, new int[] {3, 4, 5}, nrAProduct);
        jacobian.put(new int[] {0}, new int[] {6, 7, 8}, n.transpose());
        jacobian.put(new int[] {0}, new int[] {9, 10, 11}, nrBProduct);
        return jacobian;
    }

    public static DoubleMatrix constructMassMatrix(Collision[] collisions) {
        DoubleMatrix massMatrix = new DoubleMatrix(12 * collisions.length, 12 * collisions.length).fill(0.0);
        for (int i = 0; i < collisions.length; i++) {
            DoubleMatrix mat = constructMassMatrix(collisions[i].A.mass, collisions[i].A.worldFrameInertia);
            DoubleMatrix mat2 = constructMassMatrix(collisions[i].B.mass, collisions[i].B.worldFrameInertia);
            massMatrix.put(Transforms.createIndexArray(i * 6, 6), Transforms.createIndexArray(i * 6, 6), mat);
            massMatrix.put(Transforms.createIndexArray(i * 6 + 6, 6), Transforms.createIndexArray(i * 6 + 6, 6), mat2);
        }
        return massMatrix;
    }

    public static DoubleMatrix constructInverseMassMatrix(Collision[] collisions) {
        DoubleMatrix massMatrix = DoubleMatrix.diag(constructDiagonalMassVector(collisions));
        for (int i = 0; i < collisions.length; i++) {
            massMatrix.put(Transforms.createIndexArray(i * 12 + 3, 3), Transforms.createIndexArray(i * 12 + 3, 3), Transforms.jomlMatrixToJBLASMatrix(collisions[i].A.worldFrameInertiaInv));
            massMatrix.put(Transforms.createIndexArray(i * 12 + 9, 3), Transforms.createIndexArray(i * 12 + 9, 3), Transforms.jomlMatrixToJBLASMatrix(collisions[i].B.worldFrameInertiaInv));
        }
        return massMatrix;
    }

    private static DoubleMatrix constructDiagonalMassVector(Collision[] collisions) {
        DoubleMatrix diagVec = new DoubleMatrix(12 * collisions.length, 1);
        for (int i = 0; i < collisions.length; i++) {
            PhysicsComponent A = collisions[i].A, B = collisions[i].B;
            DoubleMatrix newVec = new DoubleMatrix(new double[][]{{1.0 / A.mass}, {1.0 / A.mass}, {1.0 / A.mass}, {0.0}, {0.0}, {0.0}, {1.0 / B.mass}, {1.0 / B.mass}, {1.0 / B.mass}, {0.0}, {0.0}, {0.0}});
            diagVec.put(Transforms.createIndexArray(i * 12, 12), new int[] {0}, newVec);
        }
        return diagVec;
    }

    public static DoubleMatrix constructMassMatrix(double[] masses, Matrix3d[] inertiaMatrices) {
        DoubleMatrix massMatrix = new DoubleMatrix(6 * masses.length, 6 * masses.length).fill(0.0);
        for (int i = 0; i < masses.length; i++) {
            DoubleMatrix mat = constructMassMatrix(masses[i], inertiaMatrices[i]);
            massMatrix.put(Transforms.createIndexArray(i * 6, 6), Transforms.createIndexArray(i * 6, 6), mat);
        }
        return massMatrix;
    }

    public static DoubleMatrix constructMassMatrix(double mass, Matrix3d inertiaMatrix) {
        DoubleMatrix massMatrix = DoubleMatrix.eye(6).mul(mass);
        massMatrix.put(new int[] {3, 4, 5}, new int[] {3, 4, 5}, Transforms.jomlMatrixToJBLASMatrix(inertiaMatrix));
        return massMatrix;
    }

    public static DoubleMatrix constructFVector(Vector3d[] forces, Vector3d[] torques, Vector3d[] angularVelocities, Matrix3d[] inertiaMatrices) {
        DoubleMatrix fVector = new DoubleMatrix(1, 6 * forces.length);
        for (int i = 0; i < forces.length; i++) {
            fVector.put(0, Transforms.createIndexArray(i * 6,  6), constructFVector(forces[i], torques[i], angularVelocities[i], inertiaMatrices[i]).transpose());
        }
        fVector = fVector.transpose();
        return fVector;
    }

    public static DoubleMatrix constructFVector(Collision[] collisions) {
        DoubleMatrix fVector = new DoubleMatrix(12 * collisions.length, 1);
        for (int i = 0; i < collisions.length; i++) {
            PhysicsComponent A = collisions[i].A, B = collisions[i].B;
            Vector3d[] torques = constructTotalTorque(collisions[i]);
            DoubleMatrix subFVector = Transforms.jomlVectorsToJBLASVector(new Vector3d[]{A.force, torques[0], B.force, torques[1]});
            fVector.put(Transforms.createIndexArray(i * 12, 12), new int[] {0}, subFVector);
        }
        return fVector;
    }

    public static Vector3d[] constructTotalTorque(Collision collision) {
        PhysicsComponent A = collision.A, B = collision.B;
        Vector3d ATotalTorque = new Vector3d(A.torque);
        // if (A.objectType.performTimeStep) { ATotalTorque.sub(new Vector3d(A.angularVelocity).cross(new Vector3d(A.angularVelocity).mul(A.worldFrameInertia))); }
        ATotalTorque.sub(new Vector3d(A.angularVelocity).cross(new Vector3d(A.angularVelocity).mul(A.worldFrameInertia)));
        Vector3d BTotalTorque = new Vector3d(B.torque);
        // if (B.objectType.performTimeStep) { BTotalTorque.sub(new Vector3d(B.angularVelocity).cross(new Vector3d(B.angularVelocity).mul(B.worldFrameInertia)));}
        BTotalTorque.sub(new Vector3d(B.angularVelocity).cross(new Vector3d(B.angularVelocity).mul(B.worldFrameInertia)));
        return new Vector3d[] {ATotalTorque, BTotalTorque};
    }

    public static DoubleMatrix constructFVector(Vector3d force, Vector3d torque, Vector3d angularVelocity, Matrix3d inertiaMatrix) {
        DoubleMatrix fVector = new DoubleMatrix(6, 1);
        fVector.put(new int[] {0, 1, 2}, new int[] {0}, Transforms.jomlVectorToJBLASVector(force));
        Vector3d secondVector = new Vector3d(torque).sub(new Vector3d(angularVelocity).cross(new Vector3d(angularVelocity).mul(inertiaMatrix)));
        fVector.put(new int[] {3, 4, 5}, new int[] {0}, Transforms.jomlVectorToJBLASVector(secondVector));
        return fVector;
    }

    public static DoubleMatrix constructUVector(Collision[] collisions) {
        DoubleMatrix u = new DoubleMatrix(6 * 2 * collisions.length, 1).fill(0.0);
        for (int i = 0; i < collisions.length; i++) {
            DoubleMatrix subU = Transforms.jomlVectorsToJBLASVector(new Vector3d[]{collisions[i].A.velocity, collisions[i].A.angularVelocity, collisions[i].B.velocity, collisions[i].B.angularVelocity});
            u.put(Transforms.createIndexArray(i * 12, 12), 0, subU);
        }
        return u;
    }

    public static DoubleMatrix constructUVector(Vector3d[] velocities, Vector3d[] angularVelocities) {
        DoubleMatrix u = new DoubleMatrix(3 * (velocities.length + angularVelocities.length), 1).fill(0.0);
        for (int i = 0; i < velocities.length; i++) {
            u.put(Transforms.createIndexArray(i * 6, 3), 0, Transforms.jomlVectorToJBLASVector(velocities[i]));
            u.put(Transforms.createIndexArray(i * 6 + 3, 3), 0, Transforms.jomlVectorToJBLASVector(angularVelocities[i]));
        }
        return u;
    }

    public static LCPSolver getInstance() {
        return new LCPSolver();
    }

    public static void addSolvedCollisions(List<Collision> collisions, DoubleMatrix result) {
        collisions.forEach(collision -> solvedCollisions.putIfAbsent(collision.hashCode(), result));
    }

    public static void addSolvedCollision(Collision collision, DoubleMatrix result) {
        solvedCollisions.put(collision.hashCode(), result);
    }*/

}
