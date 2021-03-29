package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import com.diablominer.opengl.render.textures.CubeMap;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL33;

import java.util.Collections;

public class TransparencyRenderingEngineUnit extends RenderingEngineUnit {

    private DirectionalLight dirLight;
    private PointLight pointLight;
    private SpotLight spotLight;
    private CubeMap convolutedCubeMap;

    public TransparencyRenderingEngineUnit(ShaderProgram shaderProgram, ShaderProgram alternativeShaderProgram, DirectionalLight dirLight, PointLight pointLight, SpotLight spotLight, CubeMap convolutedCubeMap) {
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


        this.getShaderProgram().setUniformMat4F("model", new Matrix4f().identity());

        this.getRenderables().sort((o1, o2) -> {
            float distance1 = Math.abs(o1.getPosition().distance(camera.position));
            float distance2 = Math.abs(o2.getPosition().distance(camera.position));
            return Float.compare(distance1, distance2);
        });
        Collections.reverse(this.getRenderables());
    }

    @Override
    public void render() {
        convolutedCubeMap.bind();
        shaderProgram.setUniform1I("irradianceMap", convolutedCubeMap.index);
        GL33.glDisable(GL33.GL_CULL_FACE);
        renderAllRenderables();
        GL33.glEnable(GL33.GL_CULL_FACE);
        convolutedCubeMap.unbind();
    }

    @Override
    public void renderAlternative() {
        convolutedCubeMap.bind();
        shaderProgram.setUniform1I("irradianceMap", convolutedCubeMap.index);
        GL33.glDisable(GL33.GL_CULL_FACE);
        renderAllRenderablesAlternative();
        GL33.glEnable(GL33.GL_CULL_FACE);
        convolutedCubeMap.unbind();
    }
}
