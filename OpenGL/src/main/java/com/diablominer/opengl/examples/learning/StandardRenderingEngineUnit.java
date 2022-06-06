package com.diablominer.opengl.examples.learning;

import org.lwjgl.opengl.GL33;

public class StandardRenderingEngineUnit extends RenderingEngineUnit {

    public StandardRenderingEngineUnit(ShaderProgram shaderProgram) {
        super(shaderProgram);
    }

    public StandardRenderingEngineUnit(ShaderProgram shaderProgram, Renderable[] renderables) {
        super(shaderProgram, renderables);
    }

    @Override
    public void update() {
        update(this.shaderProgram);
        // TODO: Introduce I/O Engine or something equivalent ; Replace clear function in SingleFramebufferRenderer with CubeMap ; Introduce array with a maxsize in shaders so dynamic arrays are possible
        // TODO: Make IBL, Shadowcasting, Normal mapping available ; Review assimp code ; (Improve old texture code with new code)
        // TODO: Create new textures explicitly used for assimp, multisampling ; Maybe also an enum for tex parameters
        // Added functionality:
        // Implemented directional shadow casting for directional lights and spot lights ; Added dynamic yaw and pitch calculation ; Implemented omnidirectional shadow casting ;
        // Removed standard point light cube from shadow maps ; Introduced abstract Engine class ; Abstract things that are currently solved with public static lists into managers so multiple can be had
    }

    @Override
    public void update(ShaderProgram shaderProgram) {}

    @Override
    public void render() {
        render(this.shaderProgram);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_STENCIL_TEST);

        renderAllRenderables(shaderProgram);

        GL33.glDisable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_STENCIL_TEST);
    }

    @Override
    public void destroy() {
        destroyAllRenderables();
        destroyShaderProgram();
    }
}
