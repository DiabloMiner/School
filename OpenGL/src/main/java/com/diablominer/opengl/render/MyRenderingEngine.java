package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Window;
import org.lwjgl.opengl.GL33;

public class MyRenderingEngine extends RenderingEngine {

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