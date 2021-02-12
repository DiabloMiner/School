package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

import java.util.Collections;

public class TransparencyRenderingEngineUnit extends RenderingEngineUnit {

    private DirectionalLight dirLight;
    private PointLight pointLight;
    private SpotLight spotLight;
    private float shininess = 32.0f;

    public TransparencyRenderingEngineUnit(ShaderProgram shaderProgram, ShaderProgram alternativeShaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight) {
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

        this.getShaderProgram().setUniformMat4F("model", new Matrix4f().identity());

        this.getRenderables().sort((o1, o2) -> {
            float distance1 = Math.abs(o1.getPosition().distance(camera.position));
            float distance2 = Math.abs(o2.getPosition().distance(camera.position));
            return Float.compare(distance1, distance2);
        });
        Collections.reverse(this.getRenderables());
    }

    @Override
    public void render() {
        shaderProgram.setUniform1I("material.texture_displacement1", 0);
        GL33.glDisable(GL33.GL_CULL_FACE);
        renderAllRenderables();
        GL33.glEnable(GL33.GL_CULL_FACE);
    }

    @Override
    public void renderAlternative() {
        shaderProgram.setUniform1I("material.texture_displacement1", 0);
        GL33.glDisable(GL33.GL_CULL_FACE);
        renderAllRenderablesAlternative();
        GL33.glEnable(GL33.GL_CULL_FACE);
    }
}
