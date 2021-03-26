package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import org.joml.Matrix4f;

public class MyRenderingEngineUnit extends RenderingEngineUnit {

    protected DirectionalLight dirLight;
    protected PointLight pointLight;
    protected SpotLight spotLight;
    protected float shininess = 128.0f;

    public MyRenderingEngineUnit(ShaderProgram shaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight) {
        super(shaderProgram);
        this.dirLight = dirLight;
        this.pointLight = pointLight;
        this.spotLight = spotLight;
    }

    public MyRenderingEngineUnit(ShaderProgram shaderProgram, ShaderProgram alternativeShaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight) {
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

    @Override
    public void render() {
        renderAllRenderables();
    }

    @Override
    public void renderAlternative() {
        renderAllRenderablesAlternative();
    }
}
