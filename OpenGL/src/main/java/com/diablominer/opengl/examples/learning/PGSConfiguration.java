package com.diablominer.opengl.examples.learning;

public class PGSConfiguration extends LCPSolverConfiguration {

    public int iterations;
    public double epsilonRelative;

    public PGSConfiguration() {
        this.iterations = 60;
        this.epsilonRelative = 10e-20;
        this.solverChoice = LCPSolverChoice.BLCP_PGS;
    }

    public PGSConfiguration(int iterations, double epsilonRelative) {
        this.iterations = iterations;
        this.epsilonRelative = epsilonRelative;
        this.solverChoice = LCPSolverChoice.BLCP_PGS;
    }

}
