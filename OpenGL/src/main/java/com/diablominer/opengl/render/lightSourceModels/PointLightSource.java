package com.diablominer.opengl.render.lightSourceModels;

import com.diablominer.opengl.render.Model;
import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class PointLightSource extends Model {

    private Vector3f position = new Vector3f();
    private Vector3f color = new Vector3f();

    public static List<PointLightSource> createMultiplePointLights(String[] paths) {
        List<PointLightSource> result = new ArrayList<>();
        for (String path : paths) {
            result.add(new PointLightSource(path));
        }
        return result;
    }

    public PointLightSource(String path) {
        super(path);
    }

    public void setUniforms(Vector3f position, Vector3f ambient, Vector3f diffuse, Vector3f specular, float constant, float linear, float quadratic, ShaderProgram shader) {
        this.position = position;
        this.color = diffuse;
        shader.setUniformVec3F("pointLight.position", position);
        shader.setUniformVec3F("pointLight.ambient", ambient);
        shader.setUniformVec3F("pointLight.diffuse", diffuse);
        shader.setUniformVec3F("pointLight.specular", specular);
        shader.setUniform1F("pointLight.constant", constant);
        shader.setUniform1F("pointLight.linear", linear);
        shader.setUniform1F("pointLight.quadratic", quadratic);
    }

    public void setUniforms(Vector3f position, Vector3f ambient, Vector3f diffuse, Vector3f specular, float constant, float linear, float quadratic, int i, ShaderProgram shader) {
        this.position = position;
        this.color = Transforms.getSumOf2Vectors(ambient, diffuse);
        shader.setUniformVec3F("pointLights[" + i + "].position", position);
        shader.setUniformVec3F("pointLights[" + i + "].ambient", ambient);
        shader.setUniformVec3F("pointLights[" + i + "].diffuse", diffuse);
        shader.setUniformVec3F("pointLights[" + i + "].specular", specular);
        shader.setUniform1F("pointLights[" + i + "].constant", constant);
        shader.setUniform1F("pointLights[" + i + "].linear", linear);
        shader.setUniform1F("pointLights[" + i + "].quadratic", quadratic);
    }

    @Override
    public void draw(ShaderProgram lightSourceShader) {
        lightSourceShader.setUniformVec3F("color", color);
        lightSourceShader.setUniformMat4F("model", new Matrix4f().identity().translate(position));
        super.draw(lightSourceShader);
    }
}
