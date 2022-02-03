package com.diablominer.opengl.collisiondetection;

import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.utils.Transforms;
import org.joml.*;

import java.lang.Math;
import java.util.*;

public class Face {

    public static final float epsilon = Math.ulp(1.0f);

    private final List<Vector3f> definingVertices = new ArrayList<>();
    private final List<Vector3f> originalVertices = new ArrayList<>();
    private final List<Vector3f> conflictList = new ArrayList<>();
    private final List<Face> neighbouringFaces = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    private final Vector3f supportVector;
    private final Vector3f normalizedNormal;
    private float offset;

    private final Vector3f centroid;
    private float area;

    private final boolean normalFromOrigin;

    enum FaceIntersectionType {
        Intersection,
        NoIntersection,
        Coplanar;
    }


    /**
     * This face's normal is constructed from the origin automatically. For reference see: {@link #Face(Vector3f, Vector3f, Vector3f, boolean) Face}
     * @param edge Provides first and second vertex with top and tail
     * @param vertex3 Third vertex
     */
    public Face(Edge edge, Vector3f vertex3) {
        this(edge.getTop(), edge.getTail(), vertex3, true);
    }

    /**
     * @param vertex1 First vertex
     * @param vertex2 Second vertex
     * @param vertex3 Third vertex
     * @param normalFromOrigin Determines if normal is constructed according to the <a href="https://en.wikipedia.org/wiki/Hesse_normal_form">Hesse normal form</a> which means the normal will point away from the origin of the coordinate system
     */
    public Face(Vector3f vertex1, Vector3f vertex2, Vector3f vertex3, boolean normalFromOrigin) {
        this.normalFromOrigin = normalFromOrigin;

        this.definingVertices.add(new Vector3f(vertex1));
        this.definingVertices.add(new Vector3f(vertex2));
        this.definingVertices.add(new Vector3f(vertex3));
        this.originalVertices.add(new Vector3f(vertex1));
        this.originalVertices.add(new Vector3f(vertex2));
        this.originalVertices.add(new Vector3f(vertex3));

        edges.add(new Edge(vertex1, vertex2, this));
        edges.add(new Edge(vertex1, vertex3, this));
        edges.add(new Edge(vertex2, vertex3, this));

        supportVector = vertex2;
        Vector3f normal = new Vector3f(new Vector3f(vertex2).sub(vertex1)).cross(new Vector3f(vertex3).sub(vertex1));
        if (normalFromOrigin) {
            if (normal.dot(supportVector) >= 0) {
                normalizedNormal = normal.normalize();
            } else {
                normalizedNormal = normal.normalize().mul(-1.0f);
            }
        } else {
            normalizedNormal = normal.normalize();
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
            if (edge.isOverlapping(edgeToBeTested).equals(Edge.OverlappingType.Identical)) {
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
                if (edge.isOverlapping(neighbourFacesEdge).equals(Edge.OverlappingType.Identical)) {
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

    public List<Collision> isColliding(Face face, PhysicsObject thisPObj, PhysicsObject otherPObj) {
        List<Collision> contacts = new ArrayList<>();

        FaceIntersectionType intersectionType = areTrianglesIntersectingPlanes(face);
        if (intersectionType == FaceIntersectionType.Intersection && areTrianglesColliding(face)) {
            determineVertexIntersections(face, contacts, thisPObj, otherPObj);
            face.determineVertexIntersections(this, contacts, otherPObj, thisPObj);

            determineEdgeIntersections(face, contacts, thisPObj, otherPObj);
            face.determineEdgeIntersections(this, contacts, thisPObj, otherPObj);
        } else if (intersectionType == FaceIntersectionType.Coplanar && areTrianglesCollidingCoplanar(face)) {
            determineVertexIntersections(face, contacts, thisPObj, otherPObj);
            face.determineVertexIntersections(this, contacts, otherPObj, thisPObj);

            determineEdgeIntersections(face, contacts, thisPObj, otherPObj);
            face.determineEdgeIntersections(this, contacts, thisPObj, otherPObj);
        }
        return contacts;
    }

    private void determineVertexIntersections(Face face, List<Collision> contacts, PhysicsObject thisPObj, PhysicsObject otherPObj) {
        for (Vector3f point : face.definingVertices) {
            if (isPointInsideTriangle(point)) {
                contacts.add(new Collision(point, normalizedNormal, thisPObj, otherPObj, this));
            }
        }
    }

    private void determineEdgeIntersections(Face face, List<Collision> contacts, PhysicsObject thisPObj, PhysicsObject otherPObj) {
        for (Edge otherEdge : face.getEdges()) {
            for (Edge thisEdge : this.getEdges()) {
                Vector3f point = otherEdge.isColliding(thisEdge);
                if (!point.equals(0.0f, 0.0f, 0.0f)) {
                    correctPoint(point);

                    Vector3f normal;
                    if (point.equals(otherEdge.getMiddlePoint()) && point.equals(thisEdge.getMiddlePoint()) || thisEdge.hasSameDirection(otherEdge)) {
                        normal = normalizedNormal;
                    } else {
                        normal = new Vector3f(thisEdge.getEdgeDirection()).cross(otherEdge.getEdgeDirection()).normalize();
                        if (Math.abs(new Vector3f(face.getCentroid()).sub(new Vector3f(point).add(normal)).length()) < Math.abs(new Vector3f(face.getCentroid()).sub(new Vector3f(point).sub(normal)).length())) {
                            normal.mul(-1.0f);
                        }
                    }

                    // TODO: Remove
                    if (point.equals(new Vector3f(-9.0f, -7.0f, -3.0f))) {
                        System.out.println("test");
                    }
                    if (point.equals(new Vector3f(-7.0027475f, -7.0f, -1.0f))) {
                        System.out.println("test");
                    }

                    contacts.add(new Collision(point, normal, thisPObj, otherPObj, this));
                }
            }
        }
    }

    private FaceIntersectionType areTrianglesIntersectingPlanes(Face face) {
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

        boolean isDet1Zero = (Math.signum(det1) >= -epsilon && Math.signum(det1) <= epsilon);
        boolean isDet4Zero = (Math.signum(det4) >= -epsilon && Math.signum(det4) <= epsilon);
        boolean areThisFaceSignsTheSame = (Math.signum(det1) == Math.signum(det2) && Math.signum(det1) == Math.signum(det3) && Math.signum(det2) == Math.signum(det3));
        boolean areOtherFaceSignsTheSame = (Math.signum(det4) == Math.signum(det5) && Math.signum(det4) == Math.signum(det6) && Math.signum(det5) == Math.signum(det6));

        if (areThisFaceSignsTheSame) {
            if (isDet1Zero) {
                return FaceIntersectionType.Coplanar;
            } else {
                return FaceIntersectionType.NoIntersection;
            }
        } else {
            if (areOtherFaceSignsTheSame) {
                if (isDet4Zero) {
                    return FaceIntersectionType.Coplanar;
                } else {
                    return FaceIntersectionType.NoIntersection;
                }
            } else {
                return FaceIntersectionType.Intersection;
            }
        }
    }

    private boolean areTrianglesColliding(Face face) {
        Vector3f p1 = null;
        Vector3f q1 = null;
        Vector3f r1 = null;
        for (Vector3f vertex : definingVertices) {
            List<Vector3f> remainingVertices = new ArrayList<>(definingVertices);
            remainingVertices.remove(vertex);
            if (remainingVertices.stream().noneMatch(vec -> Math.signum(face.signedDistance(vertex)) == Math.signum(face.signedDistance(vec)))) {
                p1 = (vertex);
                q1 = (remainingVertices.get(0));
                r1 = (remainingVertices.get(1));
                updateVertices(p1, q1, r1);
                break;
            }
        }

        Vector3f p2 = null;
        Vector3f q2 = null;
        Vector3f r2 = null;
        for (Vector3f vertex : face.definingVertices) {
            List<Vector3f> remainingVertices = new ArrayList<>(face.definingVertices);
            remainingVertices.remove(vertex);
            if (remainingVertices.stream().noneMatch(vec -> Math.signum(signedDistance(vertex)) == Math.signum(signedDistance(vec)))) {
                p2 = (vertex);
                q2 = (remainingVertices.get(0));
                r2 = (remainingVertices.get(1));
                face.updateVertices(p2, q2, r2);
                break;
            }
        }

        if ((Math.signum(face.signedDistance(p1)) == -1.0f) || (Math.signum(face.signedDistance(p1)) == 0.0f && Math.signum(face.signedDistance(q1)) == 1.0f)) {
            Transforms.swapTwoVectors(q2, r2);
        }

        if ((Math.signum(signedDistance(p2)) == -1.0f) || (Math.signum(signedDistance(p2)) == 0.0f && Math.signum(signedDistance(q2)) == 1.0f)) {
            Transforms.swapTwoVectors(q1, r1);
        }

        return ((calculateDeterminant(p1, q1, p2, q2)) <= 0.0f && (calculateDeterminant(p1, r1, r2, p2)) <= 0.0f);
    }

    public boolean areTrianglesCollidingCoplanar(Face face) {
        for (Edge thisEdge : edges) {
            for (Edge otherEdge : face.edges) {
                if (!thisEdge.isColliding(otherEdge).equals(0.0f, 0.0f, 0.0f)) {
                    return true;
                }
            }
        }
        return (isTriangleContainedInOtherTriangle(face) || face.isTriangleContainedInOtherTriangle(this));
    }

    private boolean isTriangleContainedInOtherTriangle(Face face) {
        return  (isPointInsideTriangle(face.definingVertices.get(0)) && isPointInsideTriangle(face.definingVertices.get(1)) && isPointInsideTriangle(face.definingVertices.get(2)));
    }

    private void correctPoint(Vector3f point) {
        for (int i = 0; i < 3; i++) {
            if (Math.abs(point.get(i) - Math.round(point.get(i))) <= epsilon) {
                point.setComponent(i, Math.round(point.get(i)));
            }
        }
    }

    public boolean isPointInsideTriangle(Vector3f p) {
        // General method used is from "Foundations of Physically Based Modelling and Animation": Barycentric coordinates
        Vector3f A = definingVertices.get(0);
        Vector3f B = definingVertices.get(1);
        Vector3f C = definingVertices.get(2);
        Vector3f vn = (new Vector3f(B).sub(A)).cross(new Vector3f(C).sub(B));
        float a = vn.length();
        Vector3f n = new Vector3f(vn).normalize();
        Vector3f w = new Vector3f(p).sub(A);

        // This is used to prevent points situated at a different level in the normal direction of the face from being reported
        if (n.dot(w) < -epsilon || n.dot(w) > epsilon) {
            return false;
        }

        float alpha = (new Vector3f(C).sub(B)).cross(new Vector3f(p).sub(B)).dot(n) / a;
        float beta = (new Vector3f(A).sub(C)).cross(new Vector3f(p).sub(C)).dot(n) / a;
        float gamma = 1.0f - alpha - beta;

        return alpha >= 0 && alpha <= 1 && beta >= 0 && beta <= 1 && gamma >= 0 && gamma <= 1;
    }

    private Vector3f areFaceAndEdgeColliding(Edge edge) {
        Vector3f e1 = new Vector3f(definingVertices.get(1)).sub(definingVertices.get(0));
        Vector3f e2 = new Vector3f(definingVertices.get(2)).sub(definingVertices.get(0));
        Vector3f s = new Vector3f(edge.getTail()).sub(definingVertices.get(0));
        Vector3f d = edge.getEdgeDirection();
        Vector3f r = new Vector3f(s).cross(e1);
        Vector3f q = new Vector3f(d).cross(e2);
        float a = e1.dot(q);

        if (a > -epsilon && a < epsilon) {
            return new Vector3f(0.0f);
        }
        float f = (1.0f / a);
        float u = f * s.dot(q);
        if (u < 0.0f) {
            return new Vector3f(0.0f);
        }
        float v = f * d.dot(r);
        if (v < 0.0f || (u + v) > 1.0f) {
            return new Vector3f(0.0f);
        }

        Vector3f result = new Vector3f(definingVertices.get(0)).mul(1.0f - u - v).add(new Vector3f(definingVertices.get(1)).mul(u)).add(new Vector3f(definingVertices.get(2)).mul(v));
        if (edge.isPointInEdge(result)) {
           return result;
        } else {
            return new Vector3f(0.0f);
        }
    }

    private void updateVertices(Vector3f v0, Vector3f v1, Vector3f v2) {
        Vector3f temp0 = new Vector3f(v0);
        Vector3f temp1 = new Vector3f(v1);
        Vector3f temp2 = new Vector3f(v2);
        this.definingVertices.get(0).set(temp0);
        this.definingVertices.get(1).set(temp1);
        this.definingVertices.get(2).set(temp2);
    }

    private void update() {
        this.supportVector.set(this.definingVertices.get(1));
        Vector3f normal = new Vector3f(new Vector3f(definingVertices.get(1)).sub(definingVertices.get(0))).cross(new Vector3f(definingVertices.get(2)).sub(definingVertices.get(0)));
        if (normalFromOrigin) {
            if (normal.dot(supportVector) >= 0) {
                normalizedNormal.set(normal.normalize());
            } else {
                normalizedNormal.set(normal.normalize().mul(-1.0f));
            }
        } else {
            normalizedNormal.set(normal.normalize());
        }
        offset = normalizedNormal.dot(supportVector);
        centroid.set(determineCentroid());
        area = determineArea();
    }

    public void update(Matrix4f worldMatrix) {
        this.definingVertices.get(0).set(Transforms.mulVectorWithMatrix4(this.originalVertices.get(0), worldMatrix));
        this.definingVertices.get(1).set(Transforms.mulVectorWithMatrix4(this.originalVertices.get(1), worldMatrix));
        this.definingVertices.get(2).set(Transforms.mulVectorWithMatrix4(this.originalVertices.get(2), worldMatrix));

        update();

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
