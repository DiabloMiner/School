package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3d;

public class StandardCollision extends Collision {

    public StandardCollision(Vector3d point, Vector3d normal, PhysicsComponent A, PhysicsComponent B, double timeStepTaken) {
        super(point, normal, A, B, timeStepTaken);
    }

    @Override
    public Vector3d[] generateTangentialDirections(){
        Vector3d bVelocity = B.velocity.add(B.angularVelocity.cross(new Vector3d(point).sub(B.position), new Vector3d()), new Vector3d());
        Vector3d aVelocity = A.velocity.add(A.angularVelocity.cross(new Vector3d(point).sub(A.position), new Vector3d()), new Vector3d());
        Vector3d deltaVelocity = new Vector3d(bVelocity).sub(aVelocity);
        Vector3d normalVelocity = new Vector3d(normal).mul(deltaVelocity.dot(normal));
        Vector3d slidingVelocity = new Vector3d(deltaVelocity).sub(normalVelocity);
        if (!Transforms.fixZeros(slidingVelocity).equals(0.0, 0.0, 0.0)) {
            return new Vector3d[] {slidingVelocity.normalize(), normal.cross(slidingVelocity, new Vector3d()).normalize()};
        } else {
            if (normal.cross(0.0, 0.0, 1.0, new Vector3d()).isFinite() && !normal.cross(0.0, 0.0, 1.0, new Vector3d()).equals(0.0, 0.0, 0.0)) {
                Vector3d tangentialDir = normal.cross(new Vector3d(0.0, 0.0, 1.0), new Vector3d()).normalize();
                Vector3d tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            } else if (normal.cross(0.0, 1.0, 0.0, new Vector3d()).isFinite() && !normal.cross(0.0, 1.0, 0.0, new Vector3d()).equals(0.0, 0.0, 0.0)) {
                Vector3d tangentialDir = normal.cross(new Vector3d(0.0, 1.0, 0.0), new Vector3d()).normalize();
                Vector3d tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            } else if (normal.cross(1.0, 0.0, 0.0, new Vector3d()).isFinite() && !normal.cross(1.0, 0.0, 0.0, new Vector3d()).equals(0.0, 0.0, 0.0)) {
                Vector3d tangentialDir = normal.cross(new Vector3d(1.0, 0.0, 0.0), new Vector3d()).normalize();
                Vector3d tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            } else {
                Vector3d tangentialDir = normal.cross(new Vector3d(1.0), new Vector3d()).normalize();
                Vector3d tangentialDir2 = normal.cross(tangentialDir, new Vector3d()).normalize();
                return new Vector3d[] {tangentialDir, tangentialDir2};
            }
        }
    }

}
