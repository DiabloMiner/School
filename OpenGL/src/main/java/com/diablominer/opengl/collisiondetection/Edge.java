package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Objects;

public class Edge {

    private final static float epsilon = Math.ulp(1.0f);

    private Face face;
    private final Vector3f tail, originalTail;
    private final Vector3f top, originalTop;

    public enum OverlappingType {
        Overlapping,
        Identical,
        None;
    }

    public Edge(Vector3f tail, Vector3f top, Face face) {
        this.tail = new Vector3f(tail);
        this.top = new Vector3f(top);
        originalTail = new Vector3f(tail);
        originalTop = new Vector3f(top);
        this.face = face;
    }

    public Edge(Vector3f tail, Vector3f top) {
        this.tail = new Vector3f(tail);
        this.top = new Vector3f(top);
        originalTail = new Vector3f(tail);
        originalTop = new Vector3f(top);
        this.face = null;
    }

    public OverlappingType isOverlapping(Edge edge) {
        if ((edge.tail.equals(this.tail) && edge.top.equals(this.top)) || (edge.tail.equals(this.top) && edge.top.equals(this.tail))) {
            return OverlappingType.Identical;
        } else if (hasSameDirection(edge) && (isPointInEdge(edge.tail) || isPointInEdge(edge.top))) {
            return OverlappingType.Overlapping;
        } else {
            return OverlappingType.None;
        }
    }

    public Vector3f isColliding(Edge edge) {
        OverlappingType overlappingType = isOverlapping(edge);
        if (overlappingType.equals(OverlappingType.None)) {
            Vector3f e = new Vector3f(tail).sub(top).normalize();
            Vector3f f = new Vector3f(edge.tail).sub(edge.top).normalize();
            Vector3f g = new Vector3f(edge.top).sub(this.top);

            Vector3f fCrossG = new Vector3f(f).cross(g);
            Vector3f fCrossE = new Vector3f(f).cross(e);
            float h = fCrossG.length();
            float k = fCrossE.length();

            if (h > -epsilon && k > -epsilon) {
                Vector3f point;
                Vector3f l = new Vector3f(e).mul(h / k);
                if (Math.signum(fCrossG.x) == Math.signum(fCrossE.x) && Math.signum(fCrossG.y) == Math.signum(fCrossE.y) && Math.signum(fCrossG.z) == Math.signum(fCrossE.z)) {
                    point = new Vector3f(this.top).add(l);
                } else {
                    point = new Vector3f(this.top).sub(l);
                }
                if (isPointInEdge(point) && edge.isPointInEdge(point)) {
                    return point;
                }
            }
            return new Vector3f(0.0f);
        } else if (overlappingType.equals(OverlappingType.Identical)) {
            return getMiddlePoint();
        } else {
            if (isPointInEdge(edge.top)) {
                return new Vector3f(edge.top);
            } else {
                return new Vector3f(edge.tail);
            }
        }
    }

    public void update(Matrix4f worldMatrix) {
        this.tail.set(Transforms.mulVectorWithMatrix4(originalTail, worldMatrix));
        this.top.set(Transforms.mulVectorWithMatrix4(originalTop, worldMatrix));
    }

    public boolean hasSameDirection(Edge edge) {
        Vector3f dir = getEdgeDirection();
        Vector3f edgeDir = edge.getEdgeDirection();
        Vector3f edgeAltDir = new Vector3f(edge.getEdgeDirection()).mul(-1.0f);

        return (dir.equals(edgeDir) || dir.equals(edgeAltDir));
    }

    public Vector3f getEdgeDirection() {
        return new Vector3f(top).sub(tail);
    }

    public boolean isPointInEdge(Vector3f pointToBeTested) {
        float d1 = new Vector3f(top).sub(pointToBeTested).length();
        float d2 = new Vector3f(tail).sub(pointToBeTested).length();
        float edgePointDistance = new Vector3f(top).sub(tail).length();

        return Math.abs((d1 + d2) - edgePointDistance) <= epsilon;
    }

    public Vector3f getMiddlePoint() {
        return new Vector3f(top).add(tail).div(2.0f);
    }

    public Face getFace() {
        return face;
    }

    public Vector3f getTail() {
        return tail;
    }

    public Vector3f getTop() {
        return top;
    }

    public void setFace(Face face) {
        this.face = face;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return face.equals(edge.face) && tail.equals(edge.tail) && top.equals(edge.top);
    }

    @Override
    public int hashCode() {
        return Objects.hash(face, tail, top);
    }
}
