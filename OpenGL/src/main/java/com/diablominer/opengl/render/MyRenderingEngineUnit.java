package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;

public class MyRenderingEngineUnit extends RenderingEngineUnit {

    private DirectionalLight dirLight;
    private PointLight pointLight;
    private SpotLight spotLight;
    private float shininess = 32.0f;

    public MyRenderingEngineUnit(ShaderProgram shaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight) {
        super(shaderProgram);
        this.dirLight = dirLight;
        this.pointLight = pointLight;
        this.spotLight = spotLight;
    }

    @Override
    public void updateRenderState(Camera camera, Window window) {
        shaderProgram.setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
        shaderProgram.setUniformMat4F("view", new Matrix4f().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp));
        shaderProgram.setUniformVec3F("viewPos", camera.cameraPos);
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

        shaderProgram.setUniformVec3F("spotLight.position", camera.cameraPos);
        shaderProgram.setUniformVec3F("spotLight.direction", camera.cameraFront);
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

    @Override
    public void render() {
        renderAllRenderables();
    }
}
