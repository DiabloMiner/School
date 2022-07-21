package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class PointLight extends Light {

    private static ShaderProgram shadowShader;
    public static float near = 0.001f, far = 30.0f;

    public Vector3f position;
    private final Framebuffer shadowFramebuffer;
    private final float aspect;
    private final FramebufferCubeMap shadowTexture;

    public PointLight(Vector3f position, Vector3f color, int shadowSize) {
        super(color);
        this.position = position;
        this.aspect = (float) shadowSize / (float) shadowSize;

        shadowTexture = new FramebufferCubeMap(shadowSize, shadowSize, Texture.InternalFormat.DEPTH, Texture.Format.DEPTH, Texture.Type.FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT);
        shadowFramebuffer = new Framebuffer(shadowTexture);
        shadowTexture.bind();
    }

    @Override
    void updateShadowMatrices() {
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
        this.lightSpaceMatrices = lightSpaceMatrices.toArray(new Matrix4f[0]);
    }

    @Override
    public void setUniformData(ShaderProgram shaderProgram, int index) {
        shaderProgram.setUniformVec3FBindless("pointLight" + index + ".position", position);
        shaderProgram.setUniformVec3FBindless("pointLight" + index + ".color", color);

        if (!shadowTexture.storedTexture.isBound()) {
            shadowTexture.storedTexture.bind();
        }
        shaderProgram.setUniform1IBindless("pointLight" + index + ".shadowMap", shadowTexture.storedTexture.getIndex());
        shaderProgram.setUniform1FBindless("pointLight" + index + ".far", far);
    }

    @Override
    public void initializeShadowRenderer(Renderable[] renderables) {
        shadowRenderer = new SingleFramebufferRenderer(shadowFramebuffer, new RenderingUnit[] {new ShadowRenderingUnit(getShadowShader(), renderables, this)});
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
