package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import org.joml.Matrix4f;

public class ReflectionRenderingEngineUnit extends RenderingEngineUnit {

    public ReflectionRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    @Override
    public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
        shaderProgram.setUniformVec3F("viewPos", camera.position);
        shaderProgram.setUniformMat4F("model", new Matrix4f().identity());
    }

    @Override
    public void render() {
        renderAllRenderables();
    }

    @Override
    public void renderAlternative() {
        renderAllRenderablesAlternative();
    }
}
