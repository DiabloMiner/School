package com.diablominer.opengl.main;

public interface GameObject {

    void updateObjectState(double timeStep);

    void predictGameObjectState(double timeStep);

}
