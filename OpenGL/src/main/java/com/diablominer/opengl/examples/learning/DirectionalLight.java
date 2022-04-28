package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class DirectionalLight implements Light {

    public static final int shaderIndex = 0;
    public static List<DirectionalLight> allDirectionalLights = new ArrayList<>();

    public Vector3f direction, color;

    public DirectionalLight(Vector3f direction, Vector3f color) {
        this.direction = direction;
        this.color = color;
        allDirectionalLights.add(this);
        allLights.add(this);
    }

    @Override
    public Vector3f getColor() {
        return color;
    }

    @Override
    public List<Vector4f> getData() {
        return new ArrayList<>(Arrays.asList(new Vector4f(direction, 0.0f), new Vector4f(color, 0.0f)));
    }
}
