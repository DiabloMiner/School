package com.diablominer.opengl.examples.learning;

import org.jblas.DoubleMatrix;

public class LCPSolverResult {

    public LCPResultFlag resultFlag;
    public DoubleMatrix x;

    public LCPSolverResult(DoubleMatrix x, LCPResultFlag resultFlag) {
        this.x = x;
        this.resultFlag = resultFlag;
    }

}
