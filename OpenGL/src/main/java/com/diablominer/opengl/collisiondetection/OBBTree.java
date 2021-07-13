package com.diablominer.opengl.collisiondetection;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class OBBTree {

    private final OBBTreeNode[] nodes;

    public OBBTree(List<Vector3f> points, int levels) {
        int size = (1 << levels) - 1;
        if (size < 0) {
            throw new IllegalArgumentException("The argument chosen for levels caused the size of the array to overflow. Please choose a smaller argument.");
        }
        nodes = new OBBTreeNode[size];

        for (int i = 0; i <= ((size - 1 - 2) / 2); i++) {
            if (i == 0) {
                nodes[0] = new OBBTreeNode(points);
                ArrayList<ArrayList<Vector3f>> twoPointSets = splitPointsInHalf(nodes[0]);
                nodes[1] = new OBBTreeNode(twoPointSets.get(0));
                nodes[2] = new OBBTreeNode(twoPointSets.get(1));
            } else {
                ArrayList<ArrayList<Vector3f>> twoPointSets = splitPointsInHalf(nodes[(int) Math.floor((i - 1) / 2.0)]);
                nodes[2 * i + 1] = new OBBTreeNode(twoPointSets.get(0));
                nodes[2 * i + 2] = new OBBTreeNode(twoPointSets.get(1));
            }
        }
    }

    private ArrayList<ArrayList<Vector3f>> splitPointsInHalf(OBBTreeNode obbTreeNode) {
        Vector3f centerPoint = obbTreeNode.getCenterPoint();
        Vector3f normalVector = obbTreeNode.getMiddleAxis();

        if (centerPoint.dot(normalVector) >= 0) {
            normalVector.normalize();
        } else {
            normalVector.normalize().mul(-1.0f);
        }
        float distance = centerPoint.dot(normalVector);

        ArrayList<Vector3f> positiveSide = new ArrayList<>();
        ArrayList<Vector3f> negativeSide = new ArrayList<>();
        for (Vector3f point : obbTreeNode.getPoints()) {
            if ((point.dot(normalVector) - distance) >= 0.0f) {
                positiveSide.add(point);
            } else {
                negativeSide.add(point);
            }
        }

        ArrayList<ArrayList<Vector3f>> finalList = new ArrayList<>();
        finalList.add(positiveSide);
        finalList.add(negativeSide);
        return finalList;
    }

    public OBBTreeNode[] getNodes() {
        return nodes;
    }

}
