package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3d;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class Collision {

    public double coefficientOfRestitution, coefficientOfStaticFriction, coefficientOfKineticFriction, coefficientOfRollingFriction;
    public Vector3d point, normal, collisionDirection;
    public Vector3d[] tangentialDirections;
    public PhysicsComponent A, B;
    public double collisionTime, distance;

    public Collision(Vector3d point, Vector3d normal, PhysicsComponent A, PhysicsComponent B, double collisionTime, double distance) {
        this.A = A;
        this.B = B;

        this.point = point;
        // The difference of the positions of A and B is projected onto the normal to find the correct sign of the direction
        // The resulting normal direction is then normalized
        this.normal = new Vector3d(normal).mul((new Vector3d(A.position).sub(B.position)).dot(normal)).normalize();
        this.collisionDirection = new Vector3d(normal).normalize();
        this.tangentialDirections = generateTangentialDirections();
        this.coefficientOfRestitution = Material.coefficientsOfRestitution.get(Material.hash(A.material, B.material));
        this.coefficientOfStaticFriction = Material.coefficientsOfStaticFriction.get(Material.hash(A.material, B.material));
        this.coefficientOfKineticFriction = Material.coefficientsOfKineticFriction.get(Material.hash(A.material, B.material));
        this.coefficientOfRollingFriction = Material.coefficientsOfRollingFriction.get(Material.hash(A.material, B.material));
        this.collisionTime = collisionTime;
        this.distance = distance;
    }

    public abstract Vector3d[] generateTangentialDirections();

    public double updateTimeStepEstimate(double maximumTime, double currentTime, double collisionTimeEpsilon, int roundingDigit) {
        // Collision direction should point from a to b
        double deltaF = new Vector3d(B.force).div(B.mass).sub(new Vector3d(A.force).div(A.mass)).dot(collisionDirection);
        double deltaV = new Vector3d(B.velocity).sub(A.velocity).dot(collisionDirection);
        double localTimeStep = 0.0;
        if (deltaF != 0.0 && deltaF != -0.0) {
            localTimeStep = Transforms.chooseSuitableSolution(collisionTimeEpsilon, (maximumTime - currentTime), 0.0, Transforms.solveQuadraticEquation(deltaF, deltaV, distance, roundingDigit));
        } else if (deltaV != 0.0 && deltaV != 0.0) {
            localTimeStep = -distance / deltaV;
        }
        return localTimeStep;
    }

    public void updateCollisionTime(double deltaTime) {
        collisionTime += deltaTime;
    }

    public void updateCollisionNormal(double epsilon, int roundingDigit) {
        collisionDirection = B.collisionShape.findPenetrationDepth(A.collisionShape);
        distance = Transforms.round(collisionDirection.length(), roundingDigit);
        if (collisionDirection.equals(new Vector3d(0.0), epsilon)) {
            Vector3d[] closestPoints = A.collisionShape.findClosestPoints(B.collisionShape);
            collisionDirection = new Vector3d(closestPoints[0]).sub(A.position);
        }
        collisionDirection.normalize();
    }

    public boolean isDistanceZero(double epsilon) {
        return distance < epsilon && distance > -epsilon;
    }

    public void finalizeCollisionData() {
        Vector3d[] closestPoints = A.collisionShape.findClosestPoints(B.collisionShape);
        point.set((closestPoints[0]).add(closestPoints[1]).div(2.0));
        if (!Transforms.fixZeros(collisionDirection).equals(Transforms.fixZeros(normal))) {
            normal.set(new Vector3d(collisionDirection).mul((new Vector3d(A.position).sub(B.position)).dot(collisionDirection)).normalize());
            tangentialDirections = generateTangentialDirections();
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

    public void applyImpulse(double normalImpulse, double[] frictionImpulses, double[] rollingFrictionImpulses, int roundingDigit) {
        Vector3d normImpulse = new Vector3d(normal).mul(normalImpulse);
        Vector3d rA = Transforms.round(new Vector3d(point).sub(A.position), roundingDigit);
        Vector3d rB = Transforms.round(new Vector3d(point).sub(B.position), roundingDigit);
        Vector3d kA = Transforms.safeNormalize(Transforms.round(new Vector3d(rA).cross(new Vector3d(normal)), 6));
        Vector3d kB = Transforms.safeNormalize(Transforms.round(new Vector3d(rB).cross(new Vector3d(normal)), 6));

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

        // Add rolling resistance impulses
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
        Vector3d normal = new Vector3d(this.normal).mul((new Vector3d(A.position).sub(B.position)).dot(this.normal)).normalize();
        return normal.negate(new Vector3d()).dot(bVelocity.sub(aVelocity));
    }

    public double getRelativeLinearVelocity(double timeStep) {
        Vector3d bVelocity = (B.velocity.add(B.force.mul(timeStep, new Vector3d()), new Vector3d()));
        Vector3d aVelocity = (A.velocity.add(A.force.mul(timeStep, new Vector3d()), new Vector3d()));
        Vector3d normal = new Vector3d(this.normal).mul((new Vector3d(A.position).sub(B.position)).dot(this.normal)).normalize();
        return normal.negate(new Vector3d()).dot(bVelocity.sub(aVelocity));
    }

    public double getRelativeForce() {
        Vector3d normal = new Vector3d(this.normal).mul((new Vector3d(A.position).sub(B.position)).dot(this.normal)).normalize();
        return normal.negate(new Vector3d()).dot(new Vector3d(B.force).sub(new Vector3d(A.force)));
    }

    public boolean containsActivePhysicsComponents(PhysicsComponent A, PhysicsComponent B) {
        if (A.objectType.performTimeStep && !B.objectType.performTimeStep) {
            return A.equals(this.A) || A.equals(this.B);
        } else if (!A.objectType.performTimeStep && B.objectType.performTimeStep) {
            return B.equals(this.A) || B.equals(this.B);
        } else {
            return (A.equals(this.A) || A.equals(this.B)) || (B.equals(this.A) || B.equals(this.B));
        }
    }

    public boolean containsActivePhysicsComponents(List<PhysicsComponent> aComps, List<PhysicsComponent> bComps) {
        for (int i = 0; i < aComps.size(); i++) {
            if (containsActivePhysicsComponents(aComps.get(i), bComps.get(i))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasStaticPhysicsComponents() {
        return !A.objectType.performTimeStep || !B.objectType.performTimeStep;
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
