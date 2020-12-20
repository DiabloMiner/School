package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TransparencyRenderingEngineUnit extends RenderingEngineUnit {

    private DirectionalLight dirLight;
    private PointLight pointLight;
    private SpotLight spotLight;
    private List<Renderable> sortedRenderables;
    private float shininess = 32.0f;

    public TransparencyRenderingEngineUnit(ShaderProgram shaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight) {
        super(shaderProgram);
        sortedRenderables = new ArrayList<>();
        this.dirLight = dirLight;
        this.pointLight = pointLight;
        this.spotLight = spotLight;
    }

    @Override
    public void updateRenderState(Camera camera, Window window) {
        this.getShaderProgram().setUniformMat4F("projection", Transforms.createProjectionMatrix(camera.fov, true, window.getWIDTH(), window.getHEIGHT(), 0.1f, 100.0f));
        this.getShaderProgram().setUniformMat4F("view", new Matrix4f().lookAt(camera.cameraPos, camera.getLookAtPosition(), camera.cameraUp));
        this.getShaderProgram().setUniformVec3F("viewPos", camera.cameraPos);
        this.getShaderProgram().setUniform1F("material.shininess", shininess);

        this.getShaderProgram().setUniformVec3F("dirLight.direction", dirLight.getDirection());
        this.getShaderProgram().setUniformVec3F("dirLight.ambient", dirLight.getAmbient());
        this.getShaderProgram().setUniformVec3F("dirLight.diffuse", dirLight.getDiffuse());
        this.getShaderProgram().setUniformVec3F("dirLight.specular", dirLight.getSpecular());

        this.getShaderProgram().setUniformVec3F("pointLight.position", pointLight.getPosition());
        this.getShaderProgram().setUniformVec3F("pointLight.ambient", pointLight.getAmbient());
        this.getShaderProgram().setUniformVec3F("pointLight.diffuse", pointLight.getDiffuse());
        this.getShaderProgram().setUniformVec3F("pointLight.specular", pointLight.getSpecular());
        this.getShaderProgram().setUniform1F("pointLight.constant", pointLight.getConstant());
        this.getShaderProgram().setUniform1F("pointLight.linear", pointLight.getLinear());
        this.getShaderProgram().setUniform1F("pointLight.quadratic", pointLight.getQuadratic());

        this.getShaderProgram().setUniformVec3F("spotLight.position", camera.cameraPos);
        this.getShaderProgram().setUniformVec3F("spotLight.direction", camera.cameraFront);
        this.getShaderProgram().setUniformVec3F("spotLight.ambient", spotLight.getAmbient());
        this.getShaderProgram().setUniformVec3F("spotLight.diffuse", spotLight.getDiffuse());
        this.getShaderProgram().setUniformVec3F("spotLight.specular", spotLight.getSpecular());
        this.getShaderProgram().setUniform1F("spotLight.constant", spotLight.getConstant());
        this.getShaderProgram().setUniform1F("spotLight.linear", spotLight.getLinear());
        this.getShaderProgram().setUniform1F("spotLight.quadratic", spotLight.getQuadratic());
        this.getShaderProgram().setUniform1F("spotLight.cutOff", spotLight.getCutOff());
        this.getShaderProgram().setUniform1F("spotLight.outerCutOff", spotLight.getOuterCutOff());

        this.getShaderProgram().setUniformMat4F("model", new Matrix4f().identity());

        this.getRenderables().sort((o1, o2) -> {
            float distance1 = Math.abs(o1.getPosition().distance(camera.cameraPos));
            float distance2 = Math.abs(o2.getPosition().distance(camera.cameraPos));
            return Float.compare(distance1, distance2);
        });
        Collections.reverse(this.getRenderables());
    }

    @Override
    public void render() {
        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
        GL33.glStencilMask(0xFF);
        renderAllRenderables();
    }
}