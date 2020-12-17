package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Window;
import com.diablominer.opengl.render.lightsources.DirectionalLight;
import com.diablominer.opengl.render.lightsources.PointLight;
import com.diablominer.opengl.render.lightsources.SpotLight;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

public class MyEngine extends Engine {

    public static final DirectionalLight directionLight = new DirectionalLight(new Vector3f(1.0f, 0.0f, 1.0f), new Vector3f(0.1f, 0.1f, 0.2f), new Vector3f(0.3f, 0.3f, 0.3f),  new Vector3f(0.8f, 0.8f, 0.8f));
    public static final PointLight pointLight = new PointLight(new Vector3f(5.0f, 2.0f, 3.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.35f, 0.7f);
    public static final SpotLight spotLight = new SpotLight(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.2f, 0.2f, 0.2f), new Vector3f(0.8f, 0.8f, 0.8f),  new Vector3f(1.0f, 1.0f, 1.0f), 1.0f, 0.35f, 0.7f, (float) Math.cos(Math.toRadians(17.5f)), (float) Math.cos(Math.toRadians(19.5f)));

    @Override
    public void render(Window window) {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        this.renderAllEngineUnits();

        window.swapBuffers();
    }

    @Override
    public void end() {
        this.cleanUpAllEngineUnits();
    }
}
