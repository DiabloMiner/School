package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;

public class ReflectionRenderingEngineUnit extends RenderingEngineUnit {

    public ReflectionRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    @Override
    public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
        shaderProgram.setUniformVec3F("viewPos", camera.cameraPos);
        shaderProgram.setUniformMat4F("model", new Matrix4f().identity());
    }

    @Override
    public void render() {
        renderAllRenderables();
    }
}
