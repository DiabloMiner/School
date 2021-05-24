package com.diablominer.opengl.collisiondetection;

import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.List;

public class OBBTreeNode {

    private Vector3f centerPoint;
    private Vector3f[] sideDirectionVectors = new Vector3f[3];
    private Vector3f[] halfLengthVectors = new Vector3f[3];

    private QuickHull quickHull;
    private List<Vector3f> points;

    public OBBTreeNode(List<Vector3f> points) {
        this.points = points;
        quickHull = new QuickHull(points);

        Matrix3f covarianceMatrix = new Matrix3f(computeCovarianceMatrixValue(0, 0), computeCovarianceMatrixValue(1, 0), computeCovarianceMatrixValue(2, 0),
                                                 computeCovarianceMatrixValue(0, 1), computeCovarianceMatrixValue(1, 1), computeCovarianceMatrixValue(2, 1),
                                                 computeCovarianceMatrixValue(0, 2), computeCovarianceMatrixValue(1, 2), computeCovarianceMatrixValue(2, 2));
    }

    private float computeCovarianceMatrixValue(int i, int j) {
        float finalValue = 0.0f;
        for (Face face : quickHull.getFaces()) {
            Vector3f centroid = face.getCentroid();
            Vector3f vertex0 = face.getDefiningVertices().get(0);
            Vector3f vertex1 = face.getDefiningVertices().get(1);
            Vector3f vertex2 = face.getDefiningVertices().get(2);
            finalValue += (face.getArea() / 12.0f) * ((9.0f * centroid.get(i) * centroid.get(j) +
                    vertex0.get(i) * vertex0.get(j) + vertex1.get(i) * vertex1.get(j) + vertex2.get(i) * vertex2.get(j))
                    - quickHull.getCentroid().get(i) * quickHull.getCentroid().get(j));
        }
        return (finalValue / quickHull.getArea());
    }

}
