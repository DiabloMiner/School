package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class SpotLight implements Light {

    public static final int sortingIndex = 2;
    public static List<SpotLight> allSpotLights = new ArrayList<>();

    public Vector3f position, direction, color;

    public SpotLight(Vector3f position, Vector3f direction, Vector3f color) {
        this.position = position;
        this.direction = direction;
        this.color = color;
        allSpotLights.add(this);
        allLights.add(this);
    }

    @Override
    public Vector3f getColor() {
        return color;
    }

    @Override
    public List<Vector4f> getData() {
        return new ArrayList<>(Arrays.asList(new Vector4f(position, 0.0f), new Vector4f(direction, 0.0f), new Vector4f(color, 0.0f)));
    }

    @Override
    public void initializeShadowRenderer(Renderable[] renderables) {

    }

    @Override
    public Renderer getShadowRenderer() {
        return null;
    }

    @Override
    public Matrix4f getLightSpaceMatrix() {
        return null;
    }

}
