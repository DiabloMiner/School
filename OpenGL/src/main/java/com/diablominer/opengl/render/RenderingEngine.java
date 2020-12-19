package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderingEngine {

    private List<RenderingEngineUnit> renderingEngineUnits = new ArrayList<>();

    public void addNewEngineUnit(RenderingEngineUnit renderingEngineUnit) {
        renderingEngineUnits.add(renderingEngineUnit);
    }

    public List<RenderingEngineUnit> getEngineUnits() {
        return renderingEngineUnits;
    }

    public void renderAllEngineUnits() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.render();
        }
    }

    public void cleanUpAllEngineUnits() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.cleanUpAllRenderables();
        }
    }

    public void updateAllEngineUnits(Camera camera, Window window) {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.updateRenderState(camera, window);
        }
    }

    public abstract void render(Window window);

    public abstract void end();

}
