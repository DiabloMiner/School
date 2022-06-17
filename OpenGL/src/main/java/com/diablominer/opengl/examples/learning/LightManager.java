package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
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

    public DirectionalLight createDirectionalLight(Vector3f direction, Vector3f color, int shadowSize) {
        DirectionalLight dirLight = new DirectionalLight(direction, color, shadowSize);
        allLights.add(dirLight);
        allDirectionalLights.add(dirLight);
        return dirLight;
    }

    public PointLight createPointLight(Vector3f position, Vector3f color, int shadowSize) {
        PointLight pointLight = new PointLight(position, color, shadowSize);
        allLights.add(pointLight);
        allPointLights.add(pointLight);
        return pointLight;
    }

    public SpotLight createPointLight(Vector3f position, Vector3f direction, Vector3f color, int shadowSize) {
        SpotLight spotLight = new SpotLight(position, direction, color, shadowSize);
        allLights.add(spotLight);
        allSpotLights.add(spotLight);
        return spotLight;
    }

    public void addDirectionalLight(DirectionalLight dirLight) {
        allLights.add(dirLight);
        allDirectionalLights.add(dirLight);
    }

    public void addPointLight(PointLight pointLight) {
        allLights.add(pointLight);
        allPointLights.add(pointLight);
    }

    public void addSpotLight(SpotLight spotLight) {
        allLights.add(spotLight);
        allSpotLights.add(spotLight);
    }

    public void addRenderablePointLight(RenderablePointLight light) {
        allLights.add(light.light);
        allRenderableLights.add(light);
        allPointLights.add((PointLight) light.light);
    }

    public void setLightUniforms(Collection<ShaderProgram> shaderProgramsUsingShadows) {
        for (ShaderProgram shaderProgram : shaderProgramsUsingShadows) {
            for (int i = 0; i < allDirectionalLights.size(); i++) {
                allDirectionalLights.get(i).setUniformData(shaderProgram, i);
            }
            for (int i = 0; i < allPointLights.size(); i++) {
                allPointLights.get(i).setUniformData(shaderProgram, i);
            }
            for (int i = 0; i < allSpotLights.size(); i++) {
                allSpotLights.get(i).setUniformData(shaderProgram, i);
            }
        }
    }

    public void unbindAllShadowTextures() {
        for (Light light : allLights) {
            light.unbindShadowTextures();
        }
    }

    public void createShadowRenderers(Renderable[] renderablesThrowingShadows) {
        for (Light light : allLights) {
            light.initializeShadowRenderer(renderablesThrowingShadows);
        }
    }

    public void renderShadowMaps() {
        for (Light light : allLights) {
            GL33.glCullFace(GL33.GL_FRONT);
            light.getShadowRenderer().update();
            light.getShadowRenderer().render();
            GL33.glCullFace(GL33.GL_BACK);
        }
    }

}
