package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.jblas.DoubleMatrix;
import org.joml.Vector3d;
import org.lwjgl.system.CallbackI;

import java.util.function.Consumer;

public class Contact {

    // TODO: Use some better way to get tol from PhysEngine to Contact
    public static double tol = 1e-10;

    // cor stands for coefficient of restitution, cosf for coefficient of static friction,
    // cokf for coefficient of kinetic friction, corf for coefficient of rolling friction
    protected double cor, cosf, cokf, corf;
    protected Vector3d point, normal, penetration;
    protected PhysicsComponent A, B;

    protected ContactConstraint c;
    protected DoubleMatrix x;

    public Contact(PhysicsComponent A, PhysicsComponent B, Vector3d point, Vector3d normal, Vector3d penetration) {
        this.A = A;
        this.B = B;
        this.point = point;

        // The difference of the positions of A and B is projected onto the normal to find the correct sign of the direction
        // The resulting normal direction is then normalized
        this.normal = new Vector3d(normal).mul((new Vector3d(B.position).sub(A.position)).dot(normal)).normalize();
        // Penetration needs to be positive to work in the correct direction
        // Hypothesis: This is an requirement because it will be multiplied with the direction reducing each body's error during multiplication of Jt and x
        this.penetration = penetration.absolute(new Vector3d());

        this.cor = Material.coefficientsOfRestitution.get(Material.hash(A.material, B.material));
        this.cosf = Material.coefficientsOfStaticFriction.get(Material.hash(A.material, B.material));
        this.cokf = Material.coefficientsOfKineticFriction.get(Material.hash(A.material, B.material));
        this.corf = Material.coefficientsOfRollingFriction.get(Material.hash(A.material, B.material));

        this.x = new DoubleMatrix(1);
    }

    public DoubleMatrix getU() {
        DoubleMatrix u = new DoubleMatrix(3 * 4, 1);
        u.put(new int[] {0, 1, 2}, 0, Transforms.jomlVectorToJBLASVector(A.velocity));
        u.put(new int[] {3, 4, 5}, 0, Transforms.jomlVectorToJBLASVector(A.angularVelocity));
        u.put(new int[] {6, 7, 8}, 0, Transforms.jomlVectorToJBLASVector(B.velocity));
        u.put(new int[] {9, 10, 11}, 0, Transforms.jomlVectorToJBLASVector(B.angularVelocity));
        return u;
    }

    /**
     * Get unsigned relative velocity
     */
    public double getURelVel() {
        Vector3d vel = (B.velocity.add(B.angularVelocity.cross(point.sub(B.position, new Vector3d()), new Vector3d()), new Vector3d()))
                .sub((A.velocity.add(A.angularVelocity.cross(point.sub(A.position, new Vector3d()), new Vector3d()), new Vector3d())), new Vector3d());
        return vel.length();
    }

    /**
     * Get signed projected relative velocity (based on current forces and torques)
     */
    public double projectRelVel(double timeStep) {
        Vector3d vA = A.velocity.add(new Vector3d(A.force).mul(timeStep / A.mass), new Vector3d());
        Vector3d vB = B.velocity.add(new Vector3d(B.force).mul(timeStep / B.mass), new Vector3d());
        Vector3d omegaA = A.angularVelocity.add(new Vector3d(A.torque).mul(timeStep).mul(A.worldFrameInertiaInv), new Vector3d());
        Vector3d omegaB = B.angularVelocity.add(new Vector3d(B.torque).mul(timeStep).mul(B.worldFrameInertiaInv), new Vector3d());
        Vector3d v = (vB.add(omegaB.cross(point.sub(B.position, new Vector3d()), new Vector3d()), new Vector3d())).sub(vA.add(omegaA.cross(point.sub(A.position, new Vector3d()), new Vector3d()), new Vector3d()));
        return normal.dot(v);
    }

    public DoubleMatrix getJMinv(PhysicsComponent physComp) {
        DoubleMatrix J = new DoubleMatrix(1, 12), MInv = ((StandardPhysicsComponent) physComp).getMInv();
        if (c == null) { c = new ContactConstraint(this, tol); }
        if (c.getJacobian(physComp).isPresent()) { J = c.getJacobian(physComp).get(); }
        return J.mmul(MInv);
    }

    // TODO: x is currently fixed to a predetermined size, if a full implementation commences, this size should be decided by the PhysicsEngine
    public DoubleMatrix getX() {
        return x;
    }

    public void setX(DoubleMatrix x) {
        this.x.put(0, x.get(0));
    }

}
