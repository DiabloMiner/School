package com.diablominer.opengl.examples.learning;

import org.jblas.DoubleMatrix;

import java.util.Optional;

public abstract class Constraint {

    // Implement a derived contact constraint
    // For each body a constraint is generated
    // OR
    // Implement a contact class storing ALL data for that contact (i.e. both rigid bodies)
    // (DO THIS) -> other solution would lead to messy storage of r and this solution may be extended to form contact graphs more easily
    // Also implement ContactConstraint (processes the data into Jacobian) while implementing Contact (determines if a contact is there and stores its data)
    // TODO: Test code and design & write some doc

    protected boolean equality;

    public Constraint(boolean equality) {
        this.equality = equality;
    }

    public abstract Optional<DoubleMatrix> getJacobian(PhysicsComponent physicsComponent);

    public abstract Optional<DoubleMatrix> getPenalty(PhysicsComponent physicsComponent);

    public boolean isEquality() {
        return equality;
    }
}
