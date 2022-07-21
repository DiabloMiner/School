package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public class SkyboxManager implements Manager {

    public List<Skybox> skyboxes;

    public SkyboxManager() {
        skyboxes = new ArrayList<>();
    }

    public Skybox addSkybox(Skybox skybox) {
        skyboxes.add(skybox);
        return skybox;
    }

    public void setSkyboxUniforms(List<ShaderProgram> shaderPrograms) {
        for (ShaderProgram shaderProgram : shaderPrograms) {
            for (int i = 0; i < skyboxes.size(); i++) {
                shaderProgram.bind();
                shaderProgram.setUniform1IBindless("irradianceMap" + i, skyboxes.get(i).getConvolutedTextureIndex());
                shaderProgram.setUniform1IBindless("prefilteredMap" + i, skyboxes.get(i).getPrefilteredTextureIndex());
                shaderProgram.setUniform1IBindless("brdfLUT" + i, Skybox.getBrdfLookUpTextureIndex());
                ShaderProgram.unbind();
            }
        }
    }

    public void destroyAllSkyboxes() {
        for (Skybox skybox : skyboxes) {
            skybox.destroy();
        }
    }

}
