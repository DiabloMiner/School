package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.BufferUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.util.*;

public class DirectionalLight implements Light {

    private static final ShaderProgram shadowShader;
    static {try {shadowShader = new ShaderProgram("L6_DirShadowVS", "L6_DirShadowFS");} catch (Exception e) {throw new RuntimeException(e);}}
    public static float near = 0.0001f, far = 30.0f;
    public static final int sortingIndex = 0;
    public static List<DirectionalLight> allDirectionalLights = new ArrayList<>();

    public Vector3f direction, color;
    private Renderer shadowRenderer;
    private final Framebuffer shadowFramebuffer;
    private final FramebufferTexture2D shadowTexture;

    public DirectionalLight(Vector3f direction, Vector3f color, int shadowSize) {
        this.direction = direction;
        this.color = color;
        allDirectionalLights.add(this);
        allLights.add(this);

        shadowTexture = new FramebufferTexture2D(shadowSize, shadowSize, GL33.GL_DEPTH_COMPONENT, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, BufferUtil.createBuffer(new Vector4f(1.0f)), FramebufferAttachment.DEPTH_ATTACHMENT);
        shadowFramebuffer = new Framebuffer(shadowTexture);
    }

    @Override
    public Vector3f getColor() {
        return color;
    }

    @Override
    public void setUniformData(ShaderProgram shaderProgram, int index) {
        shaderProgram.setUniformVec3F("dirLight" + index + ".direction", direction);
        shaderProgram.setUniformVec3F("dirLight" + index + ".color", color);
        shaderProgram.setUniformMat4F("dirLight" + index + "Matrix", getLightSpaceMatrices()[0]);

        shadowTexture.bind();
        shaderProgram.setUniform1I("dirLight" + index + ".shadowMap", shadowTexture.getIndex());
    }

    @Override
    public void unbindShadowTextures() {
        shadowTexture.unbind();
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
    public Matrix4f[] getLightSpaceMatrices() {
        Matrix4f projection = new Matrix4f().identity().ortho(-15.0f, 15.0f, -15.0f, 15.0f, near, far);
        Matrix4f view = new Matrix4f().identity().lookAt(new Vector3f(direction).mul(-5.0f), new Vector3f(0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
        return new Matrix4f[] {new Matrix4f(projection).mul(view)};
    }

}
