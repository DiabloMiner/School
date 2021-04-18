package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.main.GameObject;
import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.RenderingEngineUnit;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.CallbackI;

public class RenderablePointLight implements GameObject {

    private Model model;
    private PointLight pointLight;

    private float mass = 1.0f;
    private Vector3f force = new Vector3f(0.0f, 0.0f, 1.0f);
    private Vector3f velocity = new Vector3f(0.0f);
    private Vector3f position;

    public RenderablePointLight(PointLight pointLight, String path, LogicalEngine logicalEngine, RenderingEngineUnit renderingEngineUnit) {
        logicalEngine.addGameObject(this);
        position = pointLight.getPosition();
        System.out.println("Starting position: " + position);
        model = new Model(path, renderingEngineUnit, new Vector3f(0.0f, 0.0f, 0.0f));
        this.pointLight = pointLight;
    }

    @Override
    public void updateObjectState(double timeStep) {
        velocity.add(new Vector3f(force).div(mass).mul((float) timeStep));
        position.add(new Vector3f(velocity).mul((float) timeStep));

        pointLight.setPosition(position);
        model.setPosition(pointLight.getPosition());
    }

    @Override
    public void predictGameObjectState(double timeStep) {
        Vector3f vel = new Vector3f(velocity).add(new Vector3f(force).div(mass).mul((float) timeStep));
        Vector3f predictedPosition = new Vector3f(position).add(new Vector3f(vel).mul((float) timeStep));

        pointLight.setPosition(predictedPosition);
        model.setPosition(predictedPosition);
    }

    public Model getModel() {
        return model;
    }

    public PointLight getPointLight() {
        return pointLight;
    }
}
