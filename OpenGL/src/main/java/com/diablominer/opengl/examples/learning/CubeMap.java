package com.diablominer.opengl.examples.learning;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.AbstractMap;
import java.util.Collections;

public class CubeMap extends Texture {

    public CubeMap(String directory, String fileType, boolean flipImage, boolean isInSRGBColorSpace) {
        super(Target.CubeMapTexture);
        this.format = Format.RGBA;
        this.type = Type.UNSIGNED_BYTE;
        if (isInSRGBColorSpace) {
            this.internalFormat = InternalFormat.SRGB_ALPHA;
        } else {
            this.internalFormat = InternalFormat.RGBA;
        }
        String[] files = {directory + File.separator + "right" + fileType, directory + File.separator + "left" + fileType, directory + File.separator + "top" + fileType,
                directory + File.separator + "bottom" + fileType, directory + File.separator + "front" + fileType, directory + File.separator + "back" + fileType};

        bind();
        for (int i = 0; i < files.length; i++) {
            IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(flipImage);
            ByteBuffer buffer = STBImage.stbi_load(files[i], xBuffer, yBuffer, channelsBuffer, 4);

            width = xBuffer.get();
            height = yBuffer.get();
            if (buffer != null) {
                GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat.value, width, height, 0, format.value, type.value, buffer);
                STBImage.stbi_image_free(buffer);
            } else {
                System.err.println("CubeMap loading has failed, because the files couldn't be loaded from the given directory.");
            }
        }

        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glGenerateMipmap(target.value);

        unbind();
    }

    private CubeMap(int id, int width, int height, InternalFormat internalFormat, Format format, Type type) {
        // WARNING: This constructor should only be used if a cubemap has already been generated for OpenGL and needs to be registered in the texture handling system
        super(id, Target.CubeMapTexture);
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;
        this.width = width;
        this.height = height;
    }

    public CubeMap(int width, int height, InternalFormat internalFormat, Format format, Type type, int minFilter, int magFilter) {
        super(Target.CubeMapTexture, internalFormat, format, type, width, height);

        bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat.value, width, height, 0, format.value, type.value, (ByteBuffer) null);
        }
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MIN_FILTER, minFilter);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MAG_FILTER, magFilter);
        GL33.glGenerateMipmap(target.value);
        unbind();
    }

    public CubeMap(int width, int height, InternalFormat internalFormat, Format format, Type type) {
        super(Target.CubeMapTexture, internalFormat, format, type, width, height);
        this.width = width;
        this.height = height;

        bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat.value, width, height, 0, format.value, type.value, (ByteBuffer) null);
        }
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(target.value, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glGenerateMipmap(target.value);
        unbind();
    }

    public static FramebufferCubeMap fromEquirectangularMap(String filePath, int size, boolean flipImage) throws Exception {
        Texture2D equirectangularMap = new Texture2D(filePath, flipImage, InternalFormat.RGBA16F, Format.RGBA, Type.FLOAT, true);
        Framebuffer framebuffer = new Framebuffer(new FramebufferCubeMap[] {new FramebufferCubeMap(size, size, InternalFormat.RGBA16F, Format.RGBA, Type.FLOAT, GL33.GL_LINEAR_MIPMAP_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.COLOR_ATTACHMENT0),
            new FramebufferCubeMap(size, size, InternalFormat.DEPTH24, Format.DEPTH, Type.FLOAT, GL33.GL_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.DEPTH_ATTACHMENT)});
        ShaderProgram shaderProgram = new ShaderProgram("L6_IBLCubeMapVS", "L6_IBLCubeMapGS", "L6_EqMToCMFS");
        AssimpModel cube = new AssimpModel("./src/main/resources/models/HelloWorld/skyboxCube.obj", new Matrix4f().identity());

        Matrix4f projection = new Matrix4f().identity().perspective(Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);
        Matrix4f[] viewMatrices = new Matrix4f[] {
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(-1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  1.0f,  0.0f), new Vector3f(0.0f,  0.0f,  1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, -1.0f,  0.0f), new Vector3f(0.0f,  0.0f, -1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f,  1.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f, -1.0f), new Vector3f(0.0f, -1.0f,  0.0f))
        };

        shaderProgram.setUniformMat4F("projection", projection);
        shaderProgram.setUniformMat4FArray("viewMatrices", viewMatrices);
        equirectangularMap.bind();
        shaderProgram.setUniform1I("equirectangularMap", equirectangularMap.getIndex());

        GL33.glViewport(0, 0, size, size);
        framebuffer.bind();
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);
        GL33.glDepthFunc(GL33.GL_LEQUAL);

        cube.draw(shaderProgram, new AbstractMap.SimpleEntry<>(RenderingIntoFlag.COLOR_DEPTH, RenderingParametersFlag.COLOR_DEPTH_ENABLED));

        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        equirectangularMap.unbind();
        Framebuffer.unbind();

        equirectangularMap.destroy();
        shaderProgram.destroy();
        cube.destroy();
        framebuffer.getAttachedCubeMap(FramebufferAttachment.DEPTH_ATTACHMENT).destroy();
        framebuffer.destroyOnlyFramebuffer();
        FramebufferCubeMap result = framebuffer.getAttachedCubeMap(FramebufferAttachment.COLOR_ATTACHMENT0);

        result.bind();
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);
        result.unbind();

        return result;
    }

    public static FramebufferCubeMap convoluteCubeMap(CubeMap cubeMap, int size) throws Exception {
        Framebuffer framebuffer = new Framebuffer(new FramebufferCubeMap[] {new FramebufferCubeMap(size, size, InternalFormat.RGBA16F, Format.RGBA, Type.FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0),
                new FramebufferCubeMap(size, size, InternalFormat.DEPTH24, Format.DEPTH, Type.FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT)});
        ShaderProgram shaderProgram = new ShaderProgram("L6_IBLCubeMapVS", "L6_IBLCubeMapGS", "L6_ConvolutionFS");
        AssimpModel cube = new AssimpModel("./src/main/resources/models/HelloWorld/skyboxCube.obj", new Matrix4f().identity());

        Matrix4f projection = new Matrix4f().identity().perspective(Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);
        Matrix4f[] viewMatrices = new Matrix4f[] {
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(-1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  1.0f,  0.0f), new Vector3f(0.0f,  0.0f,  1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, -1.0f,  0.0f), new Vector3f(0.0f,  0.0f, -1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f,  1.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f, -1.0f), new Vector3f(0.0f, -1.0f,  0.0f))
        };

        shaderProgram.bind();
        shaderProgram.setUniformMat4FBindless("projection", projection);
        shaderProgram.setUniformMat4FArrayBindless("viewMatrices", viewMatrices);
        cubeMap.bind();
        shaderProgram.setUniform1IBindless("environmentMap", cubeMap.getIndex());
        ShaderProgram.unbind();

        GL33.glViewport(0, 0, size, size);
        framebuffer.bind();
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);
        GL33.glDepthFunc(GL33.GL_LEQUAL);

        cube.draw(shaderProgram, new AbstractMap.SimpleEntry<>(RenderingIntoFlag.COLOR_DEPTH, RenderingParametersFlag.COLOR_DEPTH_ENABLED));

        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        cubeMap.unbind();
        Framebuffer.unbind();

        shaderProgram.destroy();
        cube.destroy();
        framebuffer.getAttachedCubeMap(FramebufferAttachment.DEPTH_ATTACHMENT).destroy();
        framebuffer.destroyOnlyFramebuffer();

        return framebuffer.getAttachedCubeMap(FramebufferAttachment.COLOR_ATTACHMENT0);
    }

    public static FramebufferCubeMap prefilterCubeMap(CubeMap cubeMap, int size) throws Exception {
        Framebuffer framebuffer = new Framebuffer(new FramebufferCubeMap[] {new FramebufferCubeMap(size, size, InternalFormat.RGBA16F, Format.RGBA, Type.FLOAT, GL33.GL_LINEAR_MIPMAP_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.COLOR_ATTACHMENT0),
                new FramebufferCubeMap(size, size, InternalFormat.DEPTH24, Format.DEPTH, Type.FLOAT, GL33.GL_LINEAR_MIPMAP_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.DEPTH_ATTACHMENT)});
        ShaderProgram shaderProgram = new ShaderProgram("L6_IBLCubeMapVS", "L6_IBLCubeMapGS", "L6_PrefilteringFS");
        AssimpModel cube = new AssimpModel("./src/main/resources/models/HelloWorld/skyboxCube.obj", new Matrix4f().identity());

        Matrix4f projection = new Matrix4f().identity().perspective(Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);
        Matrix4f[] viewMatrices = new Matrix4f[] {
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(-1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  1.0f,  0.0f), new Vector3f(0.0f,  0.0f,  1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, -1.0f,  0.0f), new Vector3f(0.0f,  0.0f, -1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f,  1.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f, -1.0f), new Vector3f(0.0f, -1.0f,  0.0f))
        };

        shaderProgram.bind();
        shaderProgram.setUniformMat4FBindless("projection", projection);
        shaderProgram.setUniformMat4FArrayBindless("viewMatrices", viewMatrices);
        shaderProgram.setUniform1FBindless("resolution", cubeMap.width);
        cubeMap.bind();
        shaderProgram.setUniform1IBindless("environmentMap", cubeMap.getIndex());
        ShaderProgram.unbind();

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glDepthFunc(GL33.GL_LEQUAL);
        int maxMipLevels = 5;
        for (int mip = 0; mip < maxMipLevels; mip++) {
            int mipSize = (int) (size * java.lang.Math.pow(0.5, mip));
            float roughness = (float) ((double) mip / (maxMipLevels - 1.0));
            shaderProgram.setUniform1F("roughness", roughness);

            framebuffer.bind();
            framebuffer.attachCubeMap(framebuffer.getAttachedCubeMap(FramebufferAttachment.COLOR_ATTACHMENT0), mip);
            framebuffer.attachCubeMap(framebuffer.getAttachedCubeMap(FramebufferAttachment.DEPTH_ATTACHMENT), mip);
            framebuffer.setDrawBuffers(Collections.singletonList(FramebufferAttachment.COLOR_ATTACHMENT0));
            framebuffer.adjustSize(mipSize, mipSize);
            Framebuffer.unbind();

            framebuffer.bind();
            GL33.glViewport(0, 0, mipSize, mipSize);
            GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

            cube.draw(shaderProgram, new AbstractMap.SimpleEntry<>(RenderingIntoFlag.COLOR_DEPTH, RenderingParametersFlag.COLOR_DEPTH_ENABLED));

            Framebuffer.unbind();
        }
        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        cubeMap.unbind();

        shaderProgram.destroy();
        cube.destroy();
        framebuffer.getAttachedCubeMap(FramebufferAttachment.DEPTH_ATTACHMENT).destroy();
        framebuffer.destroyOnlyFramebuffer();

        return framebuffer.getAttachedCubeMap(FramebufferAttachment.COLOR_ATTACHMENT0);
    }

}
