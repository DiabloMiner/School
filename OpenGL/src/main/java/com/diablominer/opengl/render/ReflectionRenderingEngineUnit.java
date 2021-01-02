package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;

public class ReflectionRenderingEngineUnit extends RenderingEngineUnit {

    public ReflectionRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    @Override
    public void updateRenderState(Camera camera) {
        shaderProgram.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, camera.aspect, 0.1f, 100.0f));
        shaderProgram.setUniformMat4F("view", new Matrix4f().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp));
        shaderProgram.setUniformVec3F("viewPos", camera.cameraPos);
        shaderProgram.setUniformMat4F("model", new Matrix4f().identity());
    }

    @Override
    public void render() {
        renderAllRenderables();
    }
}
