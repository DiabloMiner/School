package com.diablominer.opengl.examples.learning;

import org.jblas.DoubleMatrix;

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

    public abstract DoubleMatrix getJacobian();
    // For ContactConstraint:
    // If (colliding):
    //      ContactConstraint CreateContactConstraints(Rigidbody r0, Rigidbody r1) {
    //          return new ContactConstraint(r0.normal (points to r1), r0.r x r0.normal (in matrix form)),
    //                 new ContactConstraint(-r0.normal (points to r1), -(r1.r x r0.normal) (in matrix form));
    //      }
    //      getJacobian(Rigidbody r, Vector r) {
    //          return new DoubleMatrix(r.normal (points to r1), r x r.normal (in matrix form));
    //      }
    //
    //
    //      createRMatrix(Rigidbody r, Vector r) {
    //          Vector a = r x r.normal;
    //          new DoubleMatrix(0.0, a.z, -a.y, -a.z, 0.0, a.x, a.y, -a.x, 0.0);
    //      }
    //
    //      getPenalty(Vector normal) {
    //          return new DoubleMatrix(normal);
    //      }
    //

    public abstract DoubleMatrix getPenalty();

    public boolean isEquality() {
        return equality;
    }
}
