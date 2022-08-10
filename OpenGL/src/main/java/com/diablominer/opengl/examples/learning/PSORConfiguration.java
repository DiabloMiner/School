package com.diablominer.opengl.examples.learning;

public class PSORConfiguration extends LCPSolverConfiguration{

    public int iterations;
    public double epsilonRelative, omega;

    public PSORConfiguration() {
        this.iterations = 60;
        this.epsilonRelative = 10e-20;
        this.omega = 1.0;
        this.solverChoice = LCPSolverChoice.BLCP_PSOR;
    }

    public PSORConfiguration(int iterations, double epsilonRelative, double omega) {
        this.iterations = iterations;
        this.epsilonRelative = epsilonRelative;
        this.omega = omega;
        this.solverChoice = LCPSolverChoice.BLCP_PSOR;
    }

}
