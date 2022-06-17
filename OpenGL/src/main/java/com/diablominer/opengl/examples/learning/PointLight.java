package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.*;

public class PointLight implements Light {

    private static ShaderProgram shadowShader;
    public static float near = 0.001f, far = 30.0f;

    public Vector3f position, color;
    private Renderer shadowRenderer;
    private final Framebuffer shadowFramebuffer;
    private final float aspect;
    private final FramebufferCubeMap shadowTexture;

    public PointLight(Vector3f position, Vector3f color, int shadowSize) {
        this.position = position;
        this.color = color;
        this.aspect = (float) shadowSize / (float) shadowSize;

        shadowTexture = new FramebufferCubeMap(shadowSize, shadowSize, GL33.GL_DEPTH_COMPONENT, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT);
        shadowFramebuffer = new Framebuffer(shadowTexture);
    }

    @Override
    public Vector3f getColor() {
        return color;
    }

    @Override
    public void setUniformData(ShaderProgram shaderProgram, int index) {
        shaderProgram.setUniformVec3F("pointLight" + index + ".position", position);
        shaderProgram.setUniformVec3F("pointLight" + index + ".color", color);

        shadowTexture.bind();
        shaderProgram.setUniform1I("pointLight" + index + ".shadowMap", shadowTexture.storedTexture.getIndex());
        shaderProgram.setUniform1F("pointLight" + index + ".far", far);
    }

    @Override
    public void unbindShadowTextures() {
        shadowTexture.unbind();
    }

    @Override
    public void initializeShadowRenderer(Renderable[] renderables) {
        shadowRenderer = new SingleFramebufferRenderer(shadowFramebuffer, new RenderingEngineUnit[] {new ShadowRenderingEngineUnit(getShadowShader(), renderables, this)});
    }

    @Override
    public Renderer getShadowRenderer() {
        return shadowRenderer;
    }

    @Override
    public Matrix4f[] getLightSpaceMatrices() {
        Matrix4f projection = new Matrix4f().identity().perspective((float) Math.toRadians(90.0), aspect, near, far);
        Matrix4f[] viewMatrices = {
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, 0.0f, 1.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, .0f, -1.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, -1.0f, 0.0f))
        };
        List<Matrix4f> lightSpaceMatrices = new ArrayList<>();
        Arrays.stream(viewMatrices).forEach(mat -> lightSpaceMatrices.add(new Matrix4f(projection).mul(mat)));
        return lightSpaceMatrices.toArray(new Matrix4f[0]);
    }

    public static ShaderProgram getShadowShader() {
        if (shadowShader == null) {
            try {
                shadowShader = new ShaderProgram("L6_OmniDirShadowVS", "L6_OmniDirShadowGS", "L6_OmniDirShadowFS");
                return shadowShader;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            return shadowShader;
        }
    }

}
