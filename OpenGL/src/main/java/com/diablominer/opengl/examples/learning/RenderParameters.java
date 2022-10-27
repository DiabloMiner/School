package com.diablominer.opengl.examples.learning;

public enum RenderParameters {
    COLOR_DEPTH_STENCIL_ENABLED(true, true, true),
    COLOR_DEPTH_ENABLED(true, true, false),
    COLOR_STENCIL_ENABLED(true, false, true),
    DEPTH_STENCIL_ENABLED(false, true, true),
    COLOR_ENABLED(true, false, false),
    DEPTH_ENABLED(false, true, false),
    STENCIL_ENABLED(false, false, true);

    public boolean colorEnabled, depthEnabled, stencilEnabled;

    RenderParameters(boolean colorEnabled, boolean depthEnabled, boolean stencilEnabled) {
        this.colorEnabled = colorEnabled;
        this.depthEnabled = depthEnabled;
        this.stencilEnabled = stencilEnabled;
    }
}
