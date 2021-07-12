package com.diablominer.opengl.collisiondetection;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class OBBTree {

    private OBBTreeNode[] nodes;

    public OBBTree(List<Vector3f> points, int levels) {
        int size = 0x2 << levels;
        if (size < 0) {
            throw new IllegalArgumentException("The argument chosen for levels caused the size of the array to overflow. Please choose a smaller argument.");
        }
        nodes = new OBBTreeNode[size];

        nodes[0] = new OBBTreeNode(points);
        ArrayList<ArrayList<Vector3f>> twoPointCollections = splitPointsInHalf(nodes[0], nodes[0].getPoints());
        nodes[1] = new OBBTreeNode(twoPointCollections.get(0));
        nodes[2] = new OBBTreeNode(twoPointCollections.get(1));
    }

    private ArrayList<ArrayList<Vector3f>> splitPointsInHalf(OBBTreeNode obbTreeNode, List<Vector3f> points) {
        Vector3f centerPoint = obbTreeNode.getCenterPoint();
        Vector3f longestAxis = obbTreeNode.getLongestAxis();
        Vector3f middleAxis = obbTreeNode.getMiddleAxis();

        Vector3f secondAxis = new Vector3f(longestAxis).cross(middleAxis);
        Vector3f normalVector = new Vector3f(longestAxis).cross(secondAxis);
        if (centerPoint.dot(normalVector) >= 0) {
            normalVector.normalize();
        } else {
            normalVector.normalize().mul(-1.0f);
        }
        float distance = centerPoint.dot(normalVector);

        ArrayList<Vector3f> positiveSide = new ArrayList<>();
        ArrayList<Vector3f> negativeSide = new ArrayList<>();
        for (Vector3f point : points) {
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

}
