package com.diablominer.opengl.examples.learning;

public class GaussianBlurRenderer extends FramebuffersRenderer {

    private int iterations;

    public GaussianBlurRenderer(Framebuffer[] framebuffers, RenderingEngineUnit[] renderingEngineUnits, int iterations) {
        super(framebuffers, renderingEngineUnits);
        this.iterations = iterations;
    }

    @Override
    public void update() {
        for (RenderingEngineUnit renderingEngineUnit : this.renderingEngineUnits) {
            renderingEngineUnit.updateRenderState();
        }
    }

    @Override
    public void update(ShaderProgram shaderProgram) {
        for (RenderingEngineUnit renderingEngineUnit : this.renderingEngineUnits) {
            renderingEngineUnit.updateRenderState();
        }
    }

    @Override
    public void render() {
        int fbSize = framebuffers.size();
        for (int i = 0; i < iterations; i++) {
            int sizeFactor = (int) Math.ceil((double) i / (double) fbSize);
            int fbIndex = i - sizeFactor * fbSize;
            Framebuffer currentFramebuffer = framebuffers.get(fbIndex);

            currentFramebuffer.bind();
            for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
                renderingEngineUnit.shaderProgram.setUniform1I("fbIndex", fbIndex);
                renderingEngineUnit.render();
            }
            Framebuffer.unbind();
        }
    }

    @Override
    public void render(ShaderProgram shaderProgram) {
        int fbSize = framebuffers.size();
        for (int i = 0; i < iterations; i++) {
            int sizeFactor = (int) Math.ceil((double) i / (double) fbSize);
            int fbIndex = i - sizeFactor * fbSize;
            Framebuffer currentFramebuffer = framebuffers.get(fbIndex);

            currentFramebuffer.bind();
            for (RenderingEngineUnit renderingEngineUnit : renderingEngineUnits) {
                shaderProgram.setUniform1I("fbIndex", fbIndex);
                renderingEngineUnit.render(shaderProgram);
            }
            Framebuffer.unbind();
        }
    }
}
