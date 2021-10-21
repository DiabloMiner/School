package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.render.textures.CubeMap;
import com.diablominer.opengl.render.textures.TwoDimensionalTexture;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

public class StencilTestRenderingEngineUnit extends RenderingEngineUnit {

    private DirectionalLight dirLight;
    private PointLight pointLight;
    private SpotLight spotLight;
    protected TwoDimensionalTexture brdfLookUpTexture;

    public StencilTestRenderingEngineUnit(ShaderProgram shaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight, TwoDimensionalTexture brdfLookUpTexture) {
        super(shaderProgram);
        this.dirLight = dirLight;
        this.pointLight = pointLight;
        this.spotLight = spotLight;
        this.brdfLookUpTexture = brdfLookUpTexture;
    }

    @Override
    public void updateRenderState(Camera camera, ShaderProgram shaderProgram) {
        shaderProgram.setUniformVec3F("viewPos", camera.position);

        shaderProgram.setUniformVec3F("dirLight.direction", dirLight.getDirection());
        shaderProgram.setUniformVec3F("dirLight.color", dirLight.getColor());

        shaderProgram.setUniformVec3F("pointLight.position", pointLight.getPosition());
        shaderProgram.setUniformVec3F("pointLight.color", pointLight.getColor());

        shaderProgram.setUniformVec3F("spotLight.position", camera.position);
        shaderProgram.setUniformVec3F("spotLight.direction", camera.front);
        shaderProgram.setUniformVec3F("spotLight.color", spotLight.getColor());


        shaderProgram.setUniformMat4F("model", new Matrix4f().identity());

        brdfLookUpTexture.bind();
        shaderProgram.setUniform1I("brdfLUT", brdfLookUpTexture.index);
    }

    public void setPointLight(PointLight pointLight) {
        this.pointLight = pointLight;
    }

    @Override
    public void render() {
        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
        renderAllRenderables();
        GL33.glStencilFunc(GL33.GL_ALWAYS, 0, 0xFF);
    }

}
