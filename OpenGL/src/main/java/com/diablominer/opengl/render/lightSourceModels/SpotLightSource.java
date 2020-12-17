package com.diablominer.opengl.render.lightSourceModels;

import com.diablominer.opengl.render.Model;
import com.diablominer.opengl.render.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class SpotLightSource {

    /*private Vector3f position = new Vector3f();
    private Vector3f color = new Vector3f();

    public static List<SpotLightSource> createMultipleSpotLights(String[] paths) {
        List<SpotLightSource> result = new ArrayList<>();
        for (String path : paths) {
            result.add(new SpotLightSource(path));
        }
        return result;
    }

    public SpotLightSource(String path) {
        super(path);
    }

    public void setUniforms(Vector3f position, Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular, float constant, float linear, float quadratic, float cutOffAngle, float outerCutOffAngle, ShaderProgram shader) {
        // The cutOff and outerCutOff angles have to be given in degrees
        this.position = position;
        this.color = diffuse;
        shader.setUniformVec3F("spotLight.position", position);
        shader.setUniformVec3F("spotLight.direction", direction);
        shader.setUniformVec3F("spotLight.ambient", ambient);
        shader.setUniformVec3F("spotLight.diffuse", diffuse);
        shader.setUniformVec3F("spotLight.specular", specular);
        shader.setUniform1F("spotLight.constant", constant);
        shader.setUniform1F("spotLight.linear", linear);
        shader.setUniform1F("spotLight.quadratic", quadratic);
        shader.setUniform1F("spotLight.cutOff", (float) Math.cos(Math.toRadians(cutOffAngle)));
        shader.setUniform1F("spotLight.outerCutOff", (float) Math.cos(Math.toRadians(outerCutOffAngle)));
    }

    public void setUniforms(Vector3f position, Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular, float constant, float linear, float quadratic, float cutOffAngle, float outerCutOffAngle, int i, ShaderProgram shader) {
        // The cutOff and outerCutOff angles have to be given in degrees
        this.position = position;
        this.color = diffuse;
        shader.setUniformVec3F("spotLights[" + i + "].position", position);
        shader.setUniformVec3F("spotLight[" + i + "].direction", direction);
        shader.setUniformVec3F("spotLights[" + i + "].ambient", ambient);
        shader.setUniformVec3F("spotLights[" + i + "].diffuse", diffuse);
        shader.setUniformVec3F("spotLights[" + i + "].specular", specular);
        shader.setUniform1F("spotLights[" + i + "].constant", constant);
        shader.setUniform1F("spotLights[" + i + "].linear", linear);
        shader.setUniform1F("spotLights[" + i + "].quadratic", quadratic);
        shader.setUniform1F("spotLights[" + i + "].cutOff", (float) Math.cos(Math.toRadians(cutOffAngle)));
        shader.setUniform1F("spotLights[" + i + "].outerCutOff", (float) Math.cos(Math.toRadians(outerCutOffAngle)));
    }

    @Override
    public void draw(ShaderProgram lightSourceShader) {
        lightSourceShader.setUniformVec3F("color", color);
        lightSourceShader.setUniformMat4F("model", new Matrix4f().identity().translate(position));
        super.draw(lightSourceShader);
    }*/
}
