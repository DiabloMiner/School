package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;
import org.joml.Matrix3d;
import org.joml.Vector3d;

public class ContactConstraint extends Constraint {

    protected Vector3d normal, r;

    public ContactConstraint(Vector3d normal, Vector3d r, boolean equality) {
        super(equality);
        this.normal = new Vector3d(normal);
        this.r = new Vector3d(r);
    }

    @Override
    public DoubleMatrix getJacobian() {
        DoubleMatrix result = new DoubleMatrix(3, 6);
        result.put(new int[] {0, 1, 2}, new int[] {0, 1, 2}, Transforms.jomlMatrixToJBLASMatrix(new Matrix3d().identity().scale(normal)));
        result.put(new int[] {0, 1, 2}, new int[] {3, 4, 5}, Transforms.jomlMatrixToJBLASMatrix(Transforms.crossProductMatrix(r.cross(normal, new Vector3d()))));
        return result;
    }

    @Override
    public DoubleMatrix getPenalty() {
        return new DoubleMatrix(3, 3).put(new int[] {0, 1, 2}, new int[] {0, 1, 2}, Transforms.jomlMatrixToJBLASMatrix(new Matrix3d().identity().scale(normal)));
    }

    public static ContactConstraint[] generateConstraints(Contact contact) {
        return new ContactConstraint[] {
                new ContactConstraint(contact.normal, contact.point.sub(contact.A.position, new Vector3d()), false),
                new ContactConstraint(contact.normal, contact.point.sub(contact.B.position, new Vector3d()), false)
        };
    }

}
