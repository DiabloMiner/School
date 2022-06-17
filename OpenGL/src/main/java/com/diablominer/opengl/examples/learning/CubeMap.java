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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class CubeMap implements Texture {

    public int width, height;
    protected int index;
    protected final int id, internalFormat, format, type;

    public CubeMap(String directory, String fileType, boolean flipImage, boolean isInSRGBColorSpace) {
        String[] files = {directory + File.separator + "right" + fileType, directory + File.separator + "left" + fileType, directory + File.separator + "top" + fileType,
                directory + File.separator + "bottom" + fileType, directory + File.separator + "front" + fileType, directory + File.separator + "back" + fileType};
        this.id = GL33.glGenTextures();
        this.index = -1;
        this.format = GL33.GL_RGBA;
        this.type = GL33.GL_UNSIGNED_BYTE;
        if (isInSRGBColorSpace) {
            this.internalFormat = GL33.GL_SRGB_ALPHA;
        } else {
            this.internalFormat = GL33.GL_RGBA;
        }

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
                GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, buffer);
                STBImage.stbi_image_free(buffer);
            } else {
                System.err.println("CubeMap loading has failed, because the files couldn't be loaded from the given directory.");
            }
        }

        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);

        unbind();
        allTextures.add(this);
    }

    private CubeMap(int id, int internalFormat, int format, int type) {
        // WARNING: This constructor should only be used if a cubemap has already been generated for OpenGL and needs to be registered in the texture handling system
        this.id = id;
        this.index = -1;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        bind();
        IntBuffer x = MemoryUtil.memAllocInt(1),y = MemoryUtil.memAllocInt(1);
        GL33.glGetTexLevelParameteriv(GL33.GL_TEXTURE_2D, 0, GL33.GL_TEXTURE_WIDTH, x);
        GL33.glGetTexLevelParameteriv(GL33.GL_TEXTURE_2D, 0, GL33.GL_TEXTURE_HEIGHT, y);
        this.width = x.get();
        this.height = y.get();
        unbind();

        allTextures.add(this);
    }

    public CubeMap(int width, int height, int internalFormat, int format, int type, int minFilter, int magFilter) {
        this.id = GL33.glGenTextures();
        this.index = -1;
        this.width = width;
        this.height = height;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, minFilter);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, magFilter);
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);
        unbind();

        allTextures.add(this);
    }

    public CubeMap(int width, int height, int internalFormat, int format, int type) {
        this.id = GL33.glGenTextures();
        this.index = -1;
        this.width = width;
        this.height = height;
        this.internalFormat = internalFormat;
        this.format = format;
        this.type = type;

        bind();
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);
        unbind();

        allTextures.add(this);
    }

    public void bind() {
        if (!alreadyBound.contains(this)) {
            this.index = alreadyBound.size();
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size());
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, this.id);
            alreadyBound.add(this);
        }
    }

    public void unbind() {
        if (index != -1) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + index);
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
            alreadyBound.remove(this);
            this.index = -1;
        }
    }

    public void nonModifyingUnbind() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter alreadyBound
        if (index != -1) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
            this.index = 0;
        }
    }

    public void destroy() {
        GL33.glDeleteTextures(id);
        allTextures.remove(this);
        alreadyBound.remove(this);
    }

    public void nonModifyingDestroy() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter allTextures
        GL33.glDeleteTextures(id);
        alreadyBound.remove(this);
    }

    public int getId() {
        return id;
    }

    public boolean isBound() {
        return index != -1;
    }

    public int getIndex() {
        return index;
    }

    public static FramebufferCubeMap fromEquirectangularMap(String filePath, int size, boolean flipImage) throws Exception {
        Texture2D equirectangularMap = new Texture2D(filePath, flipImage, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, true);
        Framebuffer framebuffer = new Framebuffer(new FramebufferCubeMap[] {new FramebufferCubeMap(size, size, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, GL33.GL_LINEAR_MIPMAP_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.COLOR_ATTACHMENT0),
            new FramebufferCubeMap(size, size, GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, GL33.GL_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.DEPTH_ATTACHMENT)});
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

        cube.draw(shaderProgram);

        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        equirectangularMap.unbind();
        Framebuffer.unbind();

        equirectangularMap.destroy();
        shaderProgram.destroy();
        cube.destroy();
        framebuffer.getAttachedCubeMaps().get(1).destroy();
        framebuffer.destroySelf();
        Framebuffer.allFramebuffers.remove(framebuffer);
        FramebufferCubeMap result = framebuffer.getAttachedCubeMaps().get(0);
        framebuffer.getAttachedCubeMaps().clear();

        result.bind();
        GL33.glGenerateMipmap(GL33.GL_TEXTURE_CUBE_MAP);
        result.unbind();

        return result;
    }

    public static FramebufferCubeMap convoluteCubeMap(CubeMap cubeMap, int size) throws Exception {
        Framebuffer framebuffer = new Framebuffer(new FramebufferCubeMap[] {new FramebufferCubeMap(size, size, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, FramebufferAttachment.COLOR_ATTACHMENT0),
                new FramebufferCubeMap(size, size, GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, FramebufferAttachment.DEPTH_ATTACHMENT)});
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

        shaderProgram.setUniformMat4F("projection", projection);
        shaderProgram.setUniformMat4FArray("viewMatrices", viewMatrices);
        cubeMap.bind();
        shaderProgram.setUniform1I("environmentMap", cubeMap.getIndex());

        GL33.glViewport(0, 0, size, size);
        framebuffer.bind();
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);
        GL33.glDepthFunc(GL33.GL_LEQUAL);

        cube.draw(shaderProgram);

        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        cubeMap.unbind();
        Framebuffer.unbind();

        shaderProgram.destroy();
        cube.destroy();
        framebuffer.getAttachedCubeMaps().get(1).destroy();
        framebuffer.destroySelf();
        Framebuffer.allFramebuffers.remove(framebuffer);
        FramebufferCubeMap result = framebuffer.getAttachedCubeMaps().get(0);
        framebuffer.getAttachedCubeMaps().clear();

        return result;
    }

    public static FramebufferCubeMap prefilterCubeMap(CubeMap cubeMap, int size) throws Exception {
        Framebuffer framebuffer = new Framebuffer(new FramebufferCubeMap[] {new FramebufferCubeMap(size, size, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT, GL33.GL_LINEAR_MIPMAP_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.COLOR_ATTACHMENT0),
                new FramebufferCubeMap(size, size, GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT, GL33.GL_LINEAR_MIPMAP_LINEAR, GL33.GL_LINEAR, FramebufferAttachment.DEPTH_ATTACHMENT)});
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

        shaderProgram.setUniformMat4F("projection", projection);
        shaderProgram.setUniformMat4FArray("viewMatrices", viewMatrices);
        shaderProgram.setUniform1F("resolution", cubeMap.width);
        cubeMap.bind();
        shaderProgram.setUniform1I("environmentMap", cubeMap.getIndex());

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glDepthFunc(GL33.GL_LEQUAL);
        int maxMipLevels = 5;
        for (int mip = 0; mip < maxMipLevels; mip++) {
            int mipSize = (int) (size * java.lang.Math.pow(0.5, mip));
            float roughness = (float) ((double) mip / (maxMipLevels - 1.0));
            shaderProgram.setUniform1F("roughness", roughness);

            framebuffer.bind();
            framebuffer.attachCubeMap(framebuffer.getAttachedCubeMaps().get(0), mip);
            framebuffer.attachCubeMap(framebuffer.getAttachedCubeMaps().get(1), mip);
            framebuffer.setDrawBuffers(Collections.singletonList(FramebufferAttachment.COLOR_ATTACHMENT0));
            framebuffer.adjustSize(mipSize, mipSize);
            Framebuffer.unbind();

            framebuffer.bind();
            GL33.glViewport(0, 0, mipSize, mipSize);
            GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

            cube.draw(shaderProgram);

            Framebuffer.unbind();
        }
        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glDisable(GL33.GL_DEPTH_TEST);
        cubeMap.unbind();

        shaderProgram.destroy();
        cube.destroy();
        framebuffer.getAttachedCubeMaps().get(1).destroy();
        framebuffer.destroySelf();
        Framebuffer.allFramebuffers.remove(framebuffer);
        FramebufferCubeMap result = framebuffer.getAttachedCubeMaps().get(0);
        framebuffer.getAttachedCubeMaps().clear();

        return result;
    }

}
