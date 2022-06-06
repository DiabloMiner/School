package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class RenderablePointLight extends RenderableLight {

    public static Model pointLightModel = new Model("./src/main/java/com/diablominer/opengl/examples/models/cube/cube.obj", new Vector3f(0.0f), false);

    public PointLight pointLight;

    public RenderablePointLight(PointLight pointLight) {
        super(pointLight);
        this.pointLight = (PointLight) light;
    }

    public RenderablePointLight(Vector3f position, Vector3f color, int shadowSize) {
        super(new PointLight(position, color, shadowSize));
        this.pointLight = (PointLight) light;
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        pointLightModel.draw(shaderProgram, new Matrix4f().identity().translate(pointLight.position).scale(0.2f));
    }

    @Override
    public void destroy() {
        RenderablePointLight.destroyModel();
    }

    @Override
    void addToSpecificLight(LightManager lightManager) {
        lightManager.allPointLights.add(pointLight);
    }

    public static void destroyModel() {
        if (!(pointLightModel.meshes.isEmpty() && pointLightModel.loadedTexture2DS.isEmpty())) {
            pointLightModel.destroy();
        }
    }

}
