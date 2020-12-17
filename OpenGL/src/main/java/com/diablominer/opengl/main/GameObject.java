package com.diablominer.opengl.main;

import org.joml.Matrix4f;

public abstract class GameObject {

    public GameObject(Game game, Matrix4f matrix) {
        game.addGameObject(this);
    }

    public abstract void updateObjectState();

}
