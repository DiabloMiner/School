package com.diablominer.opengl.render.lightsources;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class DirectionalLight {

    private Vector3f direction, ambient, diffuse, specular;
    private Matrix4f lightSpaceMatrix;

    public DirectionalLight(Vector3f dir, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.direction = dir;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        lightSpaceMatrix = new Matrix4f().identity();
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public Vector3f getAmbient() {
        return ambient;
    }

    public void setAmbient(Vector3f ambient) {
        this.ambient = ambient;
    }

    public Vector3f getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(Vector3f diffuse) {
        this.diffuse = diffuse;
    }

    public Vector3f getSpecular() {
        return specular;
    }

    public void setSpecular(Vector3f specular) {
        this.specular = specular;
    }

    public Matrix4f getLightSpaceMatrix() {
        return lightSpaceMatrix;
    }

    public void setLightSpaceMatrix(Matrix4f lightSpaceMatrix) {
        this.lightSpaceMatrix = lightSpaceMatrix;
    }
}
