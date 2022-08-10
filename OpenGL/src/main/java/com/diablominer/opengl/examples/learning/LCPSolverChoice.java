package com.diablominer.opengl.examples.learning;

public enum LCPSolverChoice {
    BLCP_MIN_MAP_NEWTON(true),
    BLCP_NNCP(true),
    BLCP_PGS(true),
    BLCP_PSOR(true),
    LCP_MIN_MAP_NEWTON(false);

    public boolean forBLCP;

    LCPSolverChoice(boolean forBLCP) {
        this.forBLCP = forBLCP;
    }
}
