package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ShaderProgram {

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private int geometryShaderId;

    public ShaderProgram() throws Exception {
        programId = GL33.glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        geometryShaderId = 0;
    }

    public ShaderProgram(String vertexShaderPath, String fragmentShaderPath) throws Exception {
        programId = GL33.glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        geometryShaderId = 0;
        createVertexShader(vertexShaderPath);
        createFragmentShader(fragmentShaderPath);
        link();
    }

    public ShaderProgram(String vertexShaderPath, String geometryShaderPath, String fragmentShaderPath) throws Exception {
        programId = GL33.glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
        createVertexShader(vertexShaderPath);
        createGeometryShader(geometryShaderPath);
        createFragmentShader(fragmentShaderPath);
        link();
    }

    public void createVertexShader(String shaderFile) throws Exception {
        vertexShaderId = createShader(shaderFile, GL33.GL_VERTEX_SHADER);
    }

    public void createGeometryShader(String shaderFile) throws Exception {
        geometryShaderId = createShader(shaderFile, GL33.GL_GEOMETRY_SHADER);
    }

    public void createFragmentShader(String shaderFile) throws Exception {
        fragmentShaderId = createShader(shaderFile, GL33.GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderFile, int shaderType) throws Exception {
        int shaderId = GL33.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        GL33.glShaderSource(shaderId, readFile(shaderFile));
        GL33.glCompileShader(shaderId);

        if (GL33.glGetShaderi(shaderId, GL33.GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + GL33.glGetShaderInfoLog(shaderId, 1024));
        }

        GL33.glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        GL33.glLinkProgram(programId);
        if (GL33.glGetProgrami(programId, GL33.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL33.glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            GL33.glDetachShader(programId, vertexShaderId);
        }
        if (geometryShaderId != 0) {
            GL33.glDetachShader(programId, geometryShaderId);
        }
        if (fragmentShaderId != 0) {
            GL33.glDetachShader(programId, fragmentShaderId);
        }
    }

    public void validate() {
        GL33.glValidateProgram(programId);
        if (GL33.glGetProgrami(programId, GL33.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + GL33.glGetProgramInfoLog(programId, 1024));
        }
    }

    public void bind() {
        GL33.glUseProgram(programId);
    }

    public void destroy() {
        unbind();
        if (programId != 0 && GL33.glIsProgram(programId)) {
            GL33.glDeleteProgram(programId);
        }
    }

    private String readFile(String filename) {
        StringBuilder string = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("./src/main/java/com/diablominer/opengl/examples/learning/" + filename + ".glsl"));
            String line;
            while ((line = reader.readLine()) != null) {
                string.append(line);
                string.append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return string.toString();
    }

    public int getProgramId() {
        return programId;
    }

    public void setUniform1I(String name, int value) {
        bind();
        GL33.glUniform1i(GL33.glGetUniformLocation(programId, name), value);
        unbind();
    }

    public void setUniform1F(String name, float value) {
        bind();
        GL33.glUniform1f(GL33.glGetUniformLocation(programId, name), value);
        unbind();
    }

    public void setUniformVec3F(String name, float val1, float val2, float val3) {
        bind();
        GL33.glUniform3f(GL33.glGetUniformLocation(programId, name), val1, val2, val3);
        unbind();
    }

    public void setUniformVec3F(String name, Vector3f vector) {
        bind();
        GL33.glUniform3f(GL33.glGetUniformLocation(programId, name), vector.get(0), vector.get(1), vector.get(2));
        unbind();
    }

    public void setUniformMat4F(String name, Matrix4f data) {
        bind();
        float[] value = new float[4 * 4];
        data.get(value);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(programId, name), false, value);
        unbind();
    }

    public void setUniformMat4FArray(String name, Matrix4f[] data) {
        bind();
        for (int i = 0; i < data.length; i++) {
            float[] value = new float[4 * 4];
            data[i].get(value);
            GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(programId, name + "[" + i + "]"), false, value);
        }
        unbind();
    }

    public void setUniform1IBindless(String name, int value) {
        GL33.glUniform1i(GL33.glGetUniformLocation(programId, name), value);
    }

    public void setUniform1FBindless(String name, float value) {
        GL33.glUniform1f(GL33.glGetUniformLocation(programId, name), value);
    }

    public void setUniformVec3FBindless(String name, float val1, float val2, float val3) {
        GL33.glUniform3f(GL33.glGetUniformLocation(programId, name), val1, val2, val3);
    }

    public void setUniformVec3FBindless(String name, Vector3f vector) {
        GL33.glUniform3f(GL33.glGetUniformLocation(programId, name), vector.get(0), vector.get(1), vector.get(2));
    }

    public void setUniformMat4FBindless(String name, Matrix4f data) {
        float[] value = new float[4 * 4];
        data.get(value);
        GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(programId, name), false, value);
    }

    public void setUniformMat4FArrayBindless(String name, Matrix4f[] data) {
        for (int i = 0; i < data.length; i++) {
            float[] value = new float[4 * 4];
            data[i].get(value);
            GL33.glUniformMatrix4fv(GL33.glGetUniformLocation(programId, name + "[" + i + "]"), false, value);
        }
    }

    public void setUniformBlockBindings(UniformBufferBlock[] buffers) {
        for (UniformBufferBlock buffer : buffers) {
            setUniformBlockBinding(buffer);
        }
    }

    public void setUniformBlockBinding(UniformBufferBlock buffer) {
        bind();
        GL33.glUniformBlockBinding(programId, GL33.glGetUniformBlockIndex(programId, buffer.name), buffer.bindingPoint);
        unbind();
        buffer.bindToUniformBlockBinding();
    }

    public static void unbind() {
        GL33.glUseProgram(0);
    }

}
