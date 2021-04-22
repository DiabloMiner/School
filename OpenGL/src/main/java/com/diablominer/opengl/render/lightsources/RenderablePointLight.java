package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.main.GameObject;
import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.RenderingEngineUnit;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RenderablePointLight implements GameObject {

    private Model model;
    private PointLight pointLight;

    private Vector3f pointForceIsAppliedOn = new Vector3f(0.0f, 0.5f, 0.5f);

    // Primary values
    private Vector3f position;
    private Vector3f momentum = new Vector3f(0.0f);
    private Vector3f force = new Vector3f(0.0f, 0.0f, 0.5f);
    private Quaternionf orientation = new Quaternionf();
    private Vector3f angularMomentum = new Vector3f(0.0f);
    private Vector3f torque = new Vector3f(0.0f, 0.0f, 0.0f);

    // Secondary values
    private Vector3f velocity = new Vector3f(0.0f);
    private Vector3f angularVelocity = new Vector3f(0.0f);
    private Matrix4f modelMatrix = new Matrix4f().identity();

    // Constant values
    private float mass = 1.0f;
    private float inertia = (1.0f/6.0f) * (float) Math.pow(1.0, 2.0) * mass;

    public RenderablePointLight(PointLight pointLight, String path, LogicalEngine logicalEngine, RenderingEngineUnit renderingEngineUnit) {
        logicalEngine.addGameObject(this);
        position = pointLight.getPosition();
        model = new Model(path, renderingEngineUnit, position);
        this.pointLight = pointLight;
    }

    @Override
    public void updateObjectState(double timeStep) {
        torque = new Vector3f(pointForceIsAppliedOn).cross(new Vector3f(force));

        angularMomentum = new Vector3f(torque).mul((float) timeStep);
        angularVelocity.add(new Vector3f(angularMomentum).div(inertia));
        orientation.integrate((float) timeStep, angularVelocity.x, angularVelocity.y, angularVelocity.z);

        momentum = new Vector3f(force).mul((float) timeStep);
        velocity.add(new Vector3f(momentum).div(mass));
        position.add(new Vector3f(velocity).mul((float) timeStep));

        modelMatrix = new Matrix4f().identity().translationRotate(position.x, position.y, position.z, orientation);
        pointLight.setPosition(position);
        model.setModelMatrix(modelMatrix);
    }

    @Override
    public void predictGameObjectState(double timeStep) {
        Vector3f angularMomentum = new Vector3f(torque).mul((float) timeStep);
        Vector3f angularVelocity = new Vector3f(this.angularVelocity).add(new Vector3f(angularMomentum).div(inertia));
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
}
