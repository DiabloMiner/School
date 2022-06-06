package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public class ShaderProgramManager {

    public List<ShaderProgram> allShaderPrograms;
    public List<ShaderProgram> allShaderProgramsUsingShadows;

    public ShaderProgramManager() throws Exception {
        allShaderPrograms = new ArrayList<>();
        allShaderProgramsUsingShadows = new ArrayList<>();
        addShaderProgram(DirectionalLight.getShadowShader());
        addShaderProgram(PointLight.getShadowShader());
        addShaderProgram(SpotLight.getShadowShader());
        addShaderProgram(BlurRenderer.getBlurShaderProgram());
    }

    public void addShaderProgram(ShaderProgram shaderProgram) {
        allShaderPrograms.add(shaderProgram);
        if (shaderProgram.usesShadows()) {  allShaderProgramsUsingShadows.add(shaderProgram);}
    }

    public void addShaderPrograms(List<ShaderProgram> shaderPrograms) {
        for (ShaderProgram shaderProgram : shaderPrograms) {
            addShaderProgram(shaderProgram);
        }
    }

    public void destroyAllShaderPrograms() {
        for (ShaderProgram shaderProgram : allShaderPrograms) {
            shaderProgram.destroy();
        }
    }

}
