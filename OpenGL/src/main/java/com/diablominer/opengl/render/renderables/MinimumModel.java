package com.diablominer.opengl.render.renderables;

import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.utils.ListUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

import java.util.ArrayList;
import java.util.List;

public class MinimumModel {

    private List<Float> vertices = new ArrayList<>();
    public ShaderProgram shaderProgram;
    private int VBO;
    private int VAO;

    public MinimumModel(List<Vector3f> vertices) {
        for (Vector3f vertex : vertices) {
            this.vertices.add(vertex.x);
            this.vertices.add(vertex.y);
            this.vertices.add(vertex.z);
        }
        try {
            shaderProgram = new ShaderProgram("./quickhullTestShaders/VertexShader", "./quickhullTestShaders/FragmentShader");
        }
        catch (Exception e) { e.printStackTrace(); }

        float[] floatVertices = ListUtil.convertListToArray(this.vertices);
        VBO = GL33.glGenBuffers();
        VAO = GL33.glGenVertexArrays();

        GL33.glBindVertexArray(VAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, VBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, floatVertices, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindVertexArray(0);
    }

    public void draw() {
        shaderProgram.bind();
        shaderProgram.setUniformMat4F("model", new Matrix4f().identity().translate(new Vector3f(0.0f, 1.0f, 0.0f)));
        GL33.glBindVertexArray(VAO);
        GL33.glEnableVertexAttribArray(0);
        GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, 3);
        GL33.glDisableVertexAttribArray(0);
        GL33.glBindVertexArray(0);
        shaderProgram.unbind();
    }

}
