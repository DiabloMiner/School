package com.diablominer.opengl.render;

import com.diablominer.opengl.utils.ListUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;

import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model extends Renderable {

    private List<Mesh> meshes;
    private String path;

    public Model(String path, RenderingEngineUnit renderingEngineUnit, Vector3f position) {
        super(position);
        renderingEngineUnit.addNewRenderable(this);
        meshes = new ArrayList<>();
        this.path = path;
        loadModel(path);
    }

    public void cleanUp() {
        for (Mesh mesh : meshes) {
            mesh.destroy();
        }
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        super.setPositionAsModelMatrix(shaderProgram);
        for (Mesh mesh : meshes) {
            mesh.draw(shaderProgram);
        }
    }

    private void loadModel(String path) {
        // Import the file on the given path
        AIScene aiScene = Assimp.aiImportFile(path, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs);
        if (aiScene == null || (aiScene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || aiScene.mRootNode() == null) {
            System.err.println("An Assimp loading error has been encountered: " + Assimp.aiGetErrorString());
        }

        // Process the scene
        processScene(aiScene);
    }

    private void processScene(AIScene scene) {
        // Process all meshes in this scene
        for (int i = 0; i < scene.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(i));
            meshes.add(processMesh(mesh, scene));
        }
    }

    private Mesh processMesh(AIMesh mesh, AIScene scene) {
        // Vertices and normals are processed here
        List<Float> vertices = processVertexAttribute3F(mesh.mVertices());
        List<Float> normals = processVertexAttribute3F(mesh.mNormals());
        List<Float> textureCoordinates = new ArrayList<>();
        List<Integer> indices = processIndices(mesh);
        List<Texture> textures = new ArrayList<>();

        // Texture coordinates are processed here if they exist
        if (mesh.mTextureCoords(0) != null) {
            textureCoordinates = processVertexAttribute2F(mesh.mTextureCoords(0));
        }

        // Here textures are processed if they exist
        if (mesh.mMaterialIndex() >= 0) {
            AIMaterial material = AIMaterial.create(scene.mMaterials().get(mesh.mMaterialIndex()));
            List<Texture> diffuseMaps = loadMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, "texture_diffuse");
            textures.addAll(diffuseMaps);
            List<Texture> specularMaps = loadMaterialTexture(material, Assimp.aiTextureType_SPECULAR, "texture_specular");
            textures.addAll(specularMaps);
        }
        return new Mesh(ListUtil.convertListToArray(vertices), ListUtil.convertListToArray(normals), ListUtil.convertListToArray(textureCoordinates), indices.stream().mapToInt(i -> i).toArray(), textures);
    }

    private List<Texture> loadMaterialTexture(AIMaterial material, int type, String typeName) {
        List<Texture> textures = new ArrayList<>();
        for (int i = 0; i < Assimp.aiGetMaterialTextureCount(material, type); i++) {
            AIString str = AIString.calloc();
            Assimp.aiGetMaterialTexture(material, type, 0, str, (IntBuffer) null, null, null, null, null, null);
            File file = new File(path);
            try {
                Texture texture = Texture.loadTexture(file.getParent() + File.separator + str.dataString());
                texture.type = typeName;
                textures.add(texture);
            } catch (IOException e) { e.printStackTrace(); }
            str.free();
        }
        return textures;
    }

    private List<Float> processVertexAttribute3F(AIVector3D.Buffer buffer) {
        List<Float> attributes = new ArrayList<>();
        while (buffer.remaining() > 0) {
            AIVector3D vector = buffer.get();
            attributes.add(vector.x());
            attributes.add(vector.y());
            attributes.add(vector.z());
        }
        return attributes;
    }

    private List<Float> processVertexAttribute2F(AIVector3D.Buffer buffer) {
        List<Float> attributes = new ArrayList<>();
        while (buffer.remaining() > 0) {
            AIVector3D vector = buffer.get();
            attributes.add(vector.x());
            attributes.add(vector.y());
        }
        return attributes;
    }

    private List<Integer> processIndices(AIMesh mesh) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace aiFace = mesh.mFaces().get(i);
            for (int j = 0; j < aiFace.mNumIndices(); j++) {
                indices.add(aiFace.mIndices().get(j));
            }
        }
        return indices;
    }
}
