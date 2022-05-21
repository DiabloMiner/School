package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;

import java.util.*;

public class DirectionalLight implements Light {

    private static final ShaderProgram shadowShader;
    static {try {shadowShader = new ShaderProgram("L6_ShadowVS", "L6_DirShadowFS");} catch (Exception e) {throw new RuntimeException(e);}}
    public static final int sortingIndex = 0;
    public static List<DirectionalLight> allDirectionalLights = new ArrayList<>();

    public Vector3f direction, color;
    private Renderer shadowRenderer;
    private final Framebuffer shadowFramebuffer;

    public DirectionalLight(Vector3f direction, Vector3f color, int shadowSize) {
        this.direction = direction;
        this.color = color;
        allDirectionalLights.add(this);
        allLights.add(this);

        shadowFramebuffer = new Framebuffer(new FramebufferTexture2D(shadowSize, shadowSize, GL33.GL_DEPTH_COMPONENT, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT));
    }

    @Override
    public Vector3f getColor() {
        return color;
    }

    @Override
    public List<Vector4f> getData() {
        return new ArrayList<>(Arrays.asList(new Vector4f(direction, 0.0f), new Vector4f(color, 0.0f)));
    }

    @Override
    public void initializeShadowRenderer(Renderable[] renderables) {
        shadowRenderer = new SingleFramebufferRenderer(shadowFramebuffer, new RenderingEngineUnit[] {new ShadowRenderingEngineUnit(shadowShader, renderables, this)});
    }

    @Override
    public Renderer getShadowRenderer() {
        return shadowRenderer;
    }

    @Override
    public Matrix4f getLightSpaceMatrix() {
        Matrix4f projection = new Matrix4f().identity().ortho(-10.0f, 10.0f, -10.0f, 10.0f, 0.1f, 100.0f);
        Matrix4f view = new Matrix4f().identity().lookAt(new Vector3f(direction).mul(-1.0f), new Vector3f(0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        return new Matrix4f(projection).mul(view);
    }

}
