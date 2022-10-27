package com.diablominer.opengl.examples.learning;

import java.util.Map;

public interface Renderable {

    void draw(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags);

    void destroy();

}
