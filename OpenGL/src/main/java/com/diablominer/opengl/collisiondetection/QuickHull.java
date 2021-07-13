package com.diablominer.opengl.collisiondetection;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class QuickHull {

    private final List<Face> faces = new ArrayList<>();
    private final List<Vector3f> definingPoints = new ArrayList<>();
    private final List<Vector3f> points;
    private final float epsilon;

    private final Vector3f centroid;
    private final float area;

    public QuickHull(Collection<Vector3f> vertices) {
        long startingTime = System.currentTimeMillis();

        Set<Vector3f> inputVerticesWithOutDuplicates = new HashSet<>(vertices);
        List<Vector3f> remainingVertices = new LinkedList<>(inputVerticesWithOutDuplicates);
        points = new ArrayList<>(vertices);
        Vector3f[] maxPoints = {new Vector3f(-100000000.0f), new Vector3f(-100000000.0f), new Vector3f(-100000000.0f)};
        Vector3f[] minPoints = {new Vector3f(100000000.0f), new Vector3f(100000000.0f), new Vector3f(100000000.0f)};
        // Determine the points with highest/lowest coordinates
        for (Vector3f vertex : remainingVertices) {
            if (vertex.x > maxPoints[0].x) {
                maxPoints[0] = vertex;
            }
            if (vertex.y > maxPoints[1].y) {
                maxPoints[1] = vertex;
            }
            if (vertex.z > maxPoints[2].z) {
                maxPoints[2] = vertex;
            }

            if (vertex.x < minPoints[0].x) {
                minPoints[0] = vertex;
            }
            if (vertex.y < minPoints[1].y) {
                minPoints[1] = vertex;
            }
            if (vertex.z < minPoints[2].z) {
                minPoints[2] = vertex;
            }
        }
        epsilon = 3 * (Math.max(Math.abs(maxPoints[0].x), Math.abs(minPoints[0].x)) +
                       Math.max(Math.abs(maxPoints[1].y), Math.abs(minPoints[1].y)) +
                       Math.max(Math.abs(maxPoints[2].z), Math.abs(minPoints[2].z))) * Math.ulp(1.0f);

        List<Vector3f> startingPoints = new ArrayList<>();
        startingPoints.add(new Vector3f(0.00000001f));
        startingPoints.add(new Vector3f(-0.00000001f));
        startingPoints.add(new Vector3f(0.0f));
        startingPoints.add(new Vector3f(0.0f));
        // Determine the two points with max/min coordinates that have the longest distance between them
        for (Vector3f maxPoint: maxPoints) {
            for (Vector3f minPoint : minPoints) {
                if (maxPoint.distance(minPoint) > startingPoints.get(0).distance(startingPoints.get(1))) {
                    startingPoints.set(0, maxPoint);
                    startingPoints.set(1, minPoint);
                }
            }
        }

        remainingVertices.remove(startingPoints.get(0));
        remainingVertices.remove(startingPoints.get(1));
        startingPoints.set(2, new Vector3f(startingPoints.get(0)).add(new Vector3f(0.00001f)));
        // Determine the point that has greatest distance from the line defined by the two points determined earlier
        for (Vector3f vertex : remainingVertices) {
            float newDistance = new Vector3f(vertex).sub(startingPoints.get(0)).cross(new Vector3f(vertex).sub(startingPoints.get(1))).length() / new Vector3f(startingPoints.get(1)).set(new Vector3f(startingPoints.get(0))).length();
            float oldDistance = new Vector3f(startingPoints.get(2)).sub(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(2)).sub(startingPoints.get(1))).length() / new Vector3f(startingPoints.get(1)).set(new Vector3f(startingPoints.get(0))).length();
            if (newDistance > oldDistance) {
                startingPoints.set(2, vertex);
            }
        }

        remainingVertices.remove(startingPoints.get(2));
        startingPoints.set(3, new Vector3f(startingPoints.get(2)).add(new Vector3f(0.00001f)));
        // Determine the point with the greatest distance to the plane defined by the three points determined earlier
        Vector3f constantTerm = new Vector3f(startingPoints.get(1)).sub(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(2)).sub(startingPoints.get(0))).
                div(new Vector3f(startingPoints.get(1)).sub(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(2)).sub(startingPoints.get(0))).length());
        float oldDistance = constantTerm.dot(new Vector3f(startingPoints.get(3)).sub(startingPoints.get(0)));
        for (Vector3f vertex : remainingVertices) {
            float newDistance = constantTerm.dot(new Vector3f(vertex).sub(startingPoints.get(0)));
            if (newDistance > oldDistance) {
                startingPoints.set(3, vertex);
            }
        }
        remainingVertices.remove(startingPoints.get(3));

        // Determine the centroid of the tetrahedron and transform all points so it becomes the zero point of the coordinate system
        Vector3f centroid = new Vector3f(0.0f).add(startingPoints.get(0)).add(startingPoints.get(1)).add(startingPoints.get(2)).add(startingPoints.get(3));
        centroid.mul(1.0f / 4.0f);
        for (Vector3f vertex : points) {
            vertex.sub(centroid);
        }

        // Add the first four faces and add their neighbours
        Face face1 = new Face(startingPoints.get(0), startingPoints.get(1), startingPoints.get(2));
        Face face2 = new Face(startingPoints.get(0), startingPoints.get(1), startingPoints.get(3));
        Face face3 = new Face(startingPoints.get(0), startingPoints.get(2), startingPoints.get(3));
        Face face4 = new Face(startingPoints.get(1), startingPoints.get(2), startingPoints.get(3));
        faces.add(face1);
        faces.add(face2);
        faces.add(face3);
        faces.add(face4);
        for (Face face : faces) {
            for (Face secondFace : faces) {
                face.addNewNeighbouringFace(secondFace);
            }
        }

        // Assign those points that aren't in the tetrahedron to the faces that they lie before
        ArrayList<Vector3f> toBeRemovedPoints = new ArrayList<>();
        List<Vector3f> alreadyTakenConflictVertices = new ArrayList<>();
        for (Vector3f vertex : remainingVertices) {
            if (!toBeRemovedPoints.contains(vertex)) {
                if (isPointInsideTetrahedron(startingPoints.get(0), startingPoints.get(1), startingPoints.get(2), startingPoints.get(3), vertex)) {
                    toBeRemovedPoints.add(vertex);
                } else {
                    for (Face face : faces) {
                        if (face.signedDistance(vertex) > epsilon && !alreadyTakenConflictVertices.contains(vertex)) {
                            face.addNewConflictVertex(vertex);
                            alreadyTakenConflictVertices.add(vertex);
                        }
                    }
                }
            }
        }
        remainingVertices.removeAll(toBeRemovedPoints);

        // Execute the actual quickhull algorithm
        while (!remainingVertices.isEmpty()) {
           Set<Face> toBeDeletedFaces = new HashSet<>();
           Set<Face> toBeAddedFaces = new HashSet<>();
           for (Face face : faces) {
               if (!face.isConflictListEmpty() && !toBeDeletedFaces.contains(face)) {
                   List<Face> visibleFaces = new ArrayList<>();
                   Set<Edge> horizonEdges = new HashSet<>();
                   Set<Face> newFaces = new HashSet<>();
                   Vector3f furthestPoint = face.getFurthestPointFromConflictList();
                   visibleFaces.add(face);

                   // Search for all visible faces
                   for (int i = 0; i < visibleFaces.size(); i++) {
                       Face visibleFace = visibleFaces.get(i);
                       for (Face neighbouringFace : visibleFace.getNeighbouringFaces()) {
                           if (!visibleFaces.contains(neighbouringFace)) {
                               float signedDistance = neighbouringFace.signedDistance(furthestPoint);
                               if (signedDistance > epsilon) {
                                   visibleFaces.add(neighbouringFace);
                               }
                           }
                       }
                   }

                   // Determine the horizon edges, normal edges are shared between multiple visible faces
                   for (Face visibleFace : visibleFaces) {
                       List<Face> visibleFacesWithOutTestedFace = new ArrayList<>(visibleFaces);
                       visibleFacesWithOutTestedFace.remove(visibleFace);
                       for (Edge edge : visibleFace.getEdges()) {
                           if (visibleFacesWithOutTestedFace.stream().noneMatch(toBeTestedFace -> toBeTestedFace.hasEdge(edge))) {
                               horizonEdges.add(edge);
                           }
                       }
                   }

                   // Create the new faces and add the faces that are to be deleted/added to their respective lists
                   for (Edge horizonEdge : horizonEdges) {
                       Face newFace = new Face(horizonEdge, furthestPoint);
                       newFaces.add(newFace);
                   }
                   toBeDeletedFaces.addAll(visibleFaces);
                   toBeAddedFaces.addAll(newFaces);

                   // Check the neighbours; For new faces neighbours are determined and for old and new faces neighbouring faces are checked
                   List<Face> allPotentialNeighbours = new ArrayList<>(faces);
                   allPotentialNeighbours.addAll(toBeAddedFaces);
                   allPotentialNeighbours.removeAll(toBeDeletedFaces);
                   for (Face newFace : toBeAddedFaces) {
                       for (Face potentialNeighbourFace : allPotentialNeighbours) {
                           newFace.addNewNeighbouringFace(potentialNeighbourFace);
                           potentialNeighbourFace.addNewNeighbouringFace(newFace);
                       }
                   }
                   List<Face> currentFacesAndNewFaces = new ArrayList<>(faces);
                   currentFacesAndNewFaces.addAll(toBeAddedFaces);
                   currentFacesAndNewFaces.removeAll(visibleFaces);
                   for (Face faceWithPotentialRedundantNeighbours : currentFacesAndNewFaces) {
                       faceWithPotentialRedundantNeighbours.removeNeighbouringFaces(toBeDeletedFaces);
                   }
               }
           }
           // Add/Remove all faces that need to be added/removed
           faces.addAll(toBeAddedFaces);
           faces.removeAll(toBeDeletedFaces);

           // Remove vertices that are inside the convex hull and remove conflict vertices
           remainingVertices.removeIf(this::isPointInsideConvexHull);
           for (Face faceWithRedundantConflictVertices : faces) {
               faceWithRedundantConflictVertices.getConflictList().removeIf(conflictVertex -> !remainingVertices.contains(conflictVertex));
           }

           // Reassign the conflict vertices from deleted faces to newly created faces
           Set<Vector3f> verticesToBeAdded = new HashSet<>();
           List<Vector3f> verticesToBeDeleted = new ArrayList<>();
           for (Face newFace : toBeAddedFaces) {
               for (Face otherFace : toBeDeletedFaces) {
                   for (Vector3f vertex : otherFace.getConflictList()) {
                       if (newFace.signedDistance(vertex) > epsilon) {
                           verticesToBeAdded.add(vertex);
                           verticesToBeDeleted.add(vertex);
                       }
                   }
                   otherFace.removeVerticesFromConflictList(verticesToBeDeleted);
                   verticesToBeDeleted.clear();
               }
               newFace.addNewConflictVertices(verticesToBeAdded);
               verticesToBeAdded.clear();
           }
        }
        // Transform the points back into the coordinate system they were given in
        for (Vector3f vertex : points) {
            vertex.add(centroid);
        }

        // Add all points from the faces to the defining points
        for (Face face : faces) {
            definingPoints.addAll(face.getDefiningVertices());
        }
        System.out.println("Time taken for Quickhull: " + (System.currentTimeMillis() - startingTime));

        // Initialize area and centroid
        this.area = determineArea();
        this.centroid = determineCentroid();
    }

    public List<Face> getFaces() {
        return faces;
    }

    public List<Vector3f> getDefiningPoints() {
        return definingPoints;
    }

    public List<Vector3f> getPoints() {
        return points;
    }

    private float determineArea() {
        float area = 0.0f;
        for (Face face : faces) {
            area += face.getArea();
        }
        return area;
    }

    private Vector3f determineCentroid() {
        Vector3f centroid = new Vector3f(0.0f);
        for (Face face : faces) {
            centroid.add(new Vector3f(face.getCentroid()).mul(face.getArea()));
        }
        centroid.div(this.area);
        return centroid;
    }

    public Vector3f getCentroid() {
        return centroid;
    }

    public float getArea() {
        return area;
    }

    /**
     * Test if a point r is inside a tetrahedron defined by the four points r1, r2, r3, r4, for reference see: <a href="https://en.wikipedia.org/wiki/Barycentric_coordinate_system#Barycentric_coordinates_on_tetrahedra">Barycentric coordinate system</a>
     * @param r1 First vertex of the tetrahedron
     * @param r2 Second vertex of the tetrahedron
     * @param r3 Third vertex of the tetrahedron
     * @param r4 Fourth vertex of the tetrahedron
     * @param r Point that should be tested to see if it is in the tetrahedron
     */
    private boolean isPointInsideTetrahedron(Vector3f r1, Vector3f r2, Vector3f r3, Vector3f r4, Vector3f r) {
        Matrix3f T = new Matrix3f();
        T.set(r1.x - r4.x, r1.y - r4.y, r1.z - r4.z,
              r2.x - r4.x, r2.y - r4.y, r2.z - r4.z,
              r3.x - r4.x, r3.y - r4.y, r3.z - r4.z);
        T.invert();
        Vector3f lambda = new Vector3f(r).sub(new Vector3f(r4)).mul(T);
        Vector4f allLambdas = new Vector4f(lambda, 1 - lambda.x - lambda.y - lambda.z);
        return allLambdas.x > 0 && allLambdas.y > 0 && allLambdas.z > 0 && allLambdas.w > 0;
    }

    public boolean isPointInsideConvexHull(Vector3f point) {
        return faces.stream().noneMatch(face -> face.signedDistance(point) > epsilon);
    }

}
