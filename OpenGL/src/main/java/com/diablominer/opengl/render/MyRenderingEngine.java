package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Window;
import org.lwjgl.opengl.GL33;

public class MyRenderingEngine extends RenderingEngine {

    @Override
    public void render(Window window) {
        GL33.glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glStencilOp(GL33.GL_KEEP,GL33.GL_KEEP, GL33.GL_REPLACE);

        this.renderAllEngineUnits();

        // TODO: Undo commented code here
        // GLFW.glfwSwapBuffers(window.getWindow());
    }

    @Override
    public void end() {
        this.cleanUpAllEngineUnits();
    }
}
