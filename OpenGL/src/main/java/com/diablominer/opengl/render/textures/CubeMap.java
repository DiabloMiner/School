package com.diablominer.opengl.render.textures;

import com.diablominer.opengl.render.ShaderProgram;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class CubeMap {

    public static List<CubeMap> alreadyBound = new ArrayList<>();
    public static List<CubeMap> allTextures = new ArrayList<>();
    public static int activeTextureOffset = 0;

    public int id;
    public int index;
    private CubeMap cubeMapTexture;

    public CubeMap(String directory, String fileType, boolean flipImage) {
        String[] files = {directory + File.separator + "right" + fileType, directory + File.separator + "left" + fileType, directory + File.separator + "top" + fileType,
                directory + File.separator + "bottom" + fileType, directory + File.separator + "front" + fileType, directory + File.separator + "back" + fileType};
        this.id = GL33.glGenTextures();
        this.index = 0;
        this.cubeMapTexture = null;
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, id);

        for (int i = 0; i < files.length; i++) {
            IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
            IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(flipImage);
            ByteBuffer buffer = STBImage.stbi_load(files[i], xBuffer, yBuffer, channelsBuffer, 4);

            if (buffer != null) {
                GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL33.GL_SRGB_ALPHA, xBuffer.get(), yBuffer.get(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
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

        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
        allTextures.add(this);
    }

    public CubeMap(String file, boolean flipImage, int size) throws Exception {
        // Should only be used for equirectangular maps
        // This constructor maps these maps to normal cube maps

        // Create inital equirectangular 2d texture
        IntBuffer xBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer yBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer channelsBuffer = MemoryUtil.memAllocInt(1);
        STBImage.stbi_set_flip_vertically_on_load(flipImage);
        FloatBuffer buffer = STBImage.stbi_loadf(file, xBuffer, yBuffer, channelsBuffer, 4);
        int texture = 0;
        this.index = 0;
        if (buffer != null) {
            texture = GL33.glGenTextures();
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA16F, xBuffer.get(), yBuffer.get(), 0, GL33.GL_RGBA, GL33.GL_FLOAT, buffer);

            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

            STBImage.stbi_image_free(buffer);
        } else {
            new Exception("Equirectangular CubeMap loading has failed, because the files couldn't be loaded from the given directory.");
        }

        // Create a framebuffer for mapping the texture to a cube
        int frameBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);

        cubeMapTexture = new CubeMap(size, size, GL33.GL_RGBA16F, GL33.GL_RGBA, GL33.GL_FLOAT);
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, cubeMapTexture.id, 0);

        CubeMap depthTexture = new CubeMap(size, size, GL33.GL_DEPTH_COMPONENT24, GL33.GL_DEPTH_COMPONENT, GL33.GL_FLOAT);
        GL33.glFramebufferTexture(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_ATTACHMENT, depthTexture.id, 0);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);

        // Create needed matrices for mapping
        Matrix4f projection = new Matrix4f().identity().perspective(Math.toRadians(90.0f), 1.0f, 0.1f, 10.0f);
        Matrix4f[] viewMatrices = new Matrix4f[] {
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(-1.0f,  0.0f,  0.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  1.0f,  0.0f), new Vector3f(0.0f,  0.0f,  1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f, -1.0f,  0.0f), new Vector3f(0.0f,  0.0f, -1.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f,  1.0f), new Vector3f(0.0f, -1.0f,  0.0f)),
                new Matrix4f().identity().lookAt(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f( 0.0f,  0.0f, -1.0f), new Vector3f(0.0f, -1.0f,  0.0f))
        };

        // Create shaderprogram and set uniforms
        ShaderProgram shaderProgram = new ShaderProgram("./equirectangularToCubeMap/VertexShader", "./equirectangularToCubeMap/GeometryShader", "./equirectangularToCubeMap/FragmentShader");

        shaderProgram.setUniformMat4F("projection", projection);
        shaderProgram.setUniformMat4FArray("viewMatrices", viewMatrices);
        GL33.glActiveTexture(GL33.GL_TEXTURE30);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);
        shaderProgram.setUniform1I("equirectangularMap", 30);

        // Define Cube vertices
        float[] cubeVertices = {
                -1.0f,  1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f, -1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,

                -1.0f, -1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f, -1.0f,  1.0f,
                -1.0f, -1.0f,  1.0f,

                -1.0f,  1.0f, -1.0f,
                1.0f,  1.0f, -1.0f,
                1.0f,  1.0f,  1.0f,
                1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f,  1.0f,
                -1.0f,  1.0f, -1.0f,

                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f,  1.0f,
                1.0f, -1.0f,  1.0f
        };

        // Preparing the cube to be rendered
        int VAO = GL33.glGenVertexArrays();
        int VBO = GL33.glGenBuffers();

        GL33.glBindVertexArray(VAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cubeVertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);

        // Rendering the equirectangular map onto a cube and saving the result as a cubemap
        GL33.glViewport(0, 0, size, size);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, frameBuffer);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);
        GL33.glDepthFunc(GL33.GL_LEQUAL);

        shaderProgram.bind();
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 36);
        GL33.glDisableVertexAttribArray(0);
        GL33.glBindVertexArray(0);
        shaderProgram.unbind();

        GL33.glDepthFunc(GL33.GL_LESS);
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glDisable(GL33.GL_DEPTH_TEST);

        this.id = cubeMapTexture.id;
        allTextures.add(this);

        System.out.println(GL33.glGetError());
    }

    public CubeMap(int width, int height, int internalFormat, int format, int type) {
        this.id = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, id);
        for (int i = 0; i < 6; i++) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, width, height, 0, format, type, (ByteBuffer) null);
        }
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_S, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_T, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_WRAP_R, GL33.GL_CLAMP_TO_EDGE);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_LINEAR);
        GL33.glTexParameteri(GL33.GL_TEXTURE_CUBE_MAP, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_LINEAR);
        GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);

        allTextures.add(this);
    }

    public void bind() {
        activeTextureOffset = Texture.alreadyBound.size();
        if (!alreadyBound.contains(this)) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + alreadyBound.size() + activeTextureOffset);
            this.index = alreadyBound.size() + activeTextureOffset;
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, this.id);
            alreadyBound.add(this);
        }
    }

    public void unbind() {
        if (index != 0) {
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + index);
            GL33.glBindTexture(GL33.GL_TEXTURE_CUBE_MAP, 0);
            alreadyBound.remove(this);
        }
    }

    protected void nonModifyingUnbind() {
        // Doesn't cause a ConcurrentModificationException, because it doesn't alter alreadyBound
        if (index != 0) {
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
            GL33.glActiveTexture(GL33.GL_TEXTURE0 + this.index);
        }
    }

    public void destroy() {
        GL33.glDeleteTextures(id);
    }

    public CubeMap getCubeMapTexture() {
        return cubeMapTexture;
    }

    public static void unbindAll() {
        for (CubeMap cubeMap : alreadyBound) {
            cubeMap.nonModifyingUnbind();
        }
        alreadyBound.clear();
    }

    public static void destroyAllCubeMaps() {
        for (CubeMap cubeMap : allTextures) {
            cubeMap.destroy();
        }
    }

}
