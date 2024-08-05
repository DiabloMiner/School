package com.diablominer.opengl.examples.learning;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

public class LCPSolver {

    public static void gaussSeidel(DoubleMatrix A, DoubleMatrix b, DoubleMatrix x, DoubleMatrix hi, DoubleMatrix lo, int iter) {
        int n = x.getRows();

        double sum;
        while (iter > 0) {
            for (int i = 0; i < n; i++) {
                sum = b.get(i);
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum = sum - (A.get(i, j)) * x.get(j);
                    }
                }
                assert A.get(i, i) != 0.0;
                x.put(i, -sum / A.get(i, i));
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


            iter--;
        }
    }

    protected static double newtonMeritValue(DoubleMatrix H) {
        return H.dot(H) * 0.5;
    }

    protected static double fischerMeritValue(DoubleMatrix f) {
        return Math.abs(f.dot(f)) * 0.5;
    }

    protected static double qpMeritValue(DoubleMatrix A, DoubleMatrix b, DoubleMatrix x) {
        DoubleMatrix y = A.mmul(x).add(b);
        for (int i = 0; i < y.getLength(); i++) {
            y.put(i, Math.abs(y.get(i)));
        }
        return Math.abs(x.transpose().mmul(y).get(0));
    }

    /**
     * This method returns the minimum of the numbers a,b, a^2 and b^2
     */
    protected static double min(double a, double b) {
        double min1 = Math.min(Math.abs(a), Math.abs(b));
        double min2 = Math.min(Math.abs(a * a), Math.abs(b * b));
        return Math.min(min1, min2);
    }

    /**
     * This function gets the nearest power of 2 to some number a
     */
    protected static double nearestPower(double a) {
        long bits = ((Double.doubleToLongBits(a) >>> 52) & 0b0000000000000000000000000000000000000000000000000000011111111111) - 1023L;
        return Math.pow(2, bits);
    }

    protected static DoubleMatrix adjustBounds(DoubleMatrix x, DoubleMatrix y, DoubleMatrix hi, double relMinValue, double minValueBound, double boundAdjustment, double infValue) {
        DoubleMatrix hiNew = hi.dup();
        for (int i = 0; i < hi.getRows(); i++) {
            if (Math.abs(y.get(i)) <= PhysicsEngine.epsilon) {
                // This checks if y has dropped below a predetermined epsilon threshold and then sets the appropriate bound 'infinity'
                // (i.e. a very high value that results in the fischer function returning 0 but not NaN) so the algorithm can halt.
                hiNew.put(i, infValue);
            } else {
                double bound = nearestPower(min(x.get(i), y.get(i)) * boundAdjustment);

                // If y is below a bounding value and the adjusted bound is smaller than the difference between y and x multiplied
                // by the relative min value constant, then the bound is set to that
                if (hiNew.get(i) < relMinValue * Math.abs(x.get(i) - y.get(i)) && Math.abs(y.get(i)) <= minValueBound) {
                    bound = 100 * Math.abs(x.get(i) - y.get(i));
                }

                hiNew.put(i, bound);
            }
        }
        return hiNew;
    }

    protected static double fischerFunction(double xi, double yi) {
        return Math.sqrt(xi * xi + yi * yi) - (xi + yi);
    }

    protected static DoubleMatrix fischerMeritValueGradient(DoubleMatrix H, DoubleMatrix f) {
        return H.transpose().mmul(f);
    }

    protected static DoubleMatrix fischerFunction(DoubleMatrix x, DoubleMatrix y, DoubleMatrix hi, DoubleMatrix lo) {
        int n = x.getLength();
        DoubleMatrix f = new DoubleMatrix(n, 1);
        for (int i = 0; i < n; i++) {
            double xi = x.get(i), yi = y.get(i), ui = hi.get(i), li = lo.get(i);
            f.put(i, fischerFunction(xi - li, fischerFunction(-yi, ui - xi)));
        }
        return f;
    }

    protected static double armijoLineSearch(DoubleMatrix A, DoubleMatrix b, DoubleMatrix H, DoubleMatrix f, DoubleMatrix x, DoubleMatrix deltaX, DoubleMatrix hi, DoubleMatrix lo, double alpha, double beta, double delta, double boundMinValue, double minValueBound, double boundAdjustment, double infValue, int iterations) {
        double meritValue0 = fischerMeritValue(f), tau = 1;
        DoubleMatrix meritValueGradient0 = fischerMeritValueGradient(H, f);
        DoubleMatrix xTau, hiTau, yTau;

        for (int j = 0; j < iterations; j++) {
            xTau = x.add(deltaX.mul(tau)).max(0.0);
            yTau = A.mmul(xTau).add(b);
            hiTau = adjustBounds(xTau, yTau, hi, boundMinValue, minValueBound, boundAdjustment, infValue);

            double meritValue = fischerMeritValue(fischerFunction(xTau, yTau, hiTau, lo));
            if (meritValue <= (meritValue0 + alpha * tau * meritValueGradient0.dot(deltaX))) {
                break;
            }
            if (tau <= delta) {
                break;
            }

            tau *= beta;
        }

        return tau;
    }

    protected static DoubleMatrix constructFischerH(DoubleMatrix A, DoubleMatrix x, DoubleMatrix y) {
        int n = x.getLength();
        DoubleMatrix p  = new DoubleMatrix(n), q = new DoubleMatrix(n);

        for (int i = 0; i < n; i++) {
            double xi = x.get(i), yi = y.get(i), xynorm = Math.sqrt(xi * xi + yi * yi), pi, qi;
            if (!(xi == 0 && yi == 0)) {
                pi = (xi / xynorm) - 1;
                qi = (yi / xynorm) - 1;
            } else  {
                double ai = 0, bi = 0;
                pi = ai - 1;
                qi = bi - 1;
            }
            p.put(i, pi);
            q.put(i, qi);
        }

        DoubleMatrix Dp = DoubleMatrix.diag(p), Dq = DoubleMatrix.diag(q);
        return Dp.add(Dq.mmul(A));
    }

    /**
     * This method perturbs an element xi of x by adding a small perturbation factor,
     * if the corresponding element yi of y is smaller than 0 and xi is 0.
     * The perturbation should be chosen large enough to get the algorithm out of local minima.
     */
    public static DoubleMatrix perturbX(DoubleMatrix x, DoubleMatrix y, double perturbation) {
        for (int i = 0; i < x.getLength(); i++) {
            if (y.get(i) < -PhysicsEngine.epsilon && (x.get(i) >= -PhysicsEngine.epsilon && x.get(i) <= PhysicsEngine.epsilon)) {
                x.put(i, perturbation);
            }
        }
        return x;
    }

    public static DoubleMatrix fischerNewton(DoubleMatrix A, DoubleMatrix b, DoubleMatrix x, DoubleMatrix hi, DoubleMatrix lo, double alpha, double beta, double delta, double boundMinValue, double minValueBound, double boundAdjustment, double infValue, double perturbation, double epsilonAbsolute, double epsilonRelative, int iterations, int lineSearchIterations) {
        DoubleMatrix H = constructFischerH(A, x, A.mmul(x).add(b)), deltaX, f = new DoubleMatrix(x.getRows(), 1), y = A.mmul(x).add(b);
        perturbX(x, y, perturbation);
        double currentMeritValue, previousMeritValue = fischerMeritValue(fischerFunction(x, y, hi, lo));

        for (int i = 0; i < iterations; i++) {
            if (i == 0) {
                hi = adjustBounds(x, y, hi, boundMinValue, minValueBound, boundAdjustment, infValue);
                f = fischerFunction(x, y, hi, lo);
            }
            deltaX = Solve.solve(H, f.neg());

            double tau = armijoLineSearch(A, b, H, f, x, deltaX, hi, lo, alpha, beta, delta, boundMinValue, minValueBound, boundAdjustment, infValue, lineSearchIterations);
            x.addi(deltaX.dup().mul(tau));

            y = A.mmul(x).add(b);
            H = constructFischerH(A, x, y);
            hi = adjustBounds(x, y, hi, boundMinValue, minValueBound, boundAdjustment, infValue);
            f = fischerFunction(x, y, hi, lo);
            currentMeritValue = fischerMeritValue(f);
            if (currentMeritValue < epsilonAbsolute) {
                break;
            }
            if (Math.abs(currentMeritValue - previousMeritValue) < epsilonRelative * Math.abs(previousMeritValue)) {
                break;
            }
            previousMeritValue = currentMeritValue;
        }

        return x;
    }

}
