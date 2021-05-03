package com.diablominer.opengl.render;

import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class QuickHull {

    private final List<Face> faces = new ArrayList<>();
    private final List<Vector3f> definingPoints = new ArrayList<>();
    private final List<Vector3f> points;
    private final float epsilon;

    public QuickHull(Collection<Vector3f> vertices) {
        long startingTime = System.currentTimeMillis();

        List<Vector3f> remainingVertices = new LinkedList<>(vertices);
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
        startingPoints.add(new Vector3f());
        startingPoints.add(new Vector3f());
        for (Vector3f maxPoint: maxPoints) {
            for (Vector3f minPoint : minPoints) {
                if (maxPoint.distance(minPoint) > startingPoints.get(0).distance(startingPoints.get(1))) {
                    startingPoints.set(0, maxPoint);
                    startingPoints.set(1, minPoint);
                }
            }
        }

        startingPoints.set(2, new Vector3f(startingPoints.get(0)).add(new Vector3f(0.01f)));
        for (Vector3f vertex : remainingVertices) {
            float distance1 = new Vector3f(vertex).sub(new Vector3f(startingPoints.get(0))).cross(new Vector3f(startingPoints.get(0)).sub(new Vector3f(startingPoints.get(1))).normalize()).length();
            float distance2 = new Vector3f(vertex).sub(new Vector3f(startingPoints.get(2))).cross(new Vector3f(startingPoints.get(0)).sub(new Vector3f(startingPoints.get(1))).normalize()).length();
            if (distance1 > distance2) {
                startingPoints.set(2, vertex);
            }
        }

        startingPoints.set(3, new Vector3f(startingPoints.get(2)).add(new Vector3f(0.01f)));
        for (Vector3f vertex : remainingVertices) {
            float distance1 = new Vector3f(vertex).sub(new Vector3f(startingPoints.get(0))).dot(new Vector3f(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(1))).normalize());
            float distance2 = new Vector3f(vertex).sub(new Vector3f(startingPoints.get(3))).dot(new Vector3f(startingPoints.get(0)).cross(new Vector3f(startingPoints.get(1))).normalize());
            if (distance1 > distance2) {
                startingPoints.set(3, vertex);
            }
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

        LinkedList<Vector3f> toBeRemoved = new LinkedList<>();
        for (Vector3f vertex : remainingVertices) {
            if (!isPointInsideTetrahedron(startingPoints.get(0), startingPoints.get(1), startingPoints.get(2), startingPoints.get(3), vertex) && !toBeRemoved.contains(vertex)) {
                toBeRemoved.add(vertex);
            } else if (!toBeRemoved.contains(vertex)) {
                for (Face face : faces) {
                    if (face.signedDistance(vertex) > epsilon) {
                        face.addNewConflictVertex(vertex);
                    }
                }
            }
        }
        remainingVertices.removeAll(toBeRemoved);

        while (!remainingVertices.isEmpty()) {
           for (Face face : faces) {
               if (!face.isConflictListEmpty()) {
                   Set<Face> visibleFaces = new HashSet<>();
                   Set<Edge> horizonEdges = new HashSet<>();
                   List<Face> newFaces = new ArrayList<>();
                   Vector3f furthestPoint = face.returnFurthestPoint();
                   visibleFaces.add(face);

                   for (Face visibleFace : visibleFaces) {
                       for (Face neighbouringFace : visibleFace.returnNeighbouringFaces()) {
                           float signedDistance = neighbouringFace.signedDistance(furthestPoint);
                           if (signedDistance > epsilon) {
                               visibleFaces.add(neighbouringFace);
                           } else if (signedDistance < -epsilon) {
                               horizonEdges.add(visibleFace.returnEdgeFromNeighbouringFace(neighbouringFace));
                           }
                       }
                   }

                   for (Edge horizonEdge : horizonEdges) {
                       Face newFace = new Face(horizonEdge.getTail(), horizonEdge.getTop(), furthestPoint);
                       newFace.addNewNeighbouringFace(horizonEdge.getFace());
                       newFaces.add(newFace);
                   }
                   for (Face newFace : newFaces) {
                       for (Face otherFace : visibleFaces) {
                           for (Vector3f vertex : otherFace.returnConflictList()) {
                               if (newFace.signedDistance(vertex) > epsilon) {
                                   newFace.addNewConflictVertex(vertex);
                               }
                           }
                       }
                   }

                   faces.removeAll(visibleFaces);
                   remainingVertices.removeIf(vertex -> {
                       for (Face testingFace : faces) {
                           if (testingFace.signedDistance(vertex) < epsilon) {
                               return true;
                           }
                       }
                       return false;
                   });
                   faces.addAll(newFaces);
                   for (Face newFace : newFaces) {
                       for (Face potentialNeighbourFace : faces) {
                           newFace.addNewNeighbouringFace(potentialNeighbourFace);
                       }
                   }

                   visibleFaces.clear();
                   horizonEdges.clear();
                   newFaces.clear();
               }
           }
        }
        for (Face face : faces) {
            definingPoints.addAll(face.returnDefiningVertices());
        }
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

    private boolean isPointInsideTetrahedron(Vector3f r1, Vector3f r2, Vector3f r3, Vector3f r4, Vector3f r) {
        /**
        For reference see: https://en.wikipedia.org/wiki/Barycentric_coordinate_system
         */
        Matrix3f T = new Matrix3f();
        T.set(r1.x - r4.x, r1.y - r4.y, r1.z - r4.z,
              r2.x - r4.x, r2.y - r4.y, r2.z - r4.z,
              r3.x - r4.x, r3.y - r4.y, r3.z - r4.z);
        T.invert();
        Vector3f lambda = new Vector3f(r).sub(new Vector3f(r4)).mul(T);
        Vector4f allLambdas = new Vector4f(lambda, 1 - lambda.x - lambda.y - lambda.z);
        return allLambdas.x > 0 && allLambdas.y > 0 && allLambdas.z > 0 && allLambdas.w > 0;
    }

}
