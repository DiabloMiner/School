package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.*;
import java.util.function.Consumer;

public class PointLight implements Light {

    private static final ShaderProgram shadowShader;
    static {try {shadowShader = new ShaderProgram("L6_OmniDirShadowVS", "L6_OmniDirShadowGS", "L6_OmniDirShadowFS");} catch (Exception e) {throw new RuntimeException(e);}}
    public static float near = 0.0001f, far = 30.0f;
    public static final int sortingIndex = 1;
    public static List<PointLight> allPointLights = new ArrayList<>();

    public Vector3f position, color;
    private Renderer shadowRenderer;
    private final Framebuffer shadowFramebuffer;
    private float aspect;

    public PointLight(Vector3f position, Vector3f color, int shadowSize) {
        this.position = position;
        this.color = color;
        this.aspect = (float) shadowSize / (float) shadowSize;
        allPointLights.add(this);
        allLights.add(this);

        shadowFramebuffer = new Framebuffer(new FramebufferCubeMap(shadowSize, shadowSize, GL33.GL_DEPTH_COMPONENT, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT));
    }

    @Override
    public Vector3f getColor() {
        return color;
    }

    @Override
    public void setUniformData(ShaderProgram shaderProgram, int index) {
        int correctedIndex = index - DirectionalLight.allDirectionalLights.size();
        shaderProgram.setUniformVec3F("pointLight" + correctedIndex + ".position", position);
        shaderProgram.setUniformVec3F("pointLight" + correctedIndex + ".color", color);
    }

    @Override
    public void unbindShadowTextures() {
        shadowFramebuffer.getAttached2DTextures().get(0).unbind();
    }

    @Override
    public void initializeShadowRenderer(Renderable[] renderables) {
        shadowRenderer = new SingleFramebufferRenderer(shadowFramebuffer, new RenderingEngineUnit[] {new ShadowRenderingEngineUnit(shadowShader, renderables, this)});
    }

    @Override
    public Renderer getShadowRenderer(){
        return shadowRenderer;
    }

    @Override
    public Matrix4f[] getLightSpaceMatrices() {
        Matrix4f projection = new Matrix4f().identity().perspective((float) Math.toRadians(90.0), aspect, near, far);
        Matrix4f[] viewMatrices = {
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(-1.0f, 0.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 1.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, -1.0f, 0.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 0.0f, 1.0f), new Vector3f(0.0f, -1.0f, 0.0f)),
                new Matrix4f().identity().lookAt(position, new Vector3f(position).add(0.0f, 0.0f, -1.0f), new Vector3f(0.0f, -1.0f, 0.0f))
        };
        List<Matrix4f> lightSpaceMatrices = new ArrayList<>();
        Arrays.stream(viewMatrices).forEach(mat -> lightSpaceMatrices.add(new Matrix4f(projection).mul(mat)));
        return lightSpaceMatrices.toArray(new Matrix4f[0]);
    }

}
