package com.diablominer.opengl.collisiondetection;

import org.joml.Vector3f;

import java.util.Objects;

public class Edge {

    private Face face;
    private final Vector3f tail;
    private final Vector3f top;

    public Edge(Vector3f tail, Vector3f top, Face face) {
        this.tail = tail;
        this.top = top;
        this.face = face;
    }

    public Edge(Vector3f tail, Vector3f top) {
        this.tail = tail;
        this.top = top;
        this.face = null;
    }

    public Vector3f getTail() {
        return tail;
    }

    public Vector3f getTop() {
        return top;
    }

    public boolean isOverlapping(Edge edge) {
        return (edge.tail.equals(this.tail) && edge.top.equals(this.top)) || (edge.tail.equals(this.top) && edge.top.equals(this.tail));
    }

    public Vector3f isColliding(Edge edge, float epsilon) {
        Vector3f e = new Vector3f(tail).sub(top).normalize();
        Vector3f f = new Vector3f(edge.tail).sub(edge.top).normalize();
        Vector3f g = new Vector3f(edge.top).sub(this.top);

        Vector3f fCrossG = new Vector3f(f).cross(g);
        Vector3f fCrossE = new Vector3f(f).cross(e);
        float h = fCrossG.length();
        float k = fCrossE.length();

        if (h > epsilon && k > epsilon) {
            Vector3f point;
            Vector3f l = new Vector3f(e).mul(h / k);
            if (Math.signum(fCrossG.x) == Math.signum(fCrossE.x) && Math.signum(fCrossG.y) == Math.signum(fCrossE.y) && Math.signum(fCrossG.z) == Math.signum(fCrossE.z)) {
                point = new Vector3f(this.top).add(l);
            } else {
                point = new Vector3f(this.top).sub(l);
            }
            if (isPointBetweenPoints(point, top, tail, epsilon)) {
                return point;
            }
        }
        return new Vector3f(0.0f);
    }

    public boolean isPointBetweenPoints(Vector3f pointToBeTested, Vector3f edgePoint1, Vector3f edgePoint2, float epsilon) {
        float d1 = new Vector3f(edgePoint1).sub(pointToBeTested).length();
        float d2 = new Vector3f(edgePoint2).sub(pointToBeTested).length();
        float edgePointDistance = new Vector3f(edgePoint1).sub(edgePoint2).length();

        return ((d1 + d2) - edgePointDistance) <= epsilon;
    }

    public Face getFace() {
        return face;
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
