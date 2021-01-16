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
        shaderProgram.setUniformVec3F("dirLight.ambient", dirLight.getAmbient());
        shaderProgram.setUniformVec3F("dirLight.diffuse", dirLight.getDiffuse());
        shaderProgram.setUniformVec3F("dirLight.specular", dirLight.getSpecular());

        shaderProgram.setUniformVec3F("pointLight.position", pointLight.getPosition());
        shaderProgram.setUniformVec3F("pointLight.ambient", pointLight.getAmbient());
        shaderProgram.setUniformVec3F("pointLight.diffuse", pointLight.getDiffuse());
        shaderProgram.setUniformVec3F("pointLight.specular", pointLight.getSpecular());
        shaderProgram.setUniform1F("pointLight.constant", pointLight.getConstant());
        shaderProgram.setUniform1F("pointLight.linear", pointLight.getLinear());
        shaderProgram.setUniform1F("pointLight.quadratic", pointLight.getQuadratic());

        shaderProgram.setUniformVec3F("spotLight.position", camera.position);
        shaderProgram.setUniformVec3F("spotLight.direction", camera.front);
        shaderProgram.setUniformVec3F("spotLight.ambient", spotLight.getAmbient());
        shaderProgram.setUniformVec3F("spotLight.diffuse", spotLight.getDiffuse());
        shaderProgram.setUniformVec3F("spotLight.specular", spotLight.getSpecular());
        shaderProgram.setUniform1F("spotLight.constant", spotLight.getConstant());
        shaderProgram.setUniform1F("spotLight.linear", spotLight.getLinear());
        shaderProgram.setUniform1F("spotLight.quadratic", spotLight.getQuadratic());
        shaderProgram.setUniform1F("spotLight.cutOff", spotLight.getCutOff());
        shaderProgram.setUniform1F("spotLight.outerCutOff", spotLight.getOuterCutOff());

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
