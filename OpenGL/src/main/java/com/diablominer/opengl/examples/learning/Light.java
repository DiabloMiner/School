package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface Light {

    List<Light> allLights = new ArrayList<>();

    Vector3f getColor();

    void setUniformData(ShaderProgram shaderProgram, int index);

    void unbindShadowTextures();

    void initializeShadowRenderer(Renderable[] renderables);

    Renderer getShadowRenderer();

    Matrix4f[] getLightSpaceMatrices();

    static void setUniformDataForAllLights() {
        sortAllLights();

        for (ShaderProgram shaderProgram : ShaderProgram.shaderProgramsUsingShadows) {
            for (int i = 0; i < allLights.size(); i++) {
                allLights.get(i).setUniformData(shaderProgram, i);
            }
        }
    }

    /**
     * This method ensures that the allLights list is composed in the order expected by the shader. The expected order goes like this: DirectionalLights, PointLights, SpotLights.
     */
    static void sortAllLights() {
        allLights.sort(new Comparator<>() {
            @Override
            public int compare(Light o1, Light o2) {
                int i1 = determineShaderIndex(o1);
                int i2 = determineShaderIndex(o2);
                return Integer.compare(i1, i2);
            }

            private int determineShaderIndex(Light o) {
                if (o instanceof DirectionalLight) {
                    return DirectionalLight.sortingIndex;
                } else if (o instanceof PointLight) {
                    return PointLight.sortingIndex;
                } else {
                    return SpotLight.sortingIndex;
                }
            }
        });
    }

    static void unbindAllShadowTextures() {
        for (Light light : allLights) {
            light.unbindShadowTextures();
        }
    }

    static void createShadowRenderers() {
        for (Light light : allLights) {
            light.initializeShadowRenderer(Renderable.renderablesThrowingShadows.toArray(new Renderable[0]));
        }
    }

    static void renderShadowMaps() {
        for (Light light : allLights) {
            GL33.glCullFace(GL33.GL_FRONT);
            light.getShadowRenderer().update();
            light.getShadowRenderer().render();
            GL33.glCullFace(GL33.GL_BACK);
        }
    }

}
