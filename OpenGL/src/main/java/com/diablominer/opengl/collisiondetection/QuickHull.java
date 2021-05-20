package com.diablominer.opengl.collisiondetection;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.CallbackI;

import java.util.*;
import java.util.function.Predicate;

public class QuickHull {

    private final List<Face> faces = new ArrayList<>();
    private final List<Vector3f> definingPoints = new ArrayList<>();
    private final List<Vector3f> points;
    private final float epsilon;

    public QuickHull(Collection<Vector3f> vertices) {
        long startingTime = System.currentTimeMillis();

        Set<Vector3f> inputVerticesWithOutDuplicates = new HashSet<>(vertices);
        List<Vector3f> remainingVertices = new LinkedList<>(inputVerticesWithOutDuplicates);
        points = new ArrayList<>(vertices);
        Vector3f[] maxPoints = {new Vector3f(-100000000.0f), new Vector3f(-100000000.0f), new Vector3f(-100000000.0f)};
        Vector3f[] minPoints = {new Vector3f(100000000.0f), new Vector3f(100000000.0f), new Vector3f(100000000.0f)};
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
        for (Vector3f vertex : remainingVertices) {
            float newDistance = new Vector3f(vertex).sub(startingPoints.get(0)).cross(new Vector3f(vertex).sub(startingPoints.get(1))).length() / new Vector3f(startingPoints.get(1)).set(new Vector3f(startingPoints.get(0))).length();
            float oldDistance = new Vector3f(startingPoints.get(2)).sub(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(2)).sub(startingPoints.get(1))).length() / new Vector3f(startingPoints.get(1)).set(new Vector3f(startingPoints.get(0))).length();
            if (newDistance > oldDistance) {
                startingPoints.set(2, vertex);
            }
        }

        remainingVertices.remove(startingPoints.get(2));
        startingPoints.set(3, new Vector3f(startingPoints.get(2)).add(new Vector3f(0.00001f)));
        for (Vector3f vertex : remainingVertices) {
            Vector3f constantTerm = new Vector3f(startingPoints.get(1)).sub(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(2)).sub(startingPoints.get(0))).
                                    div(new Vector3f(startingPoints.get(1)).sub(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(2)).sub(startingPoints.get(0))).length());
            float newDistance = constantTerm.dot(new Vector3f(vertex).sub(startingPoints.get(0)));
            float oldDistance = constantTerm.dot(new Vector3f(startingPoints.get(3)).sub(startingPoints.get(0)));
            if (newDistance > oldDistance) {
                startingPoints.set(3, vertex);
            }
        }
        remainingVertices.remove(startingPoints.get(3));

        Vector3f centroid = new Vector3f(0.0f).add(startingPoints.get(0)).add(startingPoints.get(1)).add(startingPoints.get(2)).add(startingPoints.get(3));
        centroid.mul(1.0f / 4.0f);
        for (Vector3f vertex : points) {
            vertex.sub(centroid);
        }

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

        List<Edge> horizonEdgee = new ArrayList<>();
        List<Vector3f> furthestPoints = new ArrayList<>();
        Face testFace = null;
        Face testFace2 = null;
        List<Face> deletedFaces = new ArrayList<>();
        int iteration = 0;
        while (iteration < 2) {
           Set<Face> toBeDeletedFaces = new HashSet<>();
           Set<Face> toBeAddedFaces = new HashSet<>();
           for (Face face : faces) {
               if (!face.isConflictListEmpty() && !toBeDeletedFaces.contains(face)) {
                   List<Face> visibleFaces = new ArrayList<>();
                   Set<Edge> horizonEdges = new HashSet<>();
                   Set<Face> newFaces = new HashSet<>();
                   Vector3f furthestPoint = face.returnFurthestPoint();
                   visibleFaces.add(face);

                   for (int i = 0; i < visibleFaces.size(); i++) {
                       Face visibleFace = visibleFaces.get(i);
                       for (Face neighbouringFace : visibleFace.returnNeighbouringFaces()) {
                           if (!visibleFaces.contains(neighbouringFace)) {
                               float signedDistance = neighbouringFace.signedDistance(furthestPoint);
                               List<Face> relevantFaces = new ArrayList<>(visibleFaces);
                               relevantFaces.remove(visibleFace);
                               Edge edge = visibleFace.returnEdgeWithNeighbouringFace(neighbouringFace);
                               if (signedDistance > epsilon) {
                                   visibleFaces.add(neighbouringFace);
                               } else if (signedDistance < -epsilon && relevantFaces.stream().noneMatch(aFace -> aFace.hasEdge(edge))) {
                                   horizonEdges.add(edge);
                               }
                           }
                       }
                   }

                   // False faces seem to be constructed OR visible faces aren't deleted correctly
                   // False Faces: Iteration 2; Indices: 13 and 2
                   // Continue to investigate false faces
                   // Problem seems to be caused by furthespoint being right of a face --> false horizonedge
                   // Horizonedge detection process has to revised(take just the ones that aren't between other visible faces)
                   // Hasnt fixed the bug see whats causing the problem: Maybe take the horizon edges only after all visible faces have been found
                   toBeDeletedFaces.addAll(visibleFaces);
                   for (Edge horizonEdge : horizonEdges) {
                       Face newFace = new Face(horizonEdge, furthestPoint);
                       newFaces.add(newFace);
                       if (!toBeDeletedFaces.contains(horizonEdge.getFace())) {
                           newFace.addNewNeighbouringFace(horizonEdge.getFace());
                           horizonEdge.getFace().addNewNeighbouringFace(newFace);
                       }
                   }
                   toBeAddedFaces.addAll(newFaces);
                   List<Face> allPotentialNeighbours = new ArrayList<>(faces);
                   allPotentialNeighbours.addAll(toBeAddedFaces);
                   for (Face newFace : toBeAddedFaces) {
                       for (Face potentialNeighbourFace : allPotentialNeighbours) {
                           if (!toBeDeletedFaces.contains(potentialNeighbourFace)) {
                               newFace.addNewNeighbouringFace(potentialNeighbourFace);
                               potentialNeighbourFace.addNewNeighbouringFace(newFace);
                           }
                       }
                   }
                   for (Face faceWithPotentialRedundantNeighbours : faces) {
                       faceWithPotentialRedundantNeighbours.removeNeighbouringFaces(toBeDeletedFaces);
                   }

                   horizonEdgee.addAll(horizonEdges);
                   furthestPoints.add(furthestPoint);
               }
           }
           faces.addAll(toBeAddedFaces);
           if (iteration == 1) {
               testFace2 = faces.get(32);
               deletedFaces.addAll(toBeDeletedFaces);
           }
           faces.removeAll(toBeDeletedFaces);
           remainingVertices.removeIf(this::isPointInsideConvexHull);
           for (Face faceWithRedundantConflictVertices : faces) {
               faceWithRedundantConflictVertices.returnConflictList().removeIf(conflictVertex -> !remainingVertices.contains(conflictVertex));
           }
           for (Face newFace : toBeAddedFaces) {
               Set<Vector3f> verticesToBeAdded = new HashSet<>();
               for (Face otherFace : toBeDeletedFaces) {
                   List<Vector3f> verticesToBeDeleted = new ArrayList<>();
                   for (Vector3f vertex : otherFace.returnConflictList()) {
                       if (newFace.signedDistance(vertex) > epsilon) {
                           verticesToBeAdded.add(vertex);
                           verticesToBeDeleted.add(vertex);
                       }
                   }
                   otherFace.removeVerticesFromConflictList(verticesToBeDeleted);
               }
               newFace.addNewConflictVertices(verticesToBeAdded);
           }
           iteration++;
           if (iteration == 1) {
               furthestPoints.clear();
               horizonEdgee.clear();
               testFace = faces.get(7);
           }
        }
        /*definingPoints.addAll(testFace.returnDefiningVertices());
        definingPoints.addAll(testFace2.returnDefiningVertices());*/
        /*definingPoints.addAll(faces.get(13).returnDefiningVertices());
        definingPoints.addAll(faces.get(16).returnDefiningVertices());*/
        //definingPoints.addAll(deletedFaces.get(1).returnDefiningVertices());
        // definingPoints.addAll(deletedFaces.get(3).returnDefiningVertices());
        definingPoints.addAll(deletedFaces.get(4).returnDefiningVertices());
        definingPoints.addAll(deletedFaces.get(4).returnNeighbouringFaces().get(0).returnDefiningVertices());
        // SuppVec: (-4,232E-1 -7,000E-1 -2,000E-2); Normal: (-1,952E-1  0,000E+0  9,808E-1); Offset: 0.062974274
        // definingPoints.addAll(deletedFaces.get(9).returnDefiningVertices());
        // definingPoints.addAll(deletedFaces.get(11).returnDefiningVertices());
        /*definingPoints.add(horizonEdgee.get(11).getTail());
        definingPoints.add(horizonEdgee.get(11).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(12).getTail());
        definingPoints.add(horizonEdgee.get(12).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(13).getTail());
        definingPoints.add(horizonEdgee.get(13).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(14).getTail());
        definingPoints.add(horizonEdgee.get(14).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(15).getTail());
        definingPoints.add(horizonEdgee.get(15).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(16).getTail());
        definingPoints.add(horizonEdgee.get(16).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(17).getTail());
        definingPoints.add(horizonEdgee.get(17).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(18).getTail());
        definingPoints.add(horizonEdgee.get(18).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));
        definingPoints.add(horizonEdgee.get(19).getTail());
        definingPoints.add(horizonEdgee.get(19).getTop());
        definingPoints.add(new Vector3f(4.619E-1f, 6.087E-1f, -2.000E-2f));*/
        /*definingPoints.addAll(faces.get(16).returnDefiningVertices());
        definingPoints.addAll(faces.get(18).returnDefiningVertices());
        definingPoints.addAll(faces.get(5).returnDefiningVertices());
        definingPoints.addAll(faces.get(15).returnDefiningVertices());
        definingPoints.addAll(faces.get(13).returnDefiningVertices());
        definingPoints.addAll(faces.get(2).returnDefiningVertices());*/
        for (Face face : faces) {
            // definingPoints.addAll(face.returnDefiningVertices());
        }
        definingPoints.add(new Vector3f(0.0f));
        System.out.println("Time taken for Quickhull: " + (System.currentTimeMillis() - startingTime));
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
