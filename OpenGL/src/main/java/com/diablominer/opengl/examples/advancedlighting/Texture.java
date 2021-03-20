package com.diablominer.opengl.examples.advancedlighting;

public class Texture {

    public int id;
    public String type;
    public String path;

    public Texture(int id, String type, String path) {
        this.id = id;
        this.type = type;
        this.path = path;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setAll(int id, String type) {
        this.id = id;
        this.type = type;
    }

}
