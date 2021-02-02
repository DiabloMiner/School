package com.diablominer.opengl.render.lightsources;

import org.joml.Vector3f;

public class SpotLight {

    private Vector3f position, direction, ambient, diffuse, specular;
    private float constant, linear, quadratic, cutOff, outerCutOff, farPlane;

    public SpotLight(Vector3f position, Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular, float constant, float linear, float quadratic, float cutOff, float outerCutOff, float farPlane) {
        this.position = position;
        this.direction = direction;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
        this.cutOff = cutOff;
        this.outerCutOff = outerCutOff;
        this.farPlane = farPlane;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
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

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public float getLinear() {
        return linear;
    }

    public void setLinear(float linear) {
        this.linear = linear;
    }

    public float getQuadratic() {
        return quadratic;
    }

    public void setQuadratic(float quadratic) {
        this.quadratic = quadratic;
    }

    public float getCutOff() {
        return cutOff;
    }

    public void setCutOff(float cutOff) {
        this.cutOff = cutOff;
    }

    public float getOuterCutOff() {
        return outerCutOff;
    }

    public void setOuterCutOff(float outerCutOff) {
        this.outerCutOff = outerCutOff;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public float getFarPlane() {
        return farPlane;
    }
}
