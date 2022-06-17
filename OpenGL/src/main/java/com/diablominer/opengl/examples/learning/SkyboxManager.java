package com.diablominer.opengl.examples.learning;

import java.util.ArrayList;
import java.util.List;

public class SkyboxManager implements Manager {

    public List<Skybox> skyboxes;

    public SkyboxManager() {
        skyboxes = new ArrayList<>();
    }

    public Skybox createSkybox(String directory, String fileType, boolean flipImage) throws Exception {
        Skybox skybox = new Skybox(directory, fileType, flipImage);
        skyboxes.add(skybox);
        return skybox;
    }

    public Skybox createSkybox(String filePath, int size, boolean flipImage) throws Exception {
        Skybox skybox = new Skybox(filePath, size, flipImage);
        skyboxes.add(skybox);
        return skybox;
    }

    public void setSkyboxUniforms(List<ShaderProgram> shaderPrograms) {
        for (ShaderProgram shaderProgram : shaderPrograms) {
            for (int i = 0; i < skyboxes.size(); i++) {
                skyboxes.get(i).bindConvolutedTexture();
                shaderProgram.setUniform1I("irradianceMap" + i, skyboxes.get(i).getConvolutedTextureIndex());
                skyboxes.get(i).bindPrefilteredTexture();
                shaderProgram.setUniform1I("prefilteredMap" + i, skyboxes.get(i).getPrefilteredTextureIndex());
                skyboxes.get(i).bindBrdfLookUpTextureTexture();
                shaderProgram.setUniform1I("brdfLUT" + i, skyboxes.get(i).getBrdfLookUpTextureIndex());
            }
        }
    }

    public void unbindSkyboxTextures() {
        for (Skybox skybox : skyboxes) {
            skybox.unbindConvolutedTexture();
            skybox.unbindPrefilteredTexture();
            skybox.unbindBrdfLookUpTextureTexture();
        }
    }

    public void destroyAllSkyboxes() {
        for (Skybox skybox : skyboxes) {
            skybox.destroy();
        }
    }

}
