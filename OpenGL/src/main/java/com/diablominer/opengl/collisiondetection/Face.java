package com.diablominer.opengl.collisiondetection;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Face {

    private final List<Vector3f> definingVertices = new ArrayList<>();
    private final List<Vector3f> conflictList = new ArrayList<>();
    private final List<Face> neighbouringFaces = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    private final Vector3f supportVector;
    private final Vector3f normalizedNormal;
    private final float offset;

    public Face(Edge edge, Vector3f vertex3) {
        Vector3f vertex1 = edge.getTop();
        Vector3f vertex2 = edge.getTail();
        this.definingVertices.add(vertex1);
        this.definingVertices.add(vertex2);
        this.definingVertices.add(vertex3);

        edges.add(new Edge(vertex1, vertex2, this));
        edges.add(new Edge(vertex1, vertex3, this));
        edges.add(new Edge(vertex2, vertex3, this));

        supportVector = vertex2;
        Vector3f normal = new Vector3f(new Vector3f(vertex2).sub(vertex1)).cross(new Vector3f(vertex3).sub(vertex1));
        if (normal.dot(supportVector) >= 0) {
            normalizedNormal = normal.normalize();
        } else {
            normalizedNormal = normal.normalize().mul(-1.0f);
        }
        offset = normalizedNormal.dot(supportVector);
    }

    public Face(Vector3f vertex1, Vector3f vertex2, Vector3f vertex3) {
        this.definingVertices.add(vertex1);
        this.definingVertices.add(vertex2);
        this.definingVertices.add(vertex3);

        edges.add(new Edge(vertex1, vertex2, this));
        edges.add(new Edge(vertex1, vertex3, this));
        edges.add(new Edge(vertex2, vertex3, this));

        supportVector = vertex2;
        Vector3f normal = new Vector3f(new Vector3f(vertex2).sub(vertex1)).cross(new Vector3f(vertex3).sub(vertex1));
        if (normal.dot(supportVector) >= 0) {
            normalizedNormal = normal.normalize();
        } else {
            normalizedNormal = normal.normalize().mul(-1.0f);
        }
        offset = normalizedNormal.dot(supportVector);
    }

    public float signedDistance(Vector3f point) {
        return (point.dot(normalizedNormal) - offset);
    }

    public Vector3f returnFurthestPoint() {
        Vector3f result = new Vector3f(supportVector);
        for (Vector3f vertex : conflictList) {
            if (signedDistance(vertex) > signedDistance(result)) {
                result = vertex;
            }
        }
        return result;
    }

    public boolean isConflictListEmpty() {
        return conflictList.isEmpty();
    }

    public boolean hasEdge(Edge edgeToBeTested) {
        for (Edge edge : edges) {
            if (edge.isOverlapping(edgeToBeTested)) {
                return true;
            }
        }
        return false;
    }

    public void addNewConflictVertex(Vector3f vertex) {
        conflictList.add(vertex);
    }

    public void addNewConflictVertices(Collection<Vector3f> vertices) {
        conflictList.addAll(vertices);
    }

    public void addNewNeighbouringFace(Face face) {
        if (!neighbouringFaces.contains(face) && !face.equals(this)) {
            boolean isPoint0ADefiningPoint = definingVertices.contains(face.definingVertices.get(0));
            boolean isPoint1ADefiningPoint = definingVertices.contains(face.definingVertices.get(1));
            boolean isPoint2ADefiningPoint = definingVertices.contains(face.definingVertices.get(2));
            if ((isPoint0ADefiningPoint && isPoint1ADefiningPoint) || (isPoint0ADefiningPoint && isPoint2ADefiningPoint) || (isPoint1ADefiningPoint && isPoint2ADefiningPoint)) {
                neighbouringFaces.add(face);
            }
        }
    }

    public Vector3f returnCenterPoint() {
        Vector3f result = new Vector3f(0.0f);
        result.add(definingVertices.get(0)).add(definingVertices.get(1)).add(definingVertices.get(2));
        result.div(3.0f);
        return result;
    }

    public List<Face> returnNeighbouringFaces() {
        return neighbouringFaces;
    }

    public Edge returnEdgeWithNeighbouringFace(Face neighbouringFace) {
        for (Edge edge : edges) {
            for (Edge neighbourFacesEdge : neighbouringFace.edges) {
                if (edge.isOverlapping(neighbourFacesEdge)) {
                    return edge;
                }
            }
        }
        return null;
    }

    public Vector3f returnNormalizedNormal() {
        return normalizedNormal;
    }

    public List<Vector3f> returnConflictList() {
        return conflictList;
    }

    public List<Vector3f> returnDefiningVertices() {
        return definingVertices;
    }

    public void removeNeighbouringFaces(Collection<Face> toBeDeletedFaces) {
        this.neighbouringFaces.removeIf(toBeDeletedFaces::contains);
    }

    public void removeVerticesFromConflictList(Collection<Vector3f> verticesToBeRemoved) {
        conflictList.removeAll(verticesToBeRemoved);
    }

    public float returnSignedDistanceFromCenterPoint(Face face) {
        return face.signedDistance(returnCenterPoint());
    }

    private boolean isPointDuplicateOfADefiningPoint(Vector3f vertex, float epsilon) {
        for (Vector3f definingVertex : definingVertices) {
            if (definingVertex.equals(vertex, epsilon)) {
                return true;
            };
        }
        return false;
    }

    private boolean isPointDuplicateOfAConflictPoint(Vector3f point, float epsilon) {
        for (Vector3f conflictPoint : conflictList) {
            if (conflictPoint.equals(point, epsilon)) {
                return true;
            };
        }
        return false;
    }

    private boolean isFaceDuplicateOfANeighbouringFace(Face face) {
        for (Face neighbouringFace : neighbouringFaces) {
            if (neighbouringFace.equals(face)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEdgeDuplicateOfADefinedEdge(Edge edge) {
        for (Edge definedEdge : edges) {
            if (definedEdge.equals(edge)) {
                return true;
            }
        }
        return false;
    }

    public void mergeWithOtherCoplanarFace(Face coplanarFace, float epsilon) {
        for (Vector3f newDefiningPoint : coplanarFace.definingVertices) {
            if (!isPointDuplicateOfADefiningPoint(newDefiningPoint, epsilon)) {
                definingVertices.add(newDefiningPoint);
            }
        }
        for (Vector3f conflictPoint : coplanarFace.conflictList) {
            if (!isPointDuplicateOfAConflictPoint(conflictPoint, epsilon)) {
                conflictList.add(conflictPoint);
            }
        }
        for (Face neighbouringFace : coplanarFace.neighbouringFaces) {
            if (!isFaceDuplicateOfANeighbouringFace(neighbouringFace)) {
                neighbouringFace.neighbouringFaces.remove(coplanarFace);
                neighbouringFace.addNewNeighbouringFace(this);
                addNewNeighbouringFace(neighbouringFace);
            }
        }
        for (Edge edge : coplanarFace.edges) {
            if (!isEdgeDuplicateOfADefinedEdge(edge)) {
                edge.setFace(this);
                edges.add(edge);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Face face = (Face) o;
        return Float.compare(face.offset, offset) == 0 && definingVertices.equals(face.definingVertices) && supportVector.equals(face.supportVector) && normalizedNormal.equals(face.normalizedNormal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(definingVertices, supportVector, normalizedNormal, offset);
    }
}
