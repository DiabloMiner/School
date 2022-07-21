package com.diablominer.opengl.examples.learning;

import com.diablominer.opengl.collisiondetection.Face;
import com.diablominer.opengl.utils.ListUtil;
import com.diablominer.opengl.utils.Transforms;
import org.joml.Matrix4d;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.*;

public class AssimpModel extends Model {

    protected String path;
    public static final Map<String, ModelTexture2D> loadedTextures = new HashMap<>();

    public AssimpModel(String path, Vector3f position) {
        super(position);
        meshes = new ArrayList<>();
        this.path = path;
        loadModel(path);
    }

    public AssimpModel(String path, Matrix4f model) {
        super(model);
        meshes = new ArrayList<>();
        this.path = path;
        loadModel(path);
    }

    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        for (Mesh mesh : meshes) {
            super.setModelMatrixUniform(shaderProgram);
            mesh.draw(shaderProgram, flags);
        }
    }

    public void draw(ShaderProgram shaderProgram, Matrix4f modelMatrix, Map.Entry<RenderingIntoFlag, RenderingParametersFlag> flags) {
        for (Mesh mesh : meshes) {
            super.setModelMatrixUniform(shaderProgram, modelMatrix);
            mesh.draw(shaderProgram, flags);
        }
    }

    private void loadModel(String path) {
        // Import the file on the given path
        AIScene aiScene = Assimp.aiImportFile(path, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_CalcTangentSpace);
        if (aiScene == null || (aiScene.mFlags() & Assimp.AI_SCENE_FLAGS_INCOMPLETE) != 0 || aiScene.mRootNode() == null) {
            System.err.println("A Loading error has been encountered: " + Assimp.aiGetErrorString());
        }

        // Process the scene
        processScene(Objects.requireNonNull(aiScene));
    }

    private void processScene(AIScene scene) {
        // Process all meshes in this scene
        for (int i = 0; i < scene.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(Objects.requireNonNull(scene.mMeshes()).get(i));
            meshes.add(processMesh(mesh, scene));
        }
    }

    private AssimpMesh processMesh(AIMesh mesh, AIScene scene) {
        // Vertices, normals, tangents and bitangents are processed here
        List<Float> vertices = processVertexAttribute3F(mesh.mVertices());
        List<Float> normals = processVertexAttribute3F(Objects.requireNonNull(mesh.mNormals()));
        List<Float> textureCoordinates = new ArrayList<>();
        List<Float> tangents = processVertexAttribute3F(Objects.requireNonNull(mesh.mTangents()));
        List<Float> biTangents = processVertexAttribute3F(Objects.requireNonNull(mesh.mBitangents()));
        List<Integer> indices = processIndices(mesh);
        List<ModelTexture2D> texture2Ds = new ArrayList<>();


        // Texture2D coordinates are processed here, if they exist
        if (mesh.mTextureCoords(0) != null) {
            textureCoordinates = processVertexAttribute2F(Objects.requireNonNull(mesh.mTextureCoords(0)));
        }

        // Here Texture2Ds are processed if they exist
        if (mesh.mMaterialIndex() >= 0) {
            AIMaterial material = AIMaterial.create(Objects.requireNonNull(scene.mMaterials()).get(mesh.mMaterialIndex()));
            List<ModelTexture2D> diffuseMaps = loadMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, "texture_color", true);
            List<ModelTexture2D> normalMaps = loadMaterialTexture(material, Assimp.aiTextureType_NORMALS, "texture_normal", false);
            List<ModelTexture2D> displacementMaps = loadMaterialTexture(material, Assimp.aiTextureType_DISPLACEMENT, "texture_displacement", false);
            List<ModelTexture2D> roughnessMaps = loadMaterialTexture(material, Assimp.aiTextureType_SPECULAR, "texture_roughness", false);
            List<ModelTexture2D> metallicMaps = loadMaterialTexture(material, Assimp.aiTextureType_EMISSIVE, "texture_metallic", false);
            List<ModelTexture2D> aoMaps = loadMaterialTexture(material, Assimp.aiTextureType_AMBIENT, "texture_ao", false);
            List<ModelTexture2D> reflectionAndRefractionMaps = loadMaterialTexture(material, Assimp.aiTextureType_OPACITY, "texture_reflection", false);

            texture2Ds.addAll(diffuseMaps);
            texture2Ds.addAll(normalMaps);
            texture2Ds.addAll(displacementMaps);
            texture2Ds.addAll(roughnessMaps);
            texture2Ds.addAll(metallicMaps);
            texture2Ds.addAll(aoMaps);
            texture2Ds.addAll(reflectionAndRefractionMaps);
        }

        return new AssimpMesh(ListUtil.convertListToArray(vertices), ListUtil.convertListToArray(normals),
                ListUtil.convertListToArray(textureCoordinates), ListUtil.convertListToArray(tangents), ListUtil.convertListToArray(biTangents),
                indices.stream().mapToInt(i -> i).toArray(), texture2Ds);
    }

    private List<ModelTexture2D> loadMaterialTexture(AIMaterial material, int type, String typeName, boolean isInSRGBColorSpace) {
        List<ModelTexture2D> texture2DS = new ArrayList<>();
        for (int i = 0; i < Assimp.aiGetMaterialTextureCount(material, type); i++) {
            AIString str = AIString.calloc();
            Assimp.aiGetMaterialTexture(material, type, 0, str, (IntBuffer) null, null, null, null, null, null);
            String path = new File(this.path).getParent() + File.separator + str.dataString();
            if (loadedTextures.get(path) != null && loadedTextures.get(path).type.equals(typeName)) {
                texture2DS.add(loadedTextures.get(path));
            } else {
                ModelTexture2D texture2D = new ModelTexture2D(path, typeName, isInSRGBColorSpace, true);
                texture2DS.add(texture2D);
                loadedTextures.put(texture2D.path, texture2D);
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
        for (Mesh mesh : meshes) {
            AssimpMesh assimpMesh = (AssimpMesh) mesh;

            for (int i = 0; i < assimpMesh.getVertices().length; i += 3) {
                points.add(new Vector3f(assimpMesh.getVertices()[i], assimpMesh.getVertices()[i + 1], assimpMesh.getVertices()[i + 2]));
            }
            for (int i = 0; i < assimpMesh.getIndices().length; i++) {
                result.add(new Vector3f(points.get(assimpMesh.getIndices()[i])));
            }
        }
        return result;
    }

    public List<Vector3d> getAllVerticesD() {
        List<Vector3d> points = new ArrayList<>();
        List<Vector3d> result = new ArrayList<>();
        for (Mesh mesh : meshes) {
            AssimpMesh assimpMesh = (AssimpMesh) mesh;

            for (int i = 0; i < assimpMesh.getVertices().length; i += 3) {
                points.add(new Vector3d(assimpMesh.getVertices()[i], assimpMesh.getVertices()[i + 1], assimpMesh.getVertices()[i + 2]));
            }
            for (int i = 0; i < assimpMesh.getIndices().length; i++) {
                result.add(new Vector3d(points.get(assimpMesh.getIndices()[i])));
            }
        }
        return result;
    }

    public List<Vector3f> getAllVerticesInWorldCoordinates(Matrix4f worldMatrix) {
        List<Vector3f> result = getAllVertices();
        Transforms.multiplyListWithMatrix(result, worldMatrix);
        return result;
    }

    public List<Vector3d> getAllVerticesInWorldCoordinates(Matrix4d worldMatrix) {
        List<Vector3d> result = getAllVerticesD();
        Transforms.multiplyListWithMatrix(result, worldMatrix);
        return result;
    }

    public List<Face> getAllFaces(Matrix4f worldMatrix) {
        List<Vector3f> vertices = getAllVertices();
        Transforms.multiplyListWithMatrix(vertices, worldMatrix);
        List<Face> faces = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i += 3) {
            faces.add(new Face(vertices.get(i), vertices.get(i + 1), vertices.get(i + 2), false));
        }
        return faces;
    }

    public List<Vector3f> getAllUniqueVertices() {
        List<Vector3f> result = new ArrayList<>();
        for (Mesh mesh : meshes) {
            AssimpMesh assimpMesh = (AssimpMesh) mesh;

            for (int i = 0; i < assimpMesh.getVertices().length; i += 3) {
                int finalI = i;
                if (result.stream().noneMatch(vector3f -> vector3f.equals(assimpMesh.getVertices()[finalI], assimpMesh.getVertices()[finalI + 1], assimpMesh.getVertices()[finalI + 2]))) {
                    result.add(new Vector3f(assimpMesh.getVertices()[i], assimpMesh.getVertices()[i + 1], assimpMesh.getVertices()[i + 2]));
                }
            }
        }
        return result;
    }

    public void destroy() {
        destroyAllMeshes();
    }

}
