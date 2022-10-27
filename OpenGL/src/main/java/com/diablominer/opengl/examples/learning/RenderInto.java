package com.diablominer.opengl.examples.learning;

public enum RenderInto {
    COLOR_DEPTH_STENCIL(true, true, true),
    COLOR_DEPTH(true, true, false),
    COLOR_STENCIL(true, false, true),
    DEPTH_STENCIL(false, true, true),
    COLOR_ONLY(true, false, false),
    DEPTH_ONLY(false, true, false),
    STENCIL_ONLY(false, false, true);

    public boolean intoColor, intoDepth, intoStencil;

    RenderInto(boolean intoColor, boolean intoDepth, boolean intoStencil) {
        this.intoColor = intoColor;
        this.intoDepth = intoDepth;
        this.intoStencil = intoStencil;
    }
}

