package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3d;

public class StandardCollision extends Collision {

    public static int roundingDigit = 15;

    public StandardCollision(Vector3d point, Vector3d normal, PhysicsComponent A, PhysicsComponent B, double timeStepTaken, double distance) {
        super(point, normal, A, B, timeStepTaken ,distance);
    }

    @Override
    public Vector3d[] generateTangentialDirections(){
        Vector3d bVelocity = B.velocity;
        Vector3d aVelocity = A.velocity;
        Vector3d deltaVelocity = Transforms.round(new Vector3d(bVelocity).sub(aVelocity), roundingDigit);
        Vector3d normalVelocity = Transforms.round(new Vector3d(normal).mul(deltaVelocity.dot(normal)), roundingDigit);
        Vector3d slidingVelocity = Transforms.round(new Vector3d(deltaVelocity).sub(normalVelocity), roundingDigit);
        Vector3d tangentialDir, tangentialDir2;
        if (!Transforms.fixZeros(slidingVelocity).equals(0.0, 0.0, 0.0)) {
            tangentialDir = slidingVelocity.normalize();
            tangentialDir2 = normal.cross(slidingVelocity, new Vector3d()).normalize();
        } else {
            if (normal.cross(0.0, 0.0, 1.0, new Vector3d()).isFinite() && !Transforms.fixZeros(normal.cross(0.0, 0.0, 1.0, new Vector3d())).equals(0.0, 0.0, 0.0)) {
                tangentialDir = normal.cross(new Vector3d(0.0, 0.0, 1.0), new Vector3d()).normalize();
                tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            } else if (normal.cross(0.0, 1.0, 0.0, new Vector3d()).isFinite() && !Transforms.fixZeros(normal.cross(0.0, 1.0, 0.0, new Vector3d())).equals(0.0, 0.0, 0.0)) {
                tangentialDir = normal.cross(new Vector3d(0.0, 1.0, 0.0), new Vector3d()).normalize();
                tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            } else if (normal.cross(1.0, 0.0, 0.0, new Vector3d()).isFinite() && !Transforms.fixZeros(normal.cross(1.0, 0.0, 0.0, new Vector3d())).equals(0.0, 0.0, 0.0)) {
                tangentialDir = normal.cross(new Vector3d(1.0, 0.0, 0.0), new Vector3d()).normalize();
                tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            } else {
                tangentialDir = normal.cross(new Vector3d(1.0), new Vector3d()).normalize();
                tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            }
        }
        return new Vector3d[] {Transforms.round(tangentialDir, roundingDigit), Transforms.round(tangentialDir2, roundingDigit)};
    }

}
