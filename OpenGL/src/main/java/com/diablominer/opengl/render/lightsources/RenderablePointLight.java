package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.main.PhysicsObject;
import com.diablominer.opengl.collisiondetection.AxisAlignedBoundingBox;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.RenderingEngineUnit;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

public class RenderablePointLight extends PhysicsObject {

    private Model model;
    private PointLight pointLight;

    // Primary values
    private Vector3f position;
    private Vector3f momentum = new Vector3f(0.0f);
    private Vector3f force = new Vector3f(-0.5f, 0.0f, 0.5f);
    private Quaternionf orientation = new Quaternionf();
    private Vector3f angularMomentum = new Vector3f(0.0f);
    private Vector3f torque = new Vector3f(0.0f, 0.0f, 0.0f);

    // Secondary values
    private Vector3f angularVelocity = new Vector3f(0.0f);
    private Matrix4f modelMatrix = new Matrix4f().identity();

    // Constant values
    private float size = 2.0f;
    private Matrix3f inertia;

    public RenderablePointLight(PointLight pointLight, String path, LogicalEngine logicalEngine, RenderingEngineUnit renderingEngineUnit) {
        super();
        logicalEngine.addGameObject(this);
        this.bv = new AxisAlignedBoundingBox(position, size);
        this.pointLight = pointLight;
        model = new Model(path, renderingEngineUnit, position);

        List<Vector3f> uniqueVertices = model.getAllUniqueVertices();
        mass = 10.0f;
        position = determineCenterOfMass(uniqueVertices);
        velocity = new Vector3f(0.0f);
        inertia = createInertiaTensor(uniqueVertices, position);
    }

    @Override
    public void updateObjectState(double timeStep) {
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

    private Matrix3f createInertiaTensor(List<Vector3f> uniqueVertices, Vector3f centerPoint) {
        Matrix3f result = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                float slotResult = mass;
                for (Vector3f uniqueVertex : uniqueVertices) {
                    Vector3f r = new Vector3f(uniqueVertex).sub(centerPoint);
                    slotResult += (r.length() * r.length() * kroneckerDelta(i, j) - r.get(i) * r.get(j));
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

}
