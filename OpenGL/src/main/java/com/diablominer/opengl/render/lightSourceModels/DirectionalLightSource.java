package com.diablominer.opengl.render.lightSourceModels;

import com.diablominer.opengl.render.ShaderProgram;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class DirectionalLightSource {

    public DirectionalLightSource() {}

    public static List<DirectionalLightSource> createMultipleDirectionalLights(int amountOfLights) {
        List<DirectionalLightSource> result = new ArrayList<>();
        for (int i = 0; i < amountOfLights; i++) {
            result.add(new DirectionalLightSource());
        }
        return result;
    }

    public void setUniforms(Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular, ShaderProgram shader) {
        shader.setUniformVec3F("dirLight.direction", direction);
        shader.setUniformVec3F("dirLight.ambient", ambient);
        shader.setUniformVec3F("dirLight.diffuse", diffuse);
        shader.setUniformVec3F("dirLight.specular", specular);
    }

    public void setUniforms(Vector3f direction, Vector3f ambient, Vector3f diffuse, Vector3f specular, int i, ShaderProgram shader) {
        shader.setUniformVec3F("dirLights[" + i + "].direction", direction);
        shader.setUniformVec3F("dirLights[" + i + "].ambient", ambient);
        shader.setUniformVec3F("dirLights[" + i + "]diffuse", diffuse);
        shader.setUniformVec3F("dirLights[" + i + "]specular", specular);
    }
}
