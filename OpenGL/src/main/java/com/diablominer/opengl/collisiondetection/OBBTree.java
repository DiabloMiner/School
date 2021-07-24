package com.diablominer.opengl.collisiondetection;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OBBTree {

    private final OBBTreeNode[] nodes;
    private final int levels;
    private final List<OBBTreeNode> collisionNodes;

    public OBBTree(List<Vector3f> points, int levels) {
        this.levels = levels;
        collisionNodes = new ArrayList<>();

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
        for (Vector3f point : obbTreeNode.getOriginalPoints()) {
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

    private OBBTreeNode[] getChildren(OBBTreeNode obbTreeNode) {
        if (Arrays.asList(nodes).contains(obbTreeNode)) {
            List<OBBTreeNode> obbTreeNodes = new ArrayList<>();
            addChildrenToList(obbTreeNodes, obbTreeNode);
            return obbTreeNodes.toArray(OBBTreeNode[]::new);
        } else {
            throw new Error("The obbtreenode for which children where requested isn't in the nodes array of this obbtree");
        }
    }

    private void addChildrenToList(List<OBBTreeNode> obbTreeNodes, OBBTreeNode obbTreeNode) {
        int index = Arrays.asList(nodes).indexOf(obbTreeNode);
        if ((2 * index + 2) <= (nodes.length - 1)) {
            obbTreeNodes.add(nodes[2 * index + 1]);
            obbTreeNodes.add(nodes[2 * index + 2]);
            addChildrenToList(obbTreeNodes, nodes[2 * index + 1]);
            addChildrenToList(obbTreeNodes, nodes[2 * index + 2]);
        }
    }

    public boolean isColliding(OBBTree otherTree, Matrix4f thisMatrix, Matrix4f otherMatrix) {
        collisionNodes.clear();
        return isColliding(otherTree, this.getNodes()[0], otherTree.getNodes()[0], thisMatrix, otherMatrix);
    }

    private boolean isColliding(OBBTree otherTree, OBBTreeNode thisNode, OBBTreeNode otherNode, Matrix4f thisMatrix, Matrix4f otherMatrix) {
        if (Arrays.asList(nodes).indexOf(thisNode) >= (1 << levels) - 3 && Arrays.asList(otherTree.nodes).indexOf(otherNode) >= (1 << otherTree.levels) - 3) {
            if (thisNode.isColliding(otherNode, thisMatrix, otherMatrix)) {
                collisionNodes.add(thisNode);
                collisionNodes.add(otherNode);
                return true;
            }
        } else if (thisNode.isColliding(otherNode, thisMatrix, otherMatrix)) {
            if (thisNode.getVolume() > otherNode.getVolume()) {
                for (OBBTreeNode child : getChildren(thisNode)) {
                    if (isColliding(otherTree, child, otherNode, thisMatrix, otherMatrix)) {
                        return true;
                    }
                }
            } else {
                for (OBBTreeNode child : otherTree.getChildren(otherNode)) {
                    if (otherTree.isColliding(this, child, thisNode, otherMatrix, thisMatrix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public OBBTreeNode[] getNodes() {
        return nodes;
    }

}
