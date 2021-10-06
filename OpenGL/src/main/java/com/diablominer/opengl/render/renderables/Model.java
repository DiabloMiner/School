package com.diablominer.opengl.render.renderables;

import com.diablominer.opengl.render.RenderingEngineUnit;
import com.diablominer.opengl.render.ShaderProgram;
import com.diablominer.opengl.render.textures.TwoDimensionalTexture;
import com.diablominer.opengl.utils.ListUtil;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Model extends Renderable {

    private List<Mesh> meshes;
    private String path;
    private List<TwoDimensionalTexture> loadedTwoDimensionalTextures;

    public Model(String path, RenderingEngineUnit renderingEngineUnit, Vector3f position) {
        super(position);
        renderingEngineUnit.addNewRenderable(this);
        meshes = new ArrayList<>();
        loadedTwoDimensionalTextures = new ArrayList<>();
        this.path = path;
        loadModel(path);
    }

    public Model(String path, Vector3f position) {
        super(position);
        meshes = new ArrayList<>();
        loadedTwoDimensionalTextures = new ArrayList<>();
        this.path = path;
        loadModel(path);
    }

    public void destroy() {
        for (Mesh mesh : meshes) {
            mesh.destroy();
        }
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        for (Mesh mesh : meshes) {
            super.setPositionAsModelMatrix(shaderProgram);
            mesh.draw(shaderProgram);
        }
    }

    private void loadModel(String path) {
        // Import the file on the given path
        AIScene aiScene = Assimp.aiImportFile(path, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_CalcTangentSpace);
        if (aiScene == null || (aiScene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || aiScene.mRootNode() == null) {
            System.err.println("An AdvancedLighting loading error has been encountered: " + Assimp.aiGetErrorString());
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
        // Vertices, normals, tangents and bitangents are processed here
        List<Float> vertices = processVertexAttribute3F(mesh.mVertices());
        List<Float> normals = processVertexAttribute3F(mesh.mNormals());
        List<Float> textureCoordinates = new ArrayList<>();
        List<Integer> indices = processIndices(mesh);
        List<TwoDimensionalTexture> twoDimensionalTextures = new ArrayList<>();
        List<Float> tangents = processVertexAttribute3F(mesh.mTangents());
        List<Float> biTangents = processVertexAttribute3F(mesh.mBitangents());


        // TwoDimensionalTexture coordinates are processed here if they exist
        if (mesh.mTextureCoords(0) != null) {
            textureCoordinates = processVertexAttribute2F(mesh.mTextureCoords(0));
        }

        // Here twoDimensionalTextures are processed if they exist
        if (mesh.mMaterialIndex() >= 0) {
            AIMaterial material = AIMaterial.create(scene.mMaterials().get(mesh.mMaterialIndex()));
            List<TwoDimensionalTexture> diffuseMaps = loadMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, "texture_color");
            List<TwoDimensionalTexture> normalMaps = loadMaterialTexture(material, Assimp.aiTextureType_NORMALS, "texture_normal");
            List<TwoDimensionalTexture> displacementMaps = loadMaterialTexture(material, Assimp.aiTextureType_DISPLACEMENT, "texture_displacement");
            List<TwoDimensionalTexture> roughnessMaps = loadMaterialTexture(material, Assimp.aiTextureType_SPECULAR, "texture_roughness");
            List<TwoDimensionalTexture> metallicMaps = loadMaterialTexture(material, Assimp.aiTextureType_EMISSIVE, "texture_metallic");
            List<TwoDimensionalTexture> aoMaps = loadMaterialTexture(material, Assimp.aiTextureType_AMBIENT, "texture_ao");
            List<TwoDimensionalTexture> reflectionAndRefractionMaps = loadMaterialTexture(material, Assimp.aiTextureType_OPACITY, "texture_reflection");

            twoDimensionalTextures.addAll(diffuseMaps);
            twoDimensionalTextures.addAll(normalMaps);
            twoDimensionalTextures.addAll(displacementMaps);
            twoDimensionalTextures.addAll(roughnessMaps);
            twoDimensionalTextures.addAll(metallicMaps);
            twoDimensionalTextures.addAll(aoMaps);
            twoDimensionalTextures.addAll(reflectionAndRefractionMaps);
        }
        return new Mesh(ListUtil.convertListToArray(vertices), ListUtil.convertListToArray(normals), ListUtil.convertListToArray(textureCoordinates), ListUtil.convertListToArray(tangents), ListUtil.convertListToArray(biTangents), indices.stream().mapToInt(i -> i).toArray(), twoDimensionalTextures);
    }

    private List<TwoDimensionalTexture> loadMaterialTexture(AIMaterial material, int type, String typeName) {
        List<TwoDimensionalTexture> twoDimensionalTextures = new ArrayList<>();
        for (int i = 0; i < Assimp.aiGetMaterialTextureCount(material, type); i++) {
            AIString str = AIString.calloc();
            Assimp.aiGetMaterialTexture(material, type, 0, str, (IntBuffer) null, null, null, null, null, null);
            boolean skip = false;
            String path = new File(this.path).getParent() + File.separator + str.dataString();
            for (TwoDimensionalTexture twoDimensionalTexture : loadedTwoDimensionalTextures) {
                if (twoDimensionalTexture.path.equals(path)) {
                    twoDimensionalTextures.add(twoDimensionalTexture);
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                TwoDimensionalTexture twoDimensionalTexture = new TwoDimensionalTexture(path, typeName, true, true);
                twoDimensionalTextures.add(twoDimensionalTexture);
                loadedTwoDimensionalTextures.add(twoDimensionalTexture);
            }
            str.free();
        }
        return twoDimensionalTextures;
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

    public List<Vector3f> getAllVertices() {
        List<Vector3f> points = new ArrayList<>();
        List<Vector3f> result = new ArrayList<>();
        for (Mesh mesh : meshes) {
            for (int i = 0; i < mesh.vertices.length; i += 3) {
                points.add(new Vector3f(mesh.vertices[i], mesh.vertices[i + 1], mesh.vertices[i + 2]));
            }
            for (int i = 0; i < mesh.indices.length; i++) {
                result.add(new Vector3f(points.get(mesh.indices[i])));
            }
        }
        return result;
    }

    public List<Vector3f> getAllVerticesInWorldCoordinates(Matrix4f worldMatrix) {
        List<Vector3f> result = getAllVertices();
        Transforms.multiplyListWithMatrix(result, worldMatrix);
        return result;
    }

    public List<Vector3f> getAllUniqueVertices() {
        List<Vector3f> result = new ArrayList<>();
        for (Mesh mesh : meshes) {
            for (int i = 0; i < mesh.vertices.length; i += 3) {
                int finalI = i;
                if (result.stream().noneMatch(vector3f -> vector3f.equals(mesh.vertices[finalI], mesh.vertices[finalI + 1], mesh.vertices[finalI + 2]))) {
                    result.add(new Vector3f(mesh.vertices[i], mesh.vertices[i + 1], mesh.vertices[i + 2]));
                }
            }
        }
        return result;
    }

}
