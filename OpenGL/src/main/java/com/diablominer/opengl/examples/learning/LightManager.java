package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.*;

public class LightManager implements Manager {

    public List<Light> allLights;
    public List<DirectionalLight> allDirectionalLights;
    public List<PointLight> allPointLights;
    public List<SpotLight> allSpotLights;
    public Set<RenderableLight> allRenderableLights;

    public LightManager() {
        allLights = new ArrayList<>();
        allDirectionalLights = new ArrayList<>();
        allPointLights = new ArrayList<>();
        allSpotLights = new ArrayList<>();
        allRenderableLights = new HashSet<>();
    }

    public DirectionalLight addDirectionalLight(DirectionalLight dirLight) {
        allLights.add(dirLight);
        allDirectionalLights.add(dirLight);
        return dirLight;
    }

    public PointLight addPointLight(PointLight pointLight) {
        allLights.add(pointLight);
        allPointLights.add(pointLight);
        return pointLight;
    }

    public SpotLight addSpotLight(SpotLight spotLight) {
        allLights.add(spotLight);
        allSpotLights.add(spotLight);
        return spotLight;
    }

    public RenderablePointLight addRenderablePointLight(RenderablePointLight light) {
        allLights.add(light.light);
        allRenderableLights.add(light);
        allPointLights.add((PointLight) light.light);
        return light;
    }

    public void updateShadowMatrices() {
        for (Light light : allLights) {
            light.updateShadowMatrices();
        }
    }

    public void setLightUniforms(Collection<ShaderProgram> shaderProgramsUsingShadows) {
        updateShadowMatrices();
        for (ShaderProgram shaderProgram : shaderProgramsUsingShadows) {
            shaderProgram.bind();
            for (int i = 0; i < allDirectionalLights.size(); i++) {
                allDirectionalLights.get(i).setUniformData(shaderProgram, i);
            }
            for (int i = 0; i < allPointLights.size(); i++) {
                allPointLights.get(i).setUniformData(shaderProgram, i);
            }
            for (int i = 0; i < allSpotLights.size(); i++) {
                allSpotLights.get(i).setUniformData(shaderProgram, i);
            }
            ShaderProgram.unbind();
        }
    }

    public void createShadowRenderers(RenderComponent[] renderablesThrowingShadows) {
        for (Light light : allLights) {
            light.initializeShadowRenderer(renderablesThrowingShadows);
        }
    }

    public void renderShadowMaps() {
        for (Light light : allLights) {
            GL33.glCullFace(GL33.GL_FRONT);
            light.shadowRenderer.update();
            light.shadowRenderer.render(RenderInto.DEPTH_ONLY);
            GL33.glCullFace(GL33.GL_BACK);
        }
    }

}
