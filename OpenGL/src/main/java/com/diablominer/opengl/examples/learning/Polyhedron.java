package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.collisiondetection.Face;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

public class Polyhedron implements CollisionShape {

    public static final Map<Integer, Vector3d> computedSearchDirections = new HashMap<>();
    public static final Vector3d firstSearchDirection = new Vector3d(1.0, 0.0, 0.0);
    public static final double epsilon = 1e-10;
    public static final int roundingDigit = 12;

    public List<Vertex> vertices;
    public Matrix4d currentWorldMatrix;

    public Polyhedron(Matrix4d worldMatrix) {
        this.currentWorldMatrix = worldMatrix;
    }

    public Polyhedron(Matrix4d worldMatrix, AssimpModel model) {
        this.currentWorldMatrix = worldMatrix;
        this.vertices = Polyhedron.createVertexList(model.getAllFaces());
    }

    public void initializeVertices(AssimpModel model) {
        this.vertices = Polyhedron.createVertexList(model.getAllFaces());
    }

    @Override
    public void update(Matrix4d worldMatrix) {
        currentWorldMatrix = worldMatrix;
    }

    @Override
    public boolean isColliding(CollisionShape shape) {
        LinkedList<Vector3d> simplex = new LinkedList<>();
        Vector3d p, direction = new Vector3d(firstSearchDirection);
        if (computedSearchDirections.containsKey(hashCode(this, shape))) { direction.set(computedSearchDirections.get(hashCode(this, shape))); }
        else { simplex.add(getSupportingPoint(shape, direction)); }

        while (true) {
            // p and v are rounded to allow the algorithm to converge on nonpolyhedral objects
            p = Transforms.round(minimumNorm(simplex), roundingDigit);

            if (p.equals(new Vector3d(0.0))) {
                computedSearchDirections.putIfAbsent(hashCode(this, shape), direction);
                return true;
            }

            direction = p.negate(new Vector3d());
            Vector3d v = Transforms.round(getSupportingPoint(shape, direction), roundingDigit);

            if (v.dot(direction) <= p.dot(direction)) {
                computedSearchDirections.putIfAbsent(hashCode(this, shape), direction);
                return false;
            }

            simplex.add(v);
        }
    }

    private Vector3d minimumNorm(List<Vector3d> simplex) {
        // Also removes unnecessary vertices
        switch (simplex.size()) {
            case 1:
                return simplex.get(0);
            case 2:
                return lineSegmentClosestPoint(simplex, new Vector3d(0.0));
            case 3:
                return triangleClosestPoint(simplex, new Vector3d(0.0));
            case 4:
                return tetrahedronClosestPoint(simplex, new Vector3d(0.0));
        }
        // Shouldn't happen
        System.out.println("The Simplex used in the GJK algorithm is bigger than the allowed size of 4 vertices.");
        return new Vector3d(Double.NaN);
    }

    @Override
    public Vector3d findPenetrationDepth(CollisionShape shape) {
        LinkedList<Vector3d> simplex = new LinkedList<>();
        Vector3d p, direction = new Vector3d(firstSearchDirection);
        if (computedSearchDirections.containsKey(hashCode(this, shape))) { direction.set(computedSearchDirections.get(hashCode(this, shape))); }
        simplex.add(getSupportingPoint(shape, direction));

        boolean intersection;
        while (true) {
            // p and v are rounded to allow the algorithm to converge on nonpolyhedral objects
            p = Transforms.round(minimumNorm(simplex), roundingDigit);

            if (p.equals(new Vector3d(0.0))) {
                computedSearchDirections.putIfAbsent(hashCode(this, shape), direction);
                intersection = true;
                break;
            }

            direction = p.negate(new Vector3d());
            Vector3d v = Transforms.round(getSupportingPoint(shape, direction), roundingDigit);

            if (v.dot(direction) <= p.dot(direction)) {
                computedSearchDirections.putIfAbsent(hashCode(this, shape), direction);
                intersection = false;
                break;
            }

            simplex.add(v);
        }

        if (intersection) {
            // This approximation only works when the two shapes are touching
            switch (simplex.size()) {
                case 1:
                    return simplex.get(0);
                case 2:
                    return lineSegmentClosestPoint(simplex, new Vector3d(0.0));
            }
            // Currently this method can only handle 1 or 2 vertex cases
            System.err.println("A simplex that has more than 2 vertices needs penetration depth.");
            return new Vector3d(0.0);
        } else {
            switch (simplex.size()) {
                case 1:
                    return simplex.get(0);
                case 2:
                    return lineSegmentClosestPoint(simplex, new Vector3d(0.0));
                case 3:
                    return triangleClosestPoint(simplex, new Vector3d(0.0));
                default:
                    System.err.println("The GJK algorithm has failed as a tetrahedron has been detected as non-colliding which should not be possible.");
                    return new Vector3d(Double.NaN);
            }
        }
    }

    private Vector3d lineSegmentClosestPoint(List<Vector3d> lineVertices, Vector3d point) {
        Vector3d a = lineVertices.get(0), b = lineVertices.get(1);
        Vector3d ab = new Vector3d(b).sub(a);
        double t = point.sub(a, new Vector3d()).dot(ab) / ab.dot(ab);
        if (t < 0.0) {
            t = 0.0;
        } else if (t > 1.0) {
            t = 1.0;
        }
        if (point.equals(a)) { lineVertices.remove(b); } else if (point.equals(b)) { lineVertices.remove(a); }
        return new Vector3d(a).add(ab.mul(t));
    }

    private Vector3d triangleClosestPoint(List<Vector3d> triangleVertices, Vector3d point) {
        Vector3d a = triangleVertices.get(0), b = triangleVertices.get(1), c = triangleVertices.get(2);
        Vector3d ab = b.sub(a, new Vector3d()), ac = c.sub(a, new Vector3d()), ap = point.sub(a, new Vector3d());
        double d1 = ab.dot(ap), d2 = ac.dot(ap);
        if (d1 <= 0.0 && d2 >= 0.0) {
            triangleVertices.remove(b);
            triangleVertices.remove(c);
            return a;
        }

        Vector3d bp = point.sub(b, new Vector3d());
        double d3 = ab.dot(bp), d4 = ac.dot(bp);
        if (d3 >= 0.0 && d4 >= d3) {
            triangleVertices.remove(a);
            triangleVertices.remove(c);
            return b;
        }

        double vc = d1 * d4 - d3 * d2;
        if (vc <= 0.0 && d1 >= 0.0 && d3 <= 0.0) {
            double v = d1 / (d1- d3);
            triangleVertices.remove(c);
            return new Vector3d(a).add(ab.mul(v, new Vector3d()));
        }

        Vector3d cp = point.sub(c, new Vector3d());
        double d5 = ab.dot(cp);
        double d6 = ac.dot(cp);
        if (d6 >= 0.0 && d5 <= d6) {
            triangleVertices.remove(a);
            triangleVertices.remove(b);
            return c;
        }

        double vb = d5 * d2 - d1 * d6;
        if (vb <= 0.0 && d2 >= 0.0 && d6 <= 0.0) {
            double w = d2 / (d2 - d6);
            triangleVertices.remove(b);
            return new Vector3d(a).add(ac.mul(w, new Vector3d()));
        }

        double va = d3 * d6 - d5 * d4;
        if (va <= 0.0 && (d4 - d3) >= 0.0 && (d5 - d6) >= 0.0) {
            double w = (d4 - d3) / ((d4 - d3) + (d5 - d6));
            triangleVertices.remove(a);
            return new Vector3d(b).add(c.sub(b, new Vector3d()).mul(w));
        }

        double denom = 1.0 / (va + vb + vc);
        double v = vb * denom;
        double w = vc * denom;
        return new Vector3d(a).add(ab.mul(v)).add(ac.mul(w));
    }

    private Vector3d tetrahedronClosestPoint(List<Vector3d> vertices, Vector3d point) {
        Vector3d closestPoint = point, a = vertices.get(0), b = vertices.get(1), c = vertices.get(2), d = vertices.get(3);
        double bestSqDistance = Double.MAX_VALUE;

        if (pointOutsideOfPlane(a, b, c, d, point)) {
            Vector3d q = triangleClosestPoint(new ArrayList<>(Arrays.asList(a, b, c)), point);
            double sqDist = q.sub(point, new Vector3d()).dot(q.sub(point, new Vector3d()));
            if (sqDist < bestSqDistance) {
                bestSqDistance = sqDist;
                closestPoint = q;
                vertices.remove(d);
            }
        }

        if (pointOutsideOfPlane(a, c, d, b, point)) {
            Vector3d q = triangleClosestPoint(new ArrayList<>(Arrays.asList(a, c, d)), point);
            double sqDist = q.sub(point, new Vector3d()).dot(q.sub(point, new Vector3d()));
            if (sqDist < bestSqDistance) {
                bestSqDistance = sqDist;
                closestPoint = q;
                vertices.remove(b);
            }
        }

        if (pointOutsideOfPlane(a, d, b, c, point)) {
            Vector3d q = triangleClosestPoint(new ArrayList<>(Arrays.asList(a, d, b)), point);
            double sqDist = q.sub(point, new Vector3d()).dot(q.sub(point, new Vector3d()));
            if (sqDist < bestSqDistance) {
                bestSqDistance = sqDist;
                closestPoint = q;
                vertices.remove(c);
            }
        }

        if (pointOutsideOfPlane(b, d, c, a, point)) {
            Vector3d q = triangleClosestPoint(new ArrayList<>(Arrays.asList(b, d, c)), point);
            double sqDist = q.sub(point, new Vector3d()).dot(q.sub(point, new Vector3d()));
            if (sqDist < bestSqDistance) {
                bestSqDistance = sqDist;
                closestPoint = q;
                vertices.remove(a);
            }
        }

        return closestPoint;
    }

    private boolean pointOutsideOfPlane(Vector3d a, Vector3d b, Vector3d c, Vector3d d, Vector3d point) {
        Vector3d ap = point.sub(a, new Vector3d()), ab = b.sub(a, new Vector3d()), ac = c.sub(a, new Vector3d()), ad = d.sub(a, new Vector3d());
        double signp = ap.dot(ab.cross(ac, new Vector3d()));
        double signd = ad.dot(ab.cross(ac, new Vector3d()));
        return signp * signd < 0.0;
    }

    private Vector3d getSupportingPoint(CollisionShape B, Vector3d direction) {
        return new Vector3d(getSupportingPoint(direction)).sub(B.getSupportingPoint(direction.negate(new Vector3d())));
    }

    @Override
    public Vector3d[] findClosestPoints(CollisionShape shape) {
        LinkedList<Vector3d> simplex = new LinkedList<>();
        Vector3d p, direction = new Vector3d(firstSearchDirection);
        if (computedSearchDirections.containsKey(hashCode(this, shape))) { direction.set(computedSearchDirections.get(hashCode(this, shape))); }
        HashMap<Vector3d, Vector3d[]> closestPoints = new HashMap<>();
        Vector3d supportingPointA = new Vector3d(this.getSupportingPoint(direction)), supportingPointB = new Vector3d(shape.getSupportingPoint(firstSearchDirection.negate(new Vector3d())));
        Vector3d support = supportingPointA.sub(supportingPointB, new Vector3d());
        closestPoints.put(support, new Vector3d[] {supportingPointA, supportingPointB});
        simplex.add(support);

        boolean intersection;
        while (true) {
            // p and the supporting points are rounded to allow the algorithm to converge on nonpolyhedral objects
            p = Transforms.round(minimumNorm(simplex), roundingDigit);

            if (p.equals(new Vector3d(0.0))) {
                computedSearchDirections.putIfAbsent(hashCode(this, shape), direction);
                intersection = true;
                break;
            }

            direction = p.negate(new Vector3d());
            supportingPointA = Transforms.round(new Vector3d(this.getSupportingPoint(direction)), roundingDigit);
            supportingPointB = Transforms.round(new Vector3d(shape.getSupportingPoint(direction.negate(new Vector3d()))), roundingDigit);
            Vector3d v = supportingPointA.sub(supportingPointB, new Vector3d());

            if (v.dot(direction) <= p.dot(direction)) {
                computedSearchDirections.putIfAbsent(hashCode(this, shape), direction);
                intersection = false;
                break;
            }

            simplex.add(v);
            closestPoints.putIfAbsent(v, new Vector3d[]{supportingPointA, supportingPointB});
        }

        if (intersection) {
            // This approximation only works when the two shapes are touching
            switch (simplex.size()) {
                case 1:
                    return closestPoints.get(simplex.get(0));
                case 2:
                    Vector3d point = lineSegmentClosestPoint(simplex, new Vector3d(0.0));
                    double[] coordinates1 = lineBarycentricCoordinates(simplex.get(0), simplex.get(1), point);
                    Vector3d closestPointA = closestPoints.get(simplex.get(0))[0].mul(coordinates1[0], new Vector3d()).add(closestPoints.get(simplex.get(1))[0].mul(coordinates1[1], new Vector3d()));
                    Vector3d closestPointB = closestPoints.get(simplex.get(0))[1].mul(coordinates1[0], new Vector3d()).add(closestPoints.get(simplex.get(1))[1].mul(coordinates1[1], new Vector3d()));
                    return new Vector3d[] {closestPointA, closestPointB};
            }
            // Currently this method can only handle 1 or 2 vertex cases
            System.err.println("A simplex that has more than 2 vertices needs closest points.");
            return new Vector3d[0];
        } else {
            switch (simplex.size()) {
                case 1:
                    Vector3d p1 = simplex.get(0);
                    return new Vector3d[] {closestPoints.get(p1)[0], closestPoints.get(p1)[1]};
                case 2:
                    Vector3d p2 = lineSegmentClosestPoint(simplex, new Vector3d(0.0));
                    double[] coordinates1 = lineBarycentricCoordinates(simplex.get(0), simplex.get(1), p2);
                    Vector3d closestPointA = closestPoints.get(simplex.get(0))[0].mul(coordinates1[0], new Vector3d()).add(closestPoints.get(simplex.get(1))[0].mul(coordinates1[1], new Vector3d()));
                    Vector3d closestPointB = closestPoints.get(simplex.get(0))[1].mul(coordinates1[0], new Vector3d()).add(closestPoints.get(simplex.get(1))[1].mul(coordinates1[1], new Vector3d()));
                    return new Vector3d[] {closestPointA, closestPointB};
                case 3:
                    Vector3d a = simplex.get(0), b = simplex.get(1), c = simplex.get(2);
                    Vector3d p3 = triangleClosestPoint(simplex, new Vector3d(0.0));
                    double[] coordinates2 = triangleBarycentricCoordinates(a, b, c, p3);
                    Vector3d pointA = closestPoints.get(a)[0].mul(coordinates2[0], new Vector3d()).add(closestPoints.get(b)[0].mul(coordinates2[1], new Vector3d())).add(closestPoints.get(c)[0].mul(coordinates2[2], new Vector3d()));
                    Vector3d pointB = closestPoints.get(a)[1].mul(coordinates2[0], new Vector3d()).add(closestPoints.get(b)[1].mul(coordinates2[1], new Vector3d())).add(closestPoints.get(c)[1].mul(coordinates2[2], new Vector3d()));
                    return new Vector3d[] {pointA, pointB};
                default:
                    System.err.println("The GJK algorithm has failed as a tetrahedron has been detected as non-colliding which should not be possible.");
                    return new Vector3d[0];
            }
        }
    }

    /**
     * @param a A point of a triangle
     * @param b Another point of a triangle
     * @param c A third point of a triangle
     * @param p The point for which barycentric coordinates should be computed
     * @return Barycentric coordinates for the point p
     */
    private double[] triangleBarycentricCoordinates(Vector3d a, Vector3d b, Vector3d c, Vector3d p) {
        Vector3d v0 = b.sub(a, new Vector3d()), v1 = c.sub(a, new Vector3d()), v2 = p.sub(a, new Vector3d());
        double d00 = v0.dot(v0), d01 = v0.dot(v1), d11 = v1.dot(v1), d20 = v2.dot(v0), d21 = v2.dot(v1);
        double denom = d00 * d11 - d01 * d01;
        double v = (d11 * d20 - d01 * d21) / denom;
        double w = (d00 * d21 - d01 * d20) / denom;
        double u = 1.0 - v - w;
        return new double[] {u, v, w};
    }

    private double[] lineBarycentricCoordinates(Vector3d linePoint1, Vector3d linePoint2, Vector3d point) {
        double t0 = point.sub(linePoint1, new Vector3d()).length() / linePoint2.sub(linePoint1, new Vector3d()).length();
        double t1 = 1.0 - t0;
        return new double[] {t0, t1};
    }

    @Override
    public Vector3d getSupportingPoint(Vector3d direction) {
        Vertex v = vertices.get(0);
        while (true) {
            Vector3d tempV = new Vector3d(v.position);
            for (Vertex vertex : v.neighbors) {
                if ((Transforms.mulVectorWithMatrix4(vertex.position, currentWorldMatrix).sub(Transforms.mulVectorWithMatrix4(v.position, currentWorldMatrix))).dot(direction) > 0) {
                    v = vertex;
                    break;
                }
            }
            if (tempV.equals(v.position)) {
                break;
            }
        }
        return Transforms.mulVectorWithMatrix4(v.position, currentWorldMatrix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Polyhedron that = (Polyhedron) o;
        return vertices.equals(that.vertices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices);
    }

    public static int hashCode(Polyhedron polyhedron, CollisionShape shape) {
        return 8 * (polyhedron.hashCode() + shape.hashCode());
    }

    public static List<Vertex> createVertexList(List<Face> faces) {
        HashMap<Vector3d, Vertex> alreadyLoaded = new HashMap<>();
        for (Face face : faces) {
            List<Vector3f> definingVertices = face.getDefiningVertices();
            Vertex v1 = getOrCreateVertex(alreadyLoaded, new Vector3d(definingVertices.get(0)));
            Vertex v2 = getOrCreateVertex(alreadyLoaded, new Vector3d(definingVertices.get(1)));
            Vertex v3 = getOrCreateVertex(alreadyLoaded, new Vector3d(definingVertices.get(2)));
            v1.addNeighbors(new ArrayList<>(Arrays.asList(v2, v3)));
            v2.addNeighbors(new ArrayList<>(Arrays.asList(v1, v3)));
            v3.addNeighbors(new ArrayList<>(Arrays.asList(v2, v3)));
        }
        return new ArrayList<>(alreadyLoaded.values());
    }

    public static Vertex getOrCreateVertex(Map<Vector3d, Vertex> alreadyLoaded, Vector3d position) {
        Vertex result;
        if (!alreadyLoaded.containsKey(position)) { result = new Vertex(position); alreadyLoaded.put(position, result); }
        else { result = alreadyLoaded.get(position); }
        return result;
    }

}
