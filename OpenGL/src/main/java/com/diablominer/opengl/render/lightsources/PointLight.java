package com.diablominer.opengl.render.lightsources;

import com.diablominer.opengl.main.GameObject;
import com.diablominer.opengl.main.LogicalEngine;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class PointLight implements GameObject {

    private Vector3f position, ambient, diffuse, specular;
    private float constant, linear, quadratic;

    public PointLight(Vector3f position, Vector3f ambient, Vector3f diffuse, Vector3f specular, float constant, float linear, float quadratic, LogicalEngine logicalEngine) {
        logicalEngine.addGameObject(this);
        this.position = position;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    @Override
    public void updateObjectState() {
        setPosition(Transforms.getProductOf2Vectors(position, new Vector3f((float) Math.cos(GLFW.glfwGetTime()), (float) Math.sin(GLFW.glfwGetTime()), (float) Math.cos(GLFW.glfwGetTime()))));
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
}
