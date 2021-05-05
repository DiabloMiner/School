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

    public Vector3f getTail() {
        return tail;
    }

    public Vector3f getTop() {
        return top;
    }

    public boolean isOverlapping(Edge edge) {
        return (edge.tail.equals(this.tail) && edge.top.equals(this.top)) || (edge.tail.equals(this.top) && edge.top.equals(this.tail));
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
