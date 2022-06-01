package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.*;

public class PointLight implements Light {

    public static final int sortingIndex = 1;
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
    public void setUniformData(ShaderProgram shaderProgram, int index) {
        int correctedIndex = index - DirectionalLight.allDirectionalLights.size();
        shaderProgram.setUniformVec3F("pointLight" + correctedIndex + ".position", position);
        shaderProgram.setUniformVec3F("pointLight" + correctedIndex + ".color", color);
    }

    @Override
    public void unbindShadowTextures() { }

    @Override
    public void initializeShadowRenderer(Renderable[] renderables) { }

    @Override
    public Renderer getShadowRenderer() {
        return null;
    }

    @Override
    public Matrix4f[] getLightSpaceMatrices() {
        return null;
    }

}
