package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.render.textures.CubeMap;
import org.joml.Matrix4f;

public class MyRenderingEngineUnit extends RenderingEngineUnit {

    protected DirectionalLight dirLight;
    protected PointLight pointLight;
    protected SpotLight spotLight;
    protected CubeMap convolutedCubeMap;

    public MyRenderingEngineUnit(ShaderProgram shaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight, CubeMap convolutedCubeMap) {
        super(shaderProgram);
        this.dirLight = dirLight;
        this.pointLight = pointLight;
        this.spotLight = spotLight;
        this.convolutedCubeMap = convolutedCubeMap;
    }

    public MyRenderingEngineUnit(ShaderProgram shaderProgram, ShaderProgram alternativeShaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight, CubeMap convolutedCubeMap) {
        super(shaderProgram, alternativeShaderProgram);
        this.dirLight = dirLight;
        this.pointLight = pointLight;
        this.spotLight = spotLight;
        this.convolutedCubeMap = convolutedCubeMap;
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
    }

    @Override
    public void render() {
        convolutedCubeMap.bind();
        shaderProgram.setUniform1I("irradianceMap", convolutedCubeMap.index);
        renderAllRenderables();
        convolutedCubeMap.unbind();
    }

    @Override
    public void renderAlternative() {
        convolutedCubeMap.bind();
        shaderProgram.setUniform1I("irradianceMap", convolutedCubeMap.index);
        renderAllRenderablesAlternative();
        convolutedCubeMap.unbind();
    }
}
