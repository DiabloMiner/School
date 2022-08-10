package com.diablominer.opengl.examples.learning;

public class NNCGConfiguration extends LCPSolverConfiguration {

    public int iterations;

    public NNCGConfiguration() {
        this.iterations = 60;
        this.solverChoice = LCPSolverChoice.BLCP_NNCP;
    }

    public NNCGConfiguration(int iterations) {
        this.iterations = iterations;
        this.solverChoice = LCPSolverChoice.BLCP_NNCP;
    }

}
