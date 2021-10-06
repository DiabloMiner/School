package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

public class Face {

    public static final float epsilon = Math.ulp(1.0f);

    private final List<Vector3f> definingVertices = new ArrayList<>();
    private final List<Vector3f> conflictList = new ArrayList<>();
    private final List<Face> neighbouringFaces = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    private final Vector3f supportVector;
    private final Vector3f normalizedNormal;
    private float offset;

    private final Vector3f centroid;
    private float area;

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

        centroid = determineCentroid();
        area = determineArea();
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

        centroid = determineCentroid();
        area = determineArea();
    }

    public float signedDistance(Vector3f point) {
        return (point.dot(normalizedNormal) - offset);
    }

    public float signedDistancePlane(Vector3f normalVector, float distance) {
        return new Vector3f(centroid).dot(normalVector) - distance;
    }

    public Vector3f getFurthestPointFromConflictList() {
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

    private Vector3f determineCentroid() {
        Vector3f result = new Vector3f(0.0f);
        result.add(definingVertices.get(0)).add(definingVertices.get(1)).add(definingVertices.get(2));
        result.div(3.0f);
        return result;
    }

    private float determineArea() {
        float a = new Vector3f(definingVertices.get(1)).sub(definingVertices.get(2)).length();
        float heightOnA = new Vector3f(definingVertices.get(0)).sub(definingVertices.get(1)).
                cross(new Vector3f(definingVertices.get(0)).sub(definingVertices.get(2))).length() /
                new Vector3f(definingVertices.get(2)).sub(definingVertices.get(1)).length();
        return (a * heightOnA) / 2.0f;
    }

    public List<Face> getNeighbouringFaces() {
        return neighbouringFaces;
    }

    public Edge determineEdgeWithNeighbouringFace(Face neighbouringFace) {
        for (Edge edge : edges) {
            for (Edge neighbourFacesEdge : neighbouringFace.edges) {
                if (edge.isOverlapping(neighbourFacesEdge)) {
                    return edge;
                }
            }
        }
        return null;
    }

    public void removeNeighbouringFaces(Collection<Face> toBeDeletedFaces) {
        this.neighbouringFaces.removeIf(toBeDeletedFaces::contains);
    }

    public void removeVerticesFromConflictList(Collection<Vector3f> verticesToBeRemoved) {
        conflictList.removeAll(verticesToBeRemoved);
    }

    public List<Collision> isColliding(Face face, PhysicsObject pObj1, PhysicsObject pObj2) {
        List<Collision> contacts = new ArrayList<>();

        Vector3f p1 = this.definingVertices.get(0);
        Vector3f q1 = this.definingVertices.get(1);
        Vector3f r1 = this.definingVertices.get(2);
        Vector3f p2 = face.definingVertices.get(0);
        Vector3f q2 = face.definingVertices.get(1);
        Vector3f r2 = face.definingVertices.get(2);

        double det1 = calculateDeterminant(p2, q2, r2, p1);
        double det2 = calculateDeterminant(p2, q2, r2, q1);
        double det3 = calculateDeterminant(p2, q2, r2, r1);
        double det4 = calculateDeterminant(p1, q1, r1, p2);
        double det5 = calculateDeterminant(p1, q1, r1, q2);
        double det6 = calculateDeterminant(p1, q1, r1, r2);

        boolean areTrianglesIntersectingPlanes = (Math.signum(det1) == Math.signum(det2) && Math.signum(det1) == Math.signum(det3) && Math.signum(det2) == Math.signum(det3) && Math.signum(det1) != 0) &&
                                                 (Math.signum(det4) == Math.signum(det5) && Math.signum(det4) == Math.signum(det6) && Math.signum(det5) == Math.signum(det6) && Math.signum(det4) != 0);

        if (areTrianglesIntersectingPlanes && (calculateDeterminant(p1, q1, p2, q2) <= 0.0 && calculateDeterminant(p1, r1, r2, p2) <= 0.0)) {
            for (Edge otherEdge : face.getEdges()) {
                Vector3f point = isColliding(otherEdge, epsilon);
                if (!point.equals(0.0f, 0.0f, 0.0f)) {
                    correctPoint(point);
                    contacts.add(new Collision(point, normalizedNormal, pObj1, pObj2));
                }
            }
            for (Edge otherEdge : face.getEdges()) {
                for (Edge thisEdge : this.getEdges()) {
                    Vector3f point = otherEdge.isColliding(thisEdge, epsilon);
                    if (!point.equals(0.0f, 0.0f, 0.0f)) {
                        correctPoint(point);

                        Vector3f normal;
                        if (point.equals(otherEdge.getMiddlePoint()) && point.equals(thisEdge.getMiddlePoint())) {
                            normal = normalizedNormal;
                        } else {
                            normal = new Vector3f(thisEdge.getEdgeDirection()).cross(otherEdge.getEdgeDirection()).normalize();
                            if (Math.abs(new Vector3f(face.getCentroid()).sub(new Vector3f(point).add(normal)).length()) < Math.abs(new Vector3f(face.getCentroid()).sub(new Vector3f(point).sub(normal)).length())) {
                                normal.mul(-1.0f);
                            }
                        }

                        contacts.add(new Collision(point, normal, pObj1, pObj2));
                    }
                }
            }
            for (Vector3f point : face.definingVertices) {
                if (isPointInsideTriangle(point)) {
                    contacts.add(new Collision(point, normalizedNormal, pObj1, pObj2));
                }
            }

            for (Edge otherEdge : getEdges()) {
                Vector3f point = face.isColliding(otherEdge, epsilon);
                if (!point.equals(0.0f, 0.0f, 0.0f)) {
                    correctPoint(point);
                    contacts.add(new Collision(point, face.normalizedNormal, pObj2, pObj1));
                }
            }
            for (Edge otherEdge : getEdges()) {
                for (Edge thisEdge : face.getEdges()) {
                    Vector3f point = otherEdge.isColliding(thisEdge, epsilon);
                    if (!point.equals(0.0f, 0.0f, 0.0f)) {
                        correctPoint(point);

                        Vector3f normal;
                        if (point.equals(otherEdge.getMiddlePoint()) && point.equals(thisEdge.getMiddlePoint())) {
                            normal = face.normalizedNormal;
                        } else {
                            normal = new Vector3f(thisEdge.getEdgeDirection()).cross(otherEdge.getEdgeDirection()).normalize();
                            if (Math.abs(new Vector3f(getCentroid()).sub(new Vector3f(point).add(normal)).length()) < Math.abs(new Vector3f(getCentroid()).sub(new Vector3f(point).sub(normal)).length())) {
                                normal.mul(-1.0f);
                            }
                        }

                        contacts.add(new Collision(point, normal, pObj2, pObj1));
                    }
                }
            }
            for (Vector3f point : definingVertices) {
                if (face.isPointInsideTriangle(point)) {
                    contacts.add(new Collision(point, face.normalizedNormal, pObj2, pObj1));
                }
            }
        } else if (det1 == 0.0f && det2 == 0.0f && det3 == 0.0f && det4 == 0.0f && det5 == 0.0f && det6 == 0.0f) {
            // TODO: Investigate why only one point is reported; Also check if point is really inside triangle

            // TODO: See why faces having y-normals dont produce many collision points
            // TODO: Write coplanar face collision code, See Book Real Time Rendering p. 760
        }
        return contacts;
    }

    private void correctPoint(Vector3f point) {
        for (int i = 0; i < 3; i++) {
            if (Math.abs(point.get(i) - Math.round(point.get(i))) <= Face.epsilon) {
                point.setComponent(i, Math.round(point.get(i)));
            }
        }
    }

    private boolean isPointInsideTriangle(Vector3f P) {
        Vector3f A = definingVertices.get(0);
        Vector3f B = definingVertices.get(1);
        Vector3f C = definingVertices.get(2);

        double alpha = new Vector3d(B).sub(P).cross(new Vector3d(C).sub(P)).length() / (2 * area);
        double beta = new Vector3d(C).sub(P).cross(new Vector3d(A).sub(P)).length() / (2 * area);
        double gamma = 1 - alpha - beta;
        return alpha >= 0 && alpha <= 1 && beta >= 0 && beta <= 1 && gamma >= 0 && gamma <= 1;
    }

    private Vector3f isColliding(Edge edge, float epsilon) {
        Vector3f e1 = new Vector3f(definingVertices.get(1)).sub(definingVertices.get(0));
        Vector3f e2 = new Vector3f(definingVertices.get(2)).sub(definingVertices.get(0));
        Vector3f s = new Vector3f(edge.getTail()).sub(definingVertices.get(0));
        Vector3f d = new Vector3f(edge.getEdgeDirection());
        Vector3f r = new Vector3f(s).cross(e1);
        Vector3f q = new Vector3f(d).cross(e2);
        float a = e1.dot(q);

        if (a > -epsilon && a < epsilon) {
            return new Vector3f(0.0f);
        }
        float u = (1.0f / a) * s.dot(q);
        if (u < 0.0f) {
            return new Vector3f(0.0f);
        }
        float v = (1.0f / a) * d.dot(r);
        if (v < 0.0f && (u + v) > 1.0f) {
            return new Vector3f(0.0f);
        }

        Vector3f result = new Vector3f(definingVertices.get(0)).mul(1.0f - u - v).add(new Vector3f(definingVertices.get(1)).mul(u)).add(new Vector3f(definingVertices.get(2)).mul(v));
        if (Math.abs((edge.getTop().distance(result) + edge.getTail().distance(result)) - edge.getTop().distance(edge.getTail())) <= epsilon) {
           return result;
        } else {
            return new Vector3f(0.0f);
        }
    }

    public void update(Matrix4f worldMatrix) {
        this.definingVertices.get(0).set(Transforms.mulVectorWithMatrix4(this.definingVertices.get(0), worldMatrix));
        this.definingVertices.get(1).set(Transforms.mulVectorWithMatrix4(this.definingVertices.get(1), worldMatrix));
        this.definingVertices.get(2).set(Transforms.mulVectorWithMatrix4(this.definingVertices.get(2), worldMatrix));

        this.supportVector.set(this.definingVertices.get(1));
        Vector3f normal = new Vector3f(new Vector3f(definingVertices.get(1)).sub(definingVertices.get(0))).cross(new Vector3f(definingVertices.get(2)).sub(definingVertices.get(0)));
        if (normal.dot(supportVector) >= 0) {
            normalizedNormal.set(normal.normalize());
        } else {
            normalizedNormal.set(normal.normalize().mul(-1.0f));
        }
        offset = normalizedNormal.dot(supportVector);
        centroid.set(determineCentroid());
        area = determineArea();

        for (Edge edge : edges) {
            edge.update(worldMatrix);
        }
    }

    private double calculateDeterminant(Vector3f a, Vector3f b, Vector3f c, Vector3f d) {
        return new Vector3d(d).sub(a).dot(new Vector3d(b).sub(a).cross(new Vector3d(c).sub(a)));
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

    public Vector3f getNormalizedNormal() {
        return normalizedNormal;
    }

    public List<Vector3f> getConflictList() {
        return conflictList;
    }

    public List<Vector3f> getDefiningVertices() {
        return definingVertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Vector3f getCentroid() {
        return centroid;
    }

    public float getArea() {
        return area;
    }
}
