package com.diablominer.opengl.examples.modelloading;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Vertex {

    public Vector3f position;
    public Vector3f normal;
    public Vector2f texCoords;

    public Vertex(Vector3f position, Vector3f normal, Vector2f texCoords) {
        this.position = position;
        this.normal = normal;
        this.texCoords = texCoords;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setNormal(Vector3f normal) {
        this.normal = normal;
    }

    public void setTexCoords(Vector2f texCoords) {
        this.texCoords = texCoords;
    }

    public void setAll(Vector3f position, Vector3f normal, Vector2f texCoords) {
        this.position = position;
        this.normal = normal;
        this.texCoords = texCoords;
    }

}
