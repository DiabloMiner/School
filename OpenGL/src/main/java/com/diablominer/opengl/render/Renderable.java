package com.diablominer.opengl.render;

public interface Renderable {

    void draw(ShaderProgram shaderProgram);

    void cleanUp();

}
