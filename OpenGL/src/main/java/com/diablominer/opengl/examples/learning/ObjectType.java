package com.diablominer.opengl.examples.learning;

public enum ObjectType {
    DYNAMIC(true),
    STATIC(false);

    public boolean performTimeStep;

    ObjectType(boolean performTimeStep) {
        this.performTimeStep = performTimeStep;
    }
}

