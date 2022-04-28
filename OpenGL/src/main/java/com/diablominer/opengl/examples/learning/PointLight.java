package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class PointLight implements Light {

    public static final int shaderIndex = 1;
    public static List<PointLight> allPointLights = new ArrayList<>();

    public Vector3f position, color;

    public PointLight(Vector3f position, Vector3f color) {
        this.position = position;
        this.color = color;
        allPointLights.add(this);
        allLights.add(this);
    }

    @Override
    public Vector3f getColor() {
        return color;
    }

    @Override
    public List<Vector4f> getData() {
        return new ArrayList<>(Arrays.asList(new Vector4f(position, 0.0f), new Vector4f(color, 0.0f)));
    }
}
