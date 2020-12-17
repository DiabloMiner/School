package com.diablominer.opengl.render.lightsources;

import org.joml.Vector3f;

public class DirectionalLight {

    private Vector3f direction, ambient, diffuse, specular;

    public DirectionalLight(Vector3f dir, Vector3f ambient, Vector3f diffuse, Vector3f specular) {
        this.direction = dir;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
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
}
