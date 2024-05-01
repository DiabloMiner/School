package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;
import org.joml.Math;
import org.joml.Vector3d;

import java.util.Optional;

public class ContactConstraint extends Constraint {

    protected Contact contact;
    protected PhysicsComponent A, B;
    protected Vector3d n, rA, rB;
    protected DoubleMatrix J, e, bounce;
    protected double tol;

    public ContactConstraint(Contact contact, double tol) {
        super(false);
        this.contact = contact;
        this.A = contact.A;
        this.B = contact.B;
        this.n = new Vector3d(contact.normal);
        this.rA = contact.point.sub(contact.A.position, new Vector3d());
        this.rB = contact.point.sub(contact.B.position, new Vector3d());
        this.tol = tol;

        this.J = getJacobian();
        this.e = getErrorCorrection();
        this.bounce = getBounce();
    }

    @Override
    public DoubleMatrix getJacobian() {
        if (this.J == null) {
            DoubleMatrix result = new DoubleMatrix(1, 12);
            result.put(new int[] {0}, new int[] {0, 1, 2}, Transforms.jomlVectorToJBLASVector(n).neg().transpose());
            result.put(new int[] {0}, new int[] {3, 4, 5}, Transforms.jomlVectorToJBLASVector(rA.cross(n, new Vector3d())).transpose());
            result.put(new int[] {0}, new int[] {6, 7, 8}, Transforms.jomlVectorToJBLASVector(n).transpose());
            result.put(new int[] {0}, new int[] {9, 10, 11}, Transforms.jomlVectorToJBLASVector(rB.cross(n, new Vector3d())).neg().transpose());
            return result;
        } else {
            return J;
        }
    }

    public Optional<DoubleMatrix> getJacobian(PhysicsComponent physComp) {
        if (this.J == null) {
            this.J = getJacobian();
        }

        if (physComp == A) {
            return Optional.of(J.get(new int[] {0}, new int[] {0, 1, 2, 3, 4, 5}));
        }  else if (physComp == B) {
            return Optional.of(J.get(new int[] {0}, new int[] {6, 7, 8, 9, 10, 11}));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public DoubleMatrix getErrorCorrection() {
        if (this.e == null) {
            DoubleMatrix e = new DoubleMatrix(1, 1);
            e.put(0, 0, -Math.abs(contact.penetration.dot(contact.normal)));
            return e;
        } else {
            return e;
        }
    }

    @Override
    public DoubleMatrix getBounce() {
        DoubleMatrix c = new DoubleMatrix(1, 12), bounce = new DoubleMatrix(1, 1);
        c.put(0, new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11}, getJacobian());
        bounce.put(0, 0, c.mmul(contact.getU()).mul(contact.cor).mul(contact.getURelVel() < this.tol ? 0 : 1).get(0));
        return bounce;
    }

}
