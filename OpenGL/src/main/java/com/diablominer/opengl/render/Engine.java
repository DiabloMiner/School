package com.diablominer.opengl.render;

import com.diablominer.opengl.io.Camera;
import com.diablominer.opengl.io.Window;

import java.util.ArrayList;
import java.util.List;

public abstract class Engine {

    private List<EngineUnit> engineUnits = new ArrayList<>();

    public void addNewEngineUnit(EngineUnit engineUnit) {
        engineUnits.add(engineUnit);
    }

    public List<EngineUnit> getEngineUnits() {
        return engineUnits;
    }

    public void renderAllEngineUnits() {
        for (EngineUnit engineUnit : engineUnits) {
            engineUnit.render();
        }
    }

    public void cleanUpAllEngineUnits() {
        for (EngineUnit engineUnit : engineUnits) {
            engineUnit.cleanUpAllRenderables();
        }
    }

    public void updateAllEngineUnits(Camera camera, Window window) {
        for (EngineUnit engineUnit : engineUnits) {
            engineUnit.updateRenderState(camera, window);
        }
    }

    public abstract void render(Window window);

    public abstract void end();

}
