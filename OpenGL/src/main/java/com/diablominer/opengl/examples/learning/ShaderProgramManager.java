package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public class ShaderProgramManager implements Manager {

    public static List<ShaderProgram> staticShaderPrograms = new ArrayList<>();

    public List<ShaderProgram> allShaderPrograms;
    public List<ShaderProgram> allShaderProgramsUsingShadows;
    public List<ShaderProgram> allShaderProgramsUsingSkyboxes;

    public ShaderProgramManager() throws Exception {
        allShaderPrograms = new ArrayList<>();
        allShaderProgramsUsingShadows = new ArrayList<>();
        allShaderProgramsUsingSkyboxes = new ArrayList<>();

        addStaticShaderProgram(DirectionalLight.getShadowShader(), this, true, false);
        addStaticShaderProgram(PointLight.getShadowShader(), this, true, false);
        addStaticShaderProgram(SpotLight.getShadowShader(), this, true, false);
        addStaticShaderProgram(BlurRenderer.getBlurShaderProgram(), this, false, false);
        addStaticShaderProgram(SkyboxRenderingEngineUnit.getSkyboxShaderProgram(), this, false, false);
    }

    // UsesSkybox means in this context that a shaderprogram uses a skybox's irradiance map
    public ShaderProgram createShaderProgram(boolean usesShadows, boolean usesSkyboxes) throws Exception {
        ShaderProgram shaderProgram = new ShaderProgram();
        allShaderPrograms.add(shaderProgram);
        if (usesShadows) {  allShaderProgramsUsingShadows.add(shaderProgram);}
        if (usesSkyboxes) {  allShaderProgramsUsingSkyboxes.add(shaderProgram);}
        return shaderProgram;
    }

    public ShaderProgram createShaderProgram(String vertexShaderPath, String fragmentShaderPath, boolean usesShadows, boolean usesSkyboxes) throws Exception {
        ShaderProgram shaderProgram = new ShaderProgram(vertexShaderPath, fragmentShaderPath);
        allShaderPrograms.add(shaderProgram);
        if (usesShadows) {  allShaderProgramsUsingShadows.add(shaderProgram);}
        if (usesSkyboxes) {  allShaderProgramsUsingSkyboxes.add(shaderProgram);}
        return shaderProgram;
    }

    public ShaderProgram createShaderProgram(String vertexShaderPath, String geometryShaderPath, String fragmentShaderPath, boolean usesShadows, boolean usesSkyboxes) throws Exception {
        ShaderProgram shaderProgram = new ShaderProgram(vertexShaderPath, geometryShaderPath, fragmentShaderPath);
        allShaderPrograms.add(shaderProgram);
        if (usesShadows) {  allShaderProgramsUsingShadows.add(shaderProgram);}
        if (usesSkyboxes) {  allShaderProgramsUsingSkyboxes.add(shaderProgram);}
        return shaderProgram;
    }

    public void addShaderProgram(ShaderProgram shaderProgram, boolean usesShadows, boolean usesSkyboxes) {
        allShaderPrograms.add(shaderProgram);
        if (usesShadows) {  allShaderProgramsUsingShadows.add(shaderProgram);}
        if (usesSkyboxes) {  allShaderProgramsUsingSkyboxes.add(shaderProgram);}
    }

    public void addShaderPrograms(List<ShaderProgram> shaderPrograms, List<Boolean> usesShadows, List<Boolean> usesSkyboxes) {
        for (int i = 0; i < shaderPrograms.size(); i++) {
            addShaderProgram(shaderPrograms.get(i), usesShadows.get(i), usesSkyboxes.get(i));
        }
    }

    public void destroyAllShaderPrograms() {
        for (ShaderProgram shaderProgram : allShaderPrograms) {
            shaderProgram.destroy();
        }
    }

    public static void addStaticShaderProgram(ShaderProgram shaderProgram, ShaderProgramManager manager, boolean usesShadows, boolean usesSkyboxes) {
        if (!staticShaderPrograms.contains(shaderProgram)) {  staticShaderPrograms.add(shaderProgram);}
        if (usesShadows) {  manager.allShaderProgramsUsingShadows.add(shaderProgram);}
        if (usesSkyboxes) {  manager.allShaderProgramsUsingSkyboxes.add(shaderProgram);}
    }

    public static void destroyAllStaticShaderPrograms() {
        for (ShaderProgram shaderProgram : staticShaderPrograms) {
            shaderProgram.destroy();
        }
    }

}
