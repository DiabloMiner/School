package com.diablominer.opengl.examples.learning;

import org.joml.Vector3d;

import java.util.Arrays;
import java.util.Objects;

public class Collision {

    public double coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction;
    public Vector3d point, normal;
    public Vector3d[] tangentialDirections;
    public PhysicsObject A, B;
    public double timeStepTaken;
    public boolean solved;

    public Collision(Vector3d point, Vector3d normal, Vector3d[] tangentialDirections, double coefficientOfRestitution, double coefficientOfStaticFriction, double coefficientOfKineticFriction, PhysicsObject A, PhysicsObject B, double timeStepTaken) {
        this.point = point;
        this.normal = normal;
        this.tangentialDirections = tangentialDirections;
        this.A = A;
        this.B = B;
        this.coefficientOfRestitution = coefficientOfRestitution;
        this.coefficientOfStaticFriction = coefficientOfStaticFriction;
        this.coefficientOfKineticFriction = coefficientOfKineticFriction;
        this.timeStepTaken = timeStepTaken;
        this.solved = false;
        if (new Vector3d(point).add(normal).distance(A.position) > point.distance(A.position)) {
            normal.mul(-1.0);
        }
    }

    public void generateAdditionalTangentialDirections() {
        if (tangentialDirections.length == 2) {
            Vector3d[] newTangentialDirections = new Vector3d[4];
            for (int i = 0; i < 2; i++) {
                newTangentialDirections[i * 2] = tangentialDirections[i];
                newTangentialDirections[i * 2 + 1] = new Vector3d(tangentialDirections[i]).negate();
            }
            tangentialDirections = newTangentialDirections;
        }
    }

    public boolean isColliding() {
        return A.isColliding(B);
    }

    public double getFrictionCoefficient() {
        Vector3d relativeVelocity = new Vector3d(B.velocity).add(new Vector3d(B.angularVelocity).cross(new Vector3d(point).sub(B.position))).sub(new Vector3d(A.velocity).add(new Vector3d(A.angularVelocity).cross(new Vector3d(point).sub(A.position))));
        for (Vector3d tangentialDirection : tangentialDirections) {
            double relativeTangentialVelocity = new Vector3d(tangentialDirection).dot(relativeVelocity);
            if (Math.abs(relativeTangentialVelocity) > 0) {
                return coefficientOfKineticFriction;
            }
        }
        return coefficientOfStaticFriction;
    }

    public void applyImpulse(double normalImpulse, double[] frictionImpulses) {
        Vector3d normImpulse = new Vector3d(normal).mul(normalImpulse);
        Vector3d rA = new Vector3d(point).sub(A.position);
        Vector3d rB = new Vector3d(point).sub(B.position);
        Vector3d kA = new Vector3d(rA).cross(new Vector3d(normal));
        Vector3d kB = new Vector3d(rB).cross(new Vector3d(normal));

        A.velocity.add(new Vector3d(normImpulse).div(A.mass));
        B.velocity.sub(new Vector3d(normImpulse).div(B.mass));
        A.angularVelocity.add(new Vector3d(kA).mul(normalImpulse).mul(A.worldFrameInertiaInv));
        B.angularVelocity.sub(new Vector3d(kB).mul(normalImpulse).mul(B.worldFrameInertiaInv));

        for (int i = 0; i < frictionImpulses.length; i++) {
            A.velocity.add(new Vector3d(tangentialDirections[i]).mul(frictionImpulses[i] / A.mass));
            B.velocity.add(new Vector3d(tangentialDirections[i]).mul(frictionImpulses[i] / B.mass));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collision collision = (Collision) o;
        return Double.compare(collision.coefficientOfRestitution, coefficientOfRestitution) == 0 && Double.compare(collision.coefficientOfStaticFriction, coefficientOfStaticFriction) == 0 && Double.compare(collision.coefficientOfKineticFriction, coefficientOfKineticFriction) == 0 && point.equals(collision.point) && normal.equals(collision.normal) && Arrays.equals(tangentialDirections, collision.tangentialDirections);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction, point, normal);
        result = 31 * result + Arrays.hashCode(tangentialDirections);
        return result;
    }

}
