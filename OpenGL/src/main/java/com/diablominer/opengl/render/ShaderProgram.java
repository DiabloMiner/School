package com.diablominer.opengl.render;

import java.io.*;

import org.lwjgl.opengl.GL20;

public class ShaderProgram {

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;

    public ShaderProgram() throws Exception {
        programId = GL20.glCreateProgram();
        if (programId == 0) {
            throw new Exception("Could not create Shader");
        }
    }

    public void createVertexShader(String shaderFile) throws Exception {
        vertexShaderId = createShader(shaderFile, GL20.GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderFile) throws Exception {
        fragmentShaderId = createShader(shaderFile, GL20.GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderFile, int shaderType) throws Exception {
        int shaderId = GL20.glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new Exception("Error creating shader. Type: " + shaderType);
        }

        GL20.glShaderSource(shaderId, readFile(shaderFile));
        GL20.glCompileShader(shaderId);

        if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0) {
            throw new Exception("Error compiling Shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024));
        }

        GL20.glAttachShader(programId, shaderId);

        return shaderId;
    }

    public void link() throws Exception {
        GL20.glLinkProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == 0) {
            throw new Exception("Error linking Shader code: " + GL20.glGetProgramInfoLog(programId, 1024));
        }

        if (vertexShaderId != 0) {
            GL20.glDetachShader(programId, vertexShaderId);
        }
        if (fragmentShaderId != 0) {
            GL20.glDetachShader(programId, fragmentShaderId);
        }

        GL20.glValidateProgram(programId);
        if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == 0) {
            System.err.println("Warning validating Shader code: " + GL20.glGetProgramInfoLog(programId, 1024));
        }
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }

    public void cleanup() {
        unbind();
        if (programId != 0) {
            GL20.glDeleteProgram(programId);
        }
    }

    private String readFile(String filename) {
        StringBuilder string = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(new File("./src/main/resources/shader/" + filename + ".glsl")));
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


}
