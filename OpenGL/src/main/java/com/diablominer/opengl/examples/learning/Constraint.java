package com.diablominer.opengl.examples.learning;

import org.jblas.DoubleMatrix;

import java.util.Optional;

public abstract class Constraint {

    protected boolean equality;

    public Constraint(boolean equality) {
        this.equality = equality;
    }

    public abstract Optional<DoubleMatrix> getJacobian(PhysicsComponent physicsComponent);

    public boolean isEquality() {
        return equality;
    }
}
