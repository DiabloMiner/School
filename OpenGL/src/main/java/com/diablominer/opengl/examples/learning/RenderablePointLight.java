package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;

public class RenderablePointLight extends RenderableLight {

    public static AssimpModel model = new AssimpModel("./src/main/java/com/diablominer/opengl/examples/models/cube/cube.obj", new Vector3f(0.0f));

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
    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        model.draw(shaderProgram, new Matrix4f().identity().translate(pointLight.position).scale(0.2f), flags);
    }

    @Override
    public void destroy() {
        RenderablePointLight.destroyModel();
    }

    public static void destroyModel() {
        if (!(model.meshes.isEmpty())) {
            model.destroy();
        }
    }

}
