package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

public class Contact {

    // cor stands for coefficient of restitution, cosf for coefficient of static friction,
    // cokf for coefficient of kinetic friction, corf for coefficient of rolling friction
    protected double cor, cosf, cokf, corf;
    protected Vector3d point, normal;
    protected PhysicsComponent A, B;

    public Contact(PhysicsComponent A, PhysicsComponent B, Vector3d point, Vector3d normal) {
        this.A = A;
        this.B = B;

        this.point = point;
        // The difference of the positions of A and B is projected onto the normal to find the correct sign of the direction
        // The resulting normal direction is then normalized
        this.normal = new Vector3d(normal).mul((new Vector3d(A.position).sub(B.position)).dot(normal)).normalize();
        this.cor = Material.coefficientsOfRestitution.get(Material.hash(A.material, B.material));
        this.cosf = Material.coefficientsOfStaticFriction.get(Material.hash(A.material, B.material));
        this.cokf = Material.coefficientsOfKineticFriction.get(Material.hash(A.material, B.material));
        this.corf = Material.coefficientsOfRollingFriction.get(Material.hash(A.material, B.material));
    }

}
