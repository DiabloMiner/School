package com.diablominer.opengl.examples.advancedlighting;

import com.diablominer.opengl.examples.modelloading.Texture;
import com.diablominer.opengl.examples.modelloading.Vertex;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.Assimp;
import org.lwjgl.assimp.*;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {

    public List<Mesh> meshes;
    private List<Texture> loadedTextures;
    String path;

    public Model(String path) {
        this.path = path;
        loadedTextures = new ArrayList<>();
        meshes = new ArrayList<>();
        loadModel(path);
    }

    private void loadModel(String path) {
        AIScene scene = Assimp.aiImportFile(path, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs);

        if (scene == null || (scene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || scene.mRootNode() == null) {
            System.err.println("An AdvancedLighting loading error has been encountered: " + Assimp.aiGetErrorString());
        }

        processScene(scene);
    }

    private void processScene(AIScene scene) {
        for (int i = 0; i < scene.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(i));
            meshes.add(processMesh(mesh, scene));
        }
    }

    private Mesh processMesh(AIMesh mesh, AIScene scene) {
        List<Vertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<Texture> textures = new ArrayList<>();

        AIVector3D.Buffer vertexBuffer = mesh.mVertices();
        AIVector3D.Buffer normalBuffer = mesh.mNormals();
        AIVector3D.Buffer texCoordBuffer = null;
        if (mesh.mTextureCoords(0) != null) {
            texCoordBuffer = mesh.mTextureCoords(0);
        }
        for (int i = 0; i < mesh.mNumVertices(); i++) {
            AIVector3D vertexVector = vertexBuffer.get(i);
            Vector3f vertex = new Vector3f(vertexVector.x(), vertexVector.y(), vertexVector.z());

            AIVector3D normalVector = normalBuffer.get(i);
            Vector3f normal = new Vector3f(normalVector.x(), normalVector.y(), normalVector.z());

            Vector2f texCoord = new Vector2f(0.0f);
            if (texCoordBuffer != null) {
                AIVector3D texCoordVector = texCoordBuffer.get(i);
                texCoord = new Vector2f(texCoordVector.x(), texCoordVector.y());
            }

            vertices.add(new Vertex(vertex, normal, texCoord));
        }

        AIFace.Buffer indicesBuffer = mesh.mFaces();
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace face = indicesBuffer.get(i);
            for (int j = 0; j < face.mNumIndices(); j++) {
                indices.add(face.mIndices().get(j));
            }
        }

        if (mesh.mMaterialIndex() >= 0) {
            AIMaterial material = AIMaterial.create(scene.mMaterials().get(mesh.mMaterialIndex()));
            List<Texture> diffuseMaps = loadMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, "texture_diffuse");
            textures.addAll(diffuseMaps);
            List<Texture> specularMaps = loadMaterialTexture(material, Assimp.aiTextureType_SPECULAR, "texture_specular");
            textures.addAll(specularMaps);
        }

        return new Mesh(vertices, indices, textures);
    }

    private List<Texture> loadMaterialTexture(AIMaterial material, int type, String typeName) {
        List<Texture> textures = new ArrayList<>();
        for (int i = 0; i < Assimp.aiGetMaterialTextureCount(material, type); i++) {
            AIString str = AIString.calloc();
            Assimp.aiGetMaterialTexture(material, type, 0, str, (IntBuffer) null, null, null, null, null, null);
            String texturePath = new File(path).getParent() + File.separator + str.dataString();
            boolean skip = false;
            for (Texture loadedTexture : loadedTextures) {
                if (loadedTexture.path.equals(texturePath)) {
                    textures.add(loadedTexture);
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                int textureId = createTexture(texturePath);
                Texture texture = new Texture(textureId, typeName, texturePath);
                textures.add(texture);
                loadedTextures.add(texture);
            }
            str.free();
        }
        return textures;
    }

    private int createTexture(String path) {
        int texture = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture);

        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);

        int[] width = new int[1];
        int[] height = new int[1];
        int[] channels = new int[1];
        STBImage.stbi_set_flip_vertically_on_load(true);
        ByteBuffer data = STBImage.stbi_load(path, width, height, channels, 4);
        if (data != null) {
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, width[0], height[0], 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, data);
            GL33.glGenerateMipmap(GL33.GL_TEXTURE_2D);

            STBImage.stbi_image_free(data);
        } else {
            System.err.println("Texture loading has failed");
        }
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);

        return texture;
    }

    public void draw(int shaderProgram) {
        for (Mesh mesh : meshes) {
            mesh.draw(shaderProgram);
        }
    }

}
