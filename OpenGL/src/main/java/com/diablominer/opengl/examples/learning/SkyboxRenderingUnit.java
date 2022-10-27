package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

import java.util.Map;

public class SkyboxRenderingUnit extends RenderingUnit {

    public static ShaderProgram shaderProgram;
    private final Skybox skybox;

    public SkyboxRenderingUnit(Skybox skybox) {
        super(getSkyboxShaderProgram(), new RenderComponent[] {Skybox.model});
        this.skybox = skybox;
        skybox.normalTexture.bind();
    }

    @Override
    public void update() {
        this.update(this.getShaderProgram());
    }

    @Override
    public void update(ShaderProgram shaderProgram) {
        if (!skybox.normalTexture.isBound()) {
            skybox.normalTexture.bind();
        }
        shaderProgram.setUniform1I("skybox", skybox.normalTexture.getIndex());
    }

    @Override
    public void render(Map.Entry<RenderInto, RenderParameters> flags) {
        this.render(shaderProgram, flags);
    }

    @Override
    public void render(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        if (flags.getValue().depthEnabled) {
            GL33.glDepthFunc(GL33.GL_LEQUAL);
        }
        renderRenderables(shaderProgram, flags);
        if (flags.getValue().depthEnabled) {
            GL33.glDepthFunc(GL33.GL_LESS);
        }
    }

    @Override
    public void destroy() {
        skybox.destroy();
        destroyShaderProgram();
        destroyRenderables();
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
