package com.diablominer.opengl.examples.learning;

import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface Light {

    List<Light> allLights = new ArrayList<>();

    List<Vector4f> getData();

    Vector3f getColor();

    static Vector4f[] getDataOfAllLights() {
        sortAllLights();

        List<Vector4f> dataList = new ArrayList<>();
        for (Light light : allLights) {
               dataList.addAll(light.getData());
        }
        Vector4f[] dataArray = new Vector4f[dataList.size()];
        dataList.toArray(dataArray);
        return dataArray;
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
                    return DirectionalLight.shaderIndex;
                } else if (o instanceof PointLight) {
                    return PointLight.shaderIndex;
                } else {
                    return SpotLight.shaderIndex;
                }
            }
        });
    }



}
