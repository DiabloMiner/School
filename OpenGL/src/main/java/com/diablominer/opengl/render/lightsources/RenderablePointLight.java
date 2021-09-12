package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.examples.modelloading.Vertex;
import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.collisiondetection.AxisAlignedBoundingBox;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.RenderingEngineUnit;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class RenderablePointLight extends PhysicsObject {

    private Model model;
    private PointLight pointLight;

    // Primary values
    private Vector3f position;
    private Vector3f momentum = new Vector3f(0.0f);
    private Vector3f force = new Vector3f(-0.0f, 0.0f, 0.0f);
    private Quaternionf orientation = new Quaternionf();
    private Vector3f angularMomentum = new Vector3f(0.0f);
    private Vector3f torque = new Vector3f(0.0f, 1.0f, 0.0f);

    // Secondary values
    private Vector3f angularVelocity = new Vector3f(0.0f);
    private Matrix4f modelMatrix = new Matrix4f().identity();

    // Constant values
    private float size = 2.0f;
    private Matrix3f inertia;

    public RenderablePointLight(PointLight pointLight, String path, LogicalEngine logicalEngine, RenderingEngineUnit renderingEngineUnit) {
        super();
        logicalEngine.addGameObject(this);
        this.pointLight = pointLight;
        model = new Model(path, renderingEngineUnit, pointLight.getPosition());

        List<Vector3f> uniqueVertices = model.getAllUniqueVertices();
        mass = 10.0f;
        position = determineCenterOfMass(uniqueVertices);
        uniqueVertices.forEach(vector3f -> vector3f.add(new Vector3f(position).mul(-1.0f)));
        inertia = createInertiaTensor(uniqueVertices, true);
        Transforms.multiplyListWithMatrix(uniqueVertices, new Matrix4f().identity().translate(pointLight.getPosition()));
        velocity = new Vector3f(0.0f);
        this.bv = new AxisAlignedBoundingBox(position, size);
    }

    @Override
    public void updateObjectState(double timeStep) {
        Matrix3f inertia = new Matrix3f().identity().rotate(orientation).mul(this.inertia).mul(new Matrix3f().identity().rotate(orientation).transpose());

        angularMomentum = new Vector3f(torque).mul((float) timeStep);
        angularVelocity.add(new Vector3f(angularMomentum).mul(new Matrix3f(inertia).invert()));
        orientation.integrate((float) timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z);

        momentum = new Vector3f(force).mul((float) timeStep);
        velocity.add(new Vector3f(momentum).div(mass));
        position.add(new Vector3f(velocity).mul((float) timeStep));

        modelMatrix = new Matrix4f().identity().translationRotate(position.x, position.y, position.z, orientation);
        pointLight.setPosition(position);
        model.setModelMatrix(modelMatrix);
    }

    @Override
    public void collisionDetectionAndResponse(Set<PhysicsObject> physicsObjects) {
        for (PhysicsObject physicsObject : physicsObjects) {
            // Broad Phase
            if (this.bv.isIntersecting(physicsObject.bv)) {
                // Narrow Phase
                /*if (physicsObject.mass == this.mass) {
                    Vector3f u1 = new Vector3f(this.velocity);
                    this.velocity = new Vector3f(physicsObject.velocity);
                    physicsObject.velocity = u1;
                } else {
                    Vector3f u1 = new Vector3f(this.velocity);
                    Vector3f u2 = new Vector3f(physicsObject.velocity);
                    float m1 = this.mass;
                    float m2 = physicsObject.mass;
                    this.velocity = new Vector3f((u1.mul(((m1 - m2) / (m1 + m2)))).add(u2.mul((2 * m2) / (m1 + m2))));
                    physicsObject.velocity = new Vector3f(u1.mul(((2 * m1) / (m1 + m2))).add(u2.mul(((m2 - m1) / (m1 + m2)))));
                }*/
            }
        }
    }

    @Override
    public void predictGameObjectState(double timeStep) {
        Matrix3f inertia = new Matrix3f().identity().rotate(orientation).mul(this.inertia).mul(new Matrix3f().identity().rotate(orientation).transpose());

        Vector3f angularMomentum = new Vector3f(torque).mul((float) timeStep);
        Vector3f angularVelocity = new Vector3f(this.angularVelocity).add(new Vector3f(angularMomentum).mul(new Matrix3f(inertia).invert()));
        Quaternionf orientation = new Quaternionf(this.orientation).integrate((float) timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z);

        Vector3f momentum = new Vector3f(force).mul((float) timeStep);
        Vector3f velocity = new Vector3f(this.velocity).add(new Vector3f(momentum).div(mass));
        Vector3f position = new Vector3f(this.position).add(new Vector3f(velocity).mul((float) timeStep));

        Matrix4f modelMatrix = new Matrix4f().identity().translationRotate(position.x, position.y, position.z, orientation);
        pointLight.setPosition(position);
        model.setModelMatrix(modelMatrix);
    }

    public Model getModel() {
        return model;
    }

    public PointLight getPointLight() {
        return pointLight;
    }

    private Vector3f determineCenterOfMass(List<Vector3f> uniqueVertices) {
        Vector3f result = new Vector3f();
        for (Vector3f uniqueVertex : uniqueVertices) {
            result.add(new Vector3f(uniqueVertex).mul(mass / uniqueVertices.size()));
        }
        return result.div(mass);
    }

    /**
     * All vertices are assumed to be defined relative to the center of mass.
     * The center of mass is assumed to be at (0, 0, 0).
     */
    private Matrix3f createInertiaTensor(List<Vector3f> uniqueVertices, boolean isSolid) {
        List<Vector3f> vertices = uniqueVertices;
        if (isSolid) {
            vertices = generateSolidGeometry(vertices, 100);
        }
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float slotResult = 0.0f;
                for (Vector3f uniqueVertex : vertices) {
                    slotResult += (mass / vertices.size()) * ((uniqueVertex.lengthSquared() * kroneckerDelta(i, j)) - (uniqueVertex.get(i) * uniqueVertex.get(j)));
                }
                result.set(j, i, slotResult);
            }
        }
        return result;
    }

    private float kroneckerDelta(int i, int j) {
        if (i != j) {
            return 0.0f;
        } else {
            return 1.0f;
        }
    }

    private List<Vector3f> generateSolidGeometry(List<Vector3f> vertices, int steps) {
        List<Vector3f> newList = new ArrayList<>();
        float factor = 1.0f / steps;
        for (int i = 0; i < steps; i++) {
            for (Vector3f vertex : vertices) {
                newList.add(new Vector3f(vertex).mul(factor));
            }
            factor += (1.0f / steps);
        }
        return newList;
    }

}
