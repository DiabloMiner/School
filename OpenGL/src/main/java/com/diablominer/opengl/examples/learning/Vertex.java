package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4d;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Vertex {

    Vector3d position;
    Set<Vertex> neighbors;

    public Vertex(Vector3d position) {
        this.position = position;
        neighbors = new HashSet<>();
    }

    public Vertex(Vector3d position, Set<Vertex> neighbors) {
        this.position = position;
        this.neighbors = neighbors;
    }

    public void addNeighbor(Vertex vertex) {
        neighbors.add(vertex);
    }

    public void addNeighbors(List<Vertex> vertices) {
        neighbors.addAll(vertices);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return position.equals(vertex.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

}
