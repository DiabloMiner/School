package com.diablominer.opengl.examples.learning;

public class MinMapNewtonConfiguration extends LCPSolverConfiguration {

    public int iterations, lineSearchIterations;
    public double alpha, beta, delta, epsilonAbsolute, epsilonRelative;

    public MinMapNewtonConfiguration(boolean lcp) {
        this.iterations = 30;
        this.lineSearchIterations = 25;
        this.alpha = 10e-4;
        this.beta = 0.5;
        this.delta = 10e-10;
        this.epsilonAbsolute = 10e-10;
        this.epsilonRelative = 10e-20;
        if (lcp) {
            this.solverChoice = LCPSolverChoice.LCP_MIN_MAP_NEWTON;
        } else {
            this.solverChoice = LCPSolverChoice.BLCP_MIN_MAP_NEWTON;
        }
    }

    public MinMapNewtonConfiguration(int iterations, int lineSearchIterations, double alpha, double beta, double delta, double epsilonAbsolute, double epsilonRelative, boolean lcp) {
        this.iterations = iterations;
        this.lineSearchIterations = lineSearchIterations;
        this.alpha = alpha;
        this.beta = beta;
        this.delta = delta;
        this.epsilonAbsolute = epsilonAbsolute;
        this.epsilonRelative = epsilonRelative;
        if (lcp) {
            this.solverChoice = LCPSolverChoice.LCP_MIN_MAP_NEWTON;
        } else {
            this.solverChoice = LCPSolverChoice.BLCP_MIN_MAP_NEWTON;
        }
    }

}
