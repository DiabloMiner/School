package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.main.GameObject;
import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.render.renderables.Model;
import com.diablominer.opengl.render.RenderingEngineUnit;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class RenderablePointLight implements GameObject {

    private Model model;
    private PointLight pointLight;
    private final Vector3f constantPosition;

    public RenderablePointLight(PointLight pointLight, String path, LogicalEngine logicalEngine, RenderingEngineUnit renderingEngineUnit) {
        logicalEngine.addGameObject(this);
        constantPosition = pointLight.getPosition();
        model = new Model(path, renderingEngineUnit, new Vector3f(0.0f, 0.0f, 0.0f));
        this.pointLight = pointLight;
    }

    @Override
    public void updateObjectState(double timeStep) {
        pointLight.setPosition(Transforms.getSumOf2Vectors(new Vector3f(10.0f * (float) Math.cos(timeStep), 5.0f * (float) Math.sin(timeStep), (float) Math.cos(timeStep)), constantPosition));
        model.setPosition(pointLight.getPosition());
    }

    public Model getModel() {
        return model;
    }

    public PointLight getPointLight() {
        return pointLight;
    }
}
