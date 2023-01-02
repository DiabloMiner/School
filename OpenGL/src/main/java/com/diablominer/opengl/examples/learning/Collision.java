package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.Objects;

public abstract class Collision {

    public double coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction, coefficientOfRollingFriction;
    public Vector3d point, normal;
    public Vector3d[] tangentialDirections;
    public PhysicsComponent A, B;
    public double timeStepTaken;

    public Collision(Vector3d point, Vector3d normal, PhysicsComponent A, PhysicsComponent B, double timeStepTaken) {
        this.A = A;
        this.B = B;

        this.point = point;
        // The difference of the positions of A and B is projected onto the normal to find the correct sign of the direction
        // The resulting collision direction is then normalized
        this.normal = new Vector3d(normal).mul((new Vector3d(A.position).sub(B.position)).dot(normal)).normalize();
        this.tangentialDirections = generateTangentialDirections();
        this.coefficientOfRestitution = Material.coefficientsOfRestitution.get(Material.hash(A.material, B.material));
        this.coefficientOfStaticFriction = Material.coefficientsOfStaticFriction.get(Material.hash(A.material, B.material));
        this.coefficientOfKineticFriction = Material.coefficientsOfKineticFriction.get(Material.hash(A.material, B.material));
        this.coefficientOfRollingFriction = Material.coefficientsOfRollingFriction.get(Material.hash(A.material, B.material));
        this.timeStepTaken = timeStepTaken;
    }

    public abstract Vector3d[] generateTangentialDirections();

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

    public boolean isColliding(double timeStep) {
        if (getRelativeVelocity(timeStep) < 0) {
            return A.isColliding(B);
        } else {
            return false;
        }
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

    /**
     * @param frictionImpulses "Normal"/Coulomb friction impulses
     * @param rollingFrictionImpulses Rolling friction impulses (The number of tangential directions should match up with the size of this array)
     */
    public void applyImpulse(double normalImpulse, double[] frictionImpulses, double[] rollingFrictionImpulses, int roundingDigit) {
        Vector3d normImpulse = new Vector3d(normal).mul(normalImpulse);
        Vector3d rA = new Vector3d(point).sub(A.position);
        Vector3d rB = new Vector3d(point).sub(B.position);
        Vector3d kA = Transforms.safeNormalize(new Vector3d(rA).cross(new Vector3d(normal)));
        Vector3d kB = Transforms.safeNormalize(new Vector3d(rB).cross(new Vector3d(normal)));

        // Add normal impulses
        if (A.objectType.performTimeStep) {
            A.velocity.add(Transforms.round(new Vector3d(normImpulse).div(A.mass), roundingDigit));
            A.angularVelocity.add(Transforms.round(new Vector3d(kA).mul(normalImpulse).mul(A.worldFrameInertiaInv), roundingDigit));
        }
        if (B.objectType.performTimeStep) {
            B.velocity.sub(Transforms.round(new Vector3d(normImpulse).div(B.mass), roundingDigit));
            B.angularVelocity.sub(Transforms.round(new Vector3d(kB).mul(normalImpulse).mul(B.worldFrameInertiaInv), roundingDigit));
        }

        // Add Coulomb friction impulses
        for (int i = 0; i < frictionImpulses.length; i++) {
            if (A.objectType.performTimeStep) {
                A.velocity.add(Transforms.round(new Vector3d(tangentialDirections[i]).mul(frictionImpulses[i] / A.mass), roundingDigit));
                A.angularVelocity.add(Transforms.round(rA.cross(tangentialDirections[i], new Vector3d()).mul(frictionImpulses[i]).mul(A.worldFrameInertiaInv), roundingDigit));
            }
            if (B.objectType.performTimeStep) {
                B.velocity.add(Transforms.round(new Vector3d(tangentialDirections[i]).mul(frictionImpulses[i] / B.mass), roundingDigit));
                B.angularVelocity.add(Transforms.round(rB.cross(tangentialDirections[i], new Vector3d()).mul(frictionImpulses[i]).mul(B.worldFrameInertiaInv), roundingDigit));
            }
        }

        // Add rolling friction impulses
        for (int i = 0; i < rollingFrictionImpulses.length; i++) {
            if (A.objectType.performTimeStep) {
                A.angularVelocity.add(Transforms.round(new Vector3d(tangentialDirections[i]).mul(rollingFrictionImpulses[i]).mul(A.worldFrameInertiaInv), roundingDigit));
            }
            if (B.objectType.performTimeStep) {
                B.angularVelocity.add(Transforms.round(new Vector3d(tangentialDirections[i]).mul(rollingFrictionImpulses[i]).mul(B.worldFrameInertiaInv), roundingDigit));
            }
        }
    }

    public double getRelativeVelocity(double timeStep) {
        // This implements equation 14 in 'Contact and Friction Simulation for Computer Graphics', but added velocity in the timestep to be taken is taken into account
        Vector3d bVelocity = (B.velocity.add(B.force.mul(timeStep, new Vector3d()), new Vector3d())).add((B.angularVelocity.add(B.torque.mul(timeStep, new Vector3d()), new Vector3d())).cross(new Vector3d(point).sub(B.position)), new Vector3d());
        Vector3d aVelocity = (A.velocity.add(A.force.mul(timeStep, new Vector3d()), new Vector3d())).add((A.angularVelocity.add(A.torque.mul(timeStep, new Vector3d()), new Vector3d())).cross(new Vector3d(point).sub(A.position)), new Vector3d());
        return normal.negate(new Vector3d()).dot(bVelocity.sub(aVelocity));
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
