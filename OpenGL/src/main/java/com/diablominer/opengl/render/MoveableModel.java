package com.diablominer.opengl.render;

import com.diablominer.opengl.main.GameObject;
import org.joml.Vector3f;

import java.util.concurrent.atomic.AtomicReference;

public class MoveableModel extends Model implements GameObject {

    private AtomicReference<Vector3f> newPosition;

    public MoveableModel(String path, RenderingEngineUnit renderingEngineUnit, Vector3f position) {
        super(path, renderingEngineUnit, position);
        newPosition.set(new Vector3f(0.0f, 0.0f, 0.0f));
    }

    @Override
    public void updateObjectState() {
        setPosition(newPosition.get());
    }

    public Vector3f getNewPosition() {
        return newPosition.get();
    }

    public void setNewPosition(Vector3f newPosition) {
        this.newPosition.set(newPosition);
    }

}
