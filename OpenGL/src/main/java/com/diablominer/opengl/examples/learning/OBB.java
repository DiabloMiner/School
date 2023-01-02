package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4d;
import org.joml.Vector3d;

public class OBB implements CollisionShape {

    public static final double epsilon = 1e-15;

    public Matrix4d worldMatrix;
    public Vector3d halflengths;

    public OBB(Matrix4d worldMatrix, Vector3d halflengths) {
        this.worldMatrix = new Matrix4d(worldMatrix);
        this.halflengths = new Vector3d(halflengths);
    }

    @Override
    public void update(Matrix4d worldMatrix) {
        this.worldMatrix.set(worldMatrix);
    }

    @Override
    public boolean isColliding(CollisionShape shape) {
        if (shape instanceof Sphere) {
            return isColliding((Sphere) shape);
        } else {
            return false;
        }
    }

    public boolean isColliding(Sphere sphere) {
        Vector3d sphereCenter = Transforms.mulVectorWithMatrix4(sphere.position, worldMatrix.invert(new Matrix4d()));
        Vector3d closestPoint = new Vector3d(Math.max(-halflengths.x, Math.min(sphereCenter.x, halflengths.x)),
                Math.max(-halflengths.y, Math.min(sphereCenter.y, halflengths.y)),
                Math.max(-halflengths.z, Math.min(sphereCenter.z, halflengths.z)));
        if (closestPoint.equals(sphereCenter)) {
            return true;
        }
        return (closestPoint.distance(sphereCenter) - sphere.radius) <= epsilon;
    }

    @Override
    public Vector3d findPenetrationDepth(CollisionShape shape) {
        if (shape instanceof Sphere) {
            return findPenetrationDepth((Sphere) shape);
        } else {
            return null;
        }
    }

    public Vector3d findPenetrationDepth(Sphere sphere) {
        Vector3d sphereCenter = Transforms.mulVectorWithMatrix4(sphere.position, worldMatrix.invert(new Matrix4d()));
        Vector3d closestPoint = new Vector3d(Math.max(-halflengths.x, Math.min(sphereCenter.x, halflengths.x)),
                Math.max(-halflengths.y, Math.min(sphereCenter.y, halflengths.y)),
                Math.max(-halflengths.z, Math.min(sphereCenter.z, halflengths.z)));
        Vector3d spherePoint;
        if (closestPoint.equals(sphereCenter)) {
            Vector3d[] faceCenters = {new Vector3d(halflengths.x, 0.0, 0.0), new Vector3d(0.0, halflengths.y, 0.0), new Vector3d(0.0, 0.0, halflengths.z),
                new Vector3d(-halflengths.x, 0.0, 0.0), new Vector3d(0.0, -halflengths.y, 0.0), new Vector3d(0.0, 0.0, -halflengths.z)};
            Vector3d closestFaceCenter = faceCenters[0];
            for (int i = 1; i < faceCenters.length; i++) {
                if (closestPoint.distance(faceCenters[i]) < closestPoint.distance(closestFaceCenter)) {
                    closestFaceCenter = faceCenters[i];
                }
            }
            Vector3d a = closestFaceCenter.normalize(new Vector3d()).sub(new Vector3d(1.0)).negate(new Vector3d());
            closestPoint = a.mul(closestPoint).add(closestFaceCenter);
            Vector3d dir = closestPoint.sub(sphereCenter, new Vector3d()).normalize();
            spherePoint = sphereCenter.sub(dir.mul(sphere.radius), new Vector3d());
        } else {
            spherePoint = new Vector3d(closestPoint).sub(sphereCenter).normalize().mul(sphere.radius).add(sphereCenter);
        }
        if (closestPoint.distance(sphereCenter) < spherePoint.distance(sphereCenter)) {
            return Transforms.mulVectorWithMatrix4(spherePoint, worldMatrix).sub(Transforms.mulVectorWithMatrix4(closestPoint, worldMatrix));
        } else {
            return Transforms.mulVectorWithMatrix4(closestPoint, worldMatrix).sub(Transforms.mulVectorWithMatrix4(spherePoint, worldMatrix));
        }
    }

    @Override
    public Vector3d[] findClosestPoints(CollisionShape shape) {
        if (shape instanceof Sphere) {
            return findClosestPoints((Sphere) shape);
        } else {
            return null;
        }
    }

    public Vector3d[] findClosestPoints(Sphere sphere) {
        Vector3d sphereCenter = Transforms.mulVectorWithMatrix4(sphere.position, worldMatrix.invert(new Matrix4d()));
        Vector3d closestPoint = new Vector3d(Math.max(-halflengths.x, Math.min(sphereCenter.x, halflengths.x)),
                Math.max(-halflengths.y, Math.min(sphereCenter.y, halflengths.y)),
                Math.max(-halflengths.z, Math.min(sphereCenter.z, halflengths.z)));
        Vector3d spherePoint;
        if (closestPoint.equals(sphereCenter)) {
            Vector3d[] faceCenters = {new Vector3d(halflengths.x, 0.0, 0.0), new Vector3d(0.0, halflengths.y, 0.0), new Vector3d(0.0, 0.0, halflengths.z),
                    new Vector3d(-halflengths.x, 0.0, 0.0), new Vector3d(0.0, -halflengths.y, 0.0), new Vector3d(0.0, 0.0, -halflengths.z)};
            Vector3d closestFaceCenter = faceCenters[0];
            for (int i = 1; i < faceCenters.length; i++) {
                if (closestPoint.distance(faceCenters[i]) < closestPoint.distance(closestFaceCenter)) {
                    closestFaceCenter = faceCenters[i];
                }
            }
            Vector3d a = closestFaceCenter.normalize(new Vector3d()).sub(new Vector3d(1.0)).negate(new Vector3d());
            closestPoint = a.mul(closestPoint).add(closestFaceCenter);
            Vector3d dir = closestPoint.sub(sphereCenter, new Vector3d()).normalize();
            spherePoint = sphereCenter.sub(dir.mul(sphere.radius), new Vector3d());
        } else {
            spherePoint = new Vector3d(closestPoint).sub(sphereCenter).normalize().mul(sphere.radius).add(sphereCenter);
        }
        return new Vector3d[] {Transforms.mulVectorWithMatrix4(closestPoint, worldMatrix), Transforms.mulVectorWithMatrix4(spherePoint, worldMatrix)};
    }

    @Override
    public Vector3d getSupportingPoint(Vector3d direction) {
        return null;
    }

}
