package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

public class Edge {

    private Face face;
    private final Vector3f tail, originalTail;
    private final Vector3f top, originalTop;

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

    public boolean isOverlapping(Edge edge) {
        return (edge.tail.equals(this.tail) && edge.top.equals(this.top)) || (edge.tail.equals(this.top) && edge.top.equals(this.tail));
    }

    public Vector3f isColliding(Edge edge, float epsilon) {
        if (!isOverlapping(edge)) {
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
                if (isPointInEdge(point, epsilon) && edge.isPointInEdge(point, epsilon)) {
                    return point;
                }
            }
            return new Vector3f(0.0f);
        } else {
            return getMiddlePoint();
        }
    }

    public void update(Matrix4f worldMatrix) {
        this.tail.set(Transforms.mulVectorWithMatrix4(originalTail, worldMatrix));
        this.top.set(Transforms.mulVectorWithMatrix4(originalTop, worldMatrix));
    }

    public Vector3f getEdgeDirection() {
        return new Vector3f(top).sub(tail);
    }

    public boolean isPointInEdge(Vector3f pointToBeTested, float epsilon) {
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
