package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderingEngine {

    protected List<RenderingEngineUnit> renderingEngineUnits = new ArrayList<>();

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

    public void destroyAllEngineUnits() {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.destroyAllRenderables();
            renderingEngineUnit.destroyShaderProgram();
        }
    }

    public void updateAllEngineUnits(Camera camera) {
        for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
            renderingEngineUnit.updateRenderState(camera);
        }
    }

    public abstract void render();

    public abstract void update();

    public abstract void destroy();

}
