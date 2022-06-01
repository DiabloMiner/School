package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.utils.ListUtil;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model extends Renderable {

    protected List<com.diablominer.opengl.examples.learning.Mesh> meshes;
    protected List<Texture2D> loadedTexture2DS;
    protected String path;

    public Model(List<Mesh> meshes, List<Texture2D> loadedTexture2DS) {
        super();
        this.meshes = new ArrayList<>(meshes);
        this.loadedTexture2DS = new ArrayList<>(loadedTexture2DS);
        this.path = "";
    }

    public Model(List<Mesh> meshes, List<Texture2D> loadedTexture2DS, boolean throwsShadow) {
        super(throwsShadow);
        this.meshes = new ArrayList<>(meshes);
        this.loadedTexture2DS = new ArrayList<>(loadedTexture2DS);
        this.path = "";
    }

    public Model(String path, Vector3f position) {
        super(position);
        meshes = new ArrayList<>();
        loadedTexture2DS = new ArrayList<>();
        this.path = path;
        loadModel(path);
    }

    public Model(String path, Matrix4f model) {
        super(model);
        meshes = new ArrayList<>();
        loadedTexture2DS = new ArrayList<>();
        this.path = path;
        loadModel(path);
    }

    public void destroy() {
        for (com.diablominer.opengl.examples.learning.Mesh mesh : meshes) {
            mesh.destroy();
        }
        meshes.clear();
        loadedTexture2DS.clear();
    }

    public void draw(ShaderProgram shaderProgram) {
        for (com.diablominer.opengl.examples.learning.Mesh mesh : meshes) {
            super.setModelMatrixUniform(shaderProgram);
            mesh.draw(shaderProgram);
        }
    }

    public void draw(ShaderProgram shaderProgram, Matrix4f modelMatrix) {
        for (com.diablominer.opengl.examples.learning.Mesh mesh : meshes) {
            super.setModelMatrixUniform(shaderProgram, modelMatrix);
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

    private com.diablominer.opengl.examples.learning.Mesh processMesh(AIMesh mesh, AIScene scene) {
        // Vertices, normals, tangents and bitangents are processed here
        List<Float> vertices = processVertexAttribute3F(mesh.mVertices());
        List<Float> normals = processVertexAttribute3F(mesh.mNormals());
        List<Float> textureCoordinates = new ArrayList<>();
        List<Float> tangents = processVertexAttribute3F(mesh.mTangents());
        List<Float> biTangents = processVertexAttribute3F(mesh.mBitangents());
        List<Integer> indices = processIndices(mesh);
        List<Texture2D> texture2Ds = new ArrayList<>();


        // Texture2D coordinates are processed here, if they exist
        if (mesh.mTextureCoords(0) != null) {
            textureCoordinates = processVertexAttribute2F(mesh.mTextureCoords(0));
        }

        // Here Texture2Ds are processed if they exist
        if (mesh.mMaterialIndex() >= 0) {
            AIMaterial material = AIMaterial.create(scene.mMaterials().get(mesh.mMaterialIndex()));
            List<Texture2D> diffuseMaps = loadMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, "texture_color");
            List<Texture2D> normalMaps = loadMaterialTexture(material, Assimp.aiTextureType_NORMALS, "texture_normal");
            List<Texture2D> displacementMaps = loadMaterialTexture(material, Assimp.aiTextureType_DISPLACEMENT, "texture_displacement");
            List<Texture2D> roughnessMaps = loadMaterialTexture(material, Assimp.aiTextureType_SPECULAR, "texture_roughness");
            List<Texture2D> metallicMaps = loadMaterialTexture(material, Assimp.aiTextureType_EMISSIVE, "texture_metallic");
            List<Texture2D> aoMaps = loadMaterialTexture(material, Assimp.aiTextureType_AMBIENT, "texture_ao");
            List<Texture2D> reflectionAndRefractionMaps = loadMaterialTexture(material, Assimp.aiTextureType_OPACITY, "texture_reflection");

            texture2Ds.addAll(diffuseMaps);
            texture2Ds.addAll(normalMaps);
            texture2Ds.addAll(displacementMaps);
            texture2Ds.addAll(roughnessMaps);
            texture2Ds.addAll(metallicMaps);
            texture2Ds.addAll(aoMaps);
            texture2Ds.addAll(reflectionAndRefractionMaps);
        }

        return new com.diablominer.opengl.examples.learning.Mesh(ListUtil.convertListToArray(vertices), ListUtil.convertListToArray(normals),
                ListUtil.convertListToArray(textureCoordinates), ListUtil.convertListToArray(tangents), ListUtil.convertListToArray(biTangents),
                indices.stream().mapToInt(i -> i).toArray(), texture2Ds);
    }

    private List<Texture2D> loadMaterialTexture(AIMaterial material, int type, String typeName) {
        List<Texture2D> texture2DS = new ArrayList<>();
        for (int i = 0; i < Assimp.aiGetMaterialTextureCount(material, type); i++) {
            AIString str = AIString.calloc();
            Assimp.aiGetMaterialTexture(material, type, 0, str, (IntBuffer) null, null, null, null, null, null);
            boolean skip = false;
            String path = new File(this.path).getParent() + File.separator + str.dataString();
            for (Texture2D texture2D : loadedTexture2DS) {
                if (texture2D.path.equals(path)) {
                    texture2DS.add(texture2D);
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                Texture2D texture2D = new Texture2D(path, typeName, true, true);
                texture2DS.add(texture2D);
                loadedTexture2DS.add(texture2D);
            }
            str.free();
        }
        return texture2DS;
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
        for (com.diablominer.opengl.examples.learning.Mesh mesh : meshes) {
            for (int i = 0; i < mesh.getVertices().length; i += 3) {
                points.add(new Vector3f(mesh.getVertices()[i], mesh.getVertices()[i + 1], mesh.getVertices()[i + 2]));
            }
            for (int i = 0; i < mesh.getIndices().length; i++) {
                result.add(new Vector3f(points.get(mesh.getIndices()[i])));
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
        for (com.diablominer.opengl.examples.learning.Mesh mesh : meshes) {
            for (int i = 0; i < mesh.getVertices().length; i += 3) {
                int finalI = i;
                if (result.stream().noneMatch(vector3f -> vector3f.equals(mesh.getVertices()[finalI], mesh.getVertices()[finalI + 1], mesh.getVertices()[finalI + 2]))) {
                    result.add(new Vector3f(mesh.getVertices()[i], mesh.getVertices()[i + 1], mesh.getVertices()[i + 2]));
                }
            }
        }
        return result;
    }

}
