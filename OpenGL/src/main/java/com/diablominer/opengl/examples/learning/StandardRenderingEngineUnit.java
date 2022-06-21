package com.diablominer.opengl.examples.learning;

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
        // TODO: Fix visual bug with gold cubes and determine why model matrix doesnt always correspond to the position
        // TODO: Reconsider public static sets (also Texture lists) ; Rethink way EventSystem is handled (PingPongIterationEvents necessitate a RenderingEngine specific approach(Maybe compare eventthrower index))
        // TODO: Introduce I/O Engine or something equivalent ; See if specular highlights on cube are correct (Check texts for this problem)
        // TODO: Fully implement Normal mapping (more effective) ; Review assimp code
        // TODO: (Maybe define texture index in another way) ; Maybe also an enum for tex parameters (Create ones for every OpenGL purpose
        // TODO: Introduce array with a maxsize in shaders so dynamic arrays are possible ; Maybe change from objects extending classes to objects having components
        // Added functionality:
        // Implemented directional shadow casting for directional lights and spot lights ; Added dynamic yaw and pitch calculation ; Implemented omnidirectional shadow casting ;
        // Removed standard point light cube from shadow maps ; Introduced abstract Engine class ; Abstract things that are currently solved with public static lists into managers so multiple can be had
        // Made it possible to add skyboxes ; Replaced clear function in SingleFramebufferRenderer with CubeMap ; Wrote a skybox class ; Created textures only used for models ; Created a mesh and model super class
        // Implemented a list for static shaderprograms ; Implemented skybox manager ; Camera controls at beginning are right way around now ; Replaced the generic renderable light in LightManager with specific functions
        // Created new textures explicitly used for multisampling ; Rethought FramebufferTextures: Textures are now a filed of FramebufferTextures ; Shadows are now not distorted by resizing
    }

    @Override
    public void update(ShaderProgram shaderProgram) {}

    @Override
    public void render() {
        render(this.shaderProgram);
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        renderAllRenderables(shaderProgram);
    }

    @Override
    public void destroy() {
        destroyAllRenderables();
        destroyShaderProgram();
    }
}
