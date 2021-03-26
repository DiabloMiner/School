package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class StencilTestRenderingEngineUnit extends RenderingEngineUnit {

    private DirectionalLight dirLight;
    private PointLight pointLight;
    private SpotLight spotLight;
    private float shininess = 32.0f;

    public StencilTestRenderingEngineUnit(ShaderProgram shaderProgram, ShaderProgram alternativeShaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight) {
        super(shaderProgram, alternativeShaderProgram);
        this.dirLight = dirLight;
        this.pointLight = pointLight;
        this.spotLight = spotLight;
    }

    @Override
    public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
        shaderProgram.setUniformVec3F("viewPos", camera.position);
        shaderProgram.setUniform1F("material.shininess", shininess);

        shaderProgram.setUniformVec3F("dirLight.direction", dirLight.getDirection());
        shaderProgram.setUniformVec3F("dirLight.color", dirLight.getColor());

        shaderProgram.setUniformVec3F("pointLight.position", pointLight.getPosition());
        shaderProgram.setUniformVec3F("pointLight.color", pointLight.getColor());

        shaderProgram.setUniformVec3F("spotLight.position", camera.position);
        shaderProgram.setUniformVec3F("spotLight.direction", camera.front);
        shaderProgram.setUniformVec3F("spotLight.color", spotLight.getColor());


        shaderProgram.setUniformMat4F("model", new Matrix4f().identity());
    }

    public void setPointLight(PointLight pointLight) {
        this.pointLight = pointLight;
    }

    @Override
    public void render() {
        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
        renderAllRenderables();
        GL33.glStencilFunc(GL33.GL_ALWAYS, 0, 0xFF);
    }

    @Override
    public void renderAlternative() {
        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
        renderAllRenderablesAlternative();
        GL33.glStencilFunc(GL33.GL_ALWAYS, 0, 0xFF);
    }
}
