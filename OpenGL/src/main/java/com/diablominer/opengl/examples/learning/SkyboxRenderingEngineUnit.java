package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class SkyboxRenderingEngineUnit extends RenderingEngineUnit {

    public static ShaderProgram shaderProgram;
    private final Skybox skybox;

    public SkyboxRenderingEngineUnit(Skybox skybox) {
        super(getSkyboxShaderProgram(), new Renderable[] {Skybox.model});
        this.skybox = skybox;
    }

    @Override
    public void update() {
        skybox.bindTexture();
        shaderProgram.setUniform1I("skybox", skybox.getTextureIndex());
    }

    @Override
    public void update(ShaderProgram shaderProgram) {
        skybox.bindTexture();
        shaderProgram.setUniform1I("skybox", skybox.getTextureIndex());
    }

    @Override
    public void render() {
        GL33.glDepthFunc(GL33.GL_LEQUAL);
        renderAllRenderables();
        GL33.glDepthFunc(GL33.GL_LESS);
        skybox.unbindTexture();
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        GL33.glDepthFunc(GL33.GL_LEQUAL);
        renderAllRenderables(shaderProgram);
        GL33.glDepthFunc(GL33.GL_LESS);
        skybox.unbindTexture();
    }

    @Override
    public void destroy() {
        skybox.destroy();
        destroyShaderProgram();
        destroyAllRenderables();
    }

    public static ShaderProgram getSkyboxShaderProgram() {
        if (shaderProgram == null) {
            try {
                shaderProgram = new ShaderProgram("L6_SkyboxVS", "L6_SkyboxFS");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return shaderProgram;
    }

}
