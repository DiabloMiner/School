package com.diablominer.opengl.render;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

public class Model {

    private List<Mesh> meshes;
    private List<Texture> loadedTextures;
    private String filePath;

    public Model(String path) throws IOException {
        meshes = new ArrayList<>();
        loadedTextures = new ArrayList<>();
        filePath = path;
        loadModel(path);
    }

    public void draw(ShaderProgram shader) {
        for (Mesh mesh : meshes) {
            mesh.draw(shader);
        }
    }

    private void loadModel(String path) throws IOException {
        AIScene scene = Assimp.aiImportFile(path, Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs);

        if (scene == null || scene.mFlags() == Assimp.AI_SCENE_FLAGS_INCOMPLETE || scene.mRootNode() == null) {
            System.err.println("Assimp has encountered a error: " + Assimp.aiGetErrorString());
            return;
        }

        processNode(scene.mRootNode(), scene);
    }

    private void processNode(AINode node, AIScene scene) throws IOException {
        // Process all of the nodes meshes
        PointerBuffer aiMeshes = scene.mMeshes();
        for (int i = 0; i < node.mNumMeshes(); i++) {
            assert aiMeshes != null : "aiMeshes is null";
            AIMesh mesh = AIMesh.create(aiMeshes.get(i));
            meshes.add(processMesh(mesh, scene));
        }

        // Then process all of its children
        for (int i = 0; i < node.mNumChildren(); i++) {
            PointerBuffer pointer = node.mChildren();
            AINode childNode = AINode.create(pointer.get(i));
            processNode(childNode, scene);
        }
    }

    private Mesh processMesh(AIMesh mesh, AIScene scene) throws IOException {
        float[] vertices = processVertexAttribute3F2(mesh.mVertices());
        float[] normals = processVertexAttribute3F2(mesh.mNormals());
        float[] texCoords;
        if (mesh.mTextureCoords(0) != null) {
            texCoords = processVertexAttribute2F2(mesh.mTextureCoords(0));
        } else {
            System.err.println("No texture coordinates could be loaded");
            texCoords = new float[0];
        }
        int[] indices = processIndices(mesh);
        List<Texture> textures;
        if (scene.mNumMaterials() > 0 || scene.mNumTextures() > 0) {
            textures = processTextures(mesh, scene);
        } else {
            System.err.println("No textures could be loaded");
            textures = new ArrayList<>();
        }
        /*float[] vertices = new float[1];
        float[] normals = new float[1];
        float[] texCoords = new float[1];
        int[] indices = new int[1];
        List<Texture> textures = new ArrayList<>();*/
        return new Mesh(vertices, normals, texCoords, indices, textures);
    }

    private List<Texture> processTextures(AIMesh mesh, AIScene scene) throws IOException {
        List<Texture> textures = new ArrayList<>();
        if (mesh.mMaterialIndex() >= 0) {
            PointerBuffer a = scene.mMaterials();
            AIMaterial material = AIMaterial.create(a.get(mesh.mMaterialIndex()));

            List<Texture> diffuseMaps = loadMaterialTextures(scene, material, Assimp.aiTextureType_DIFFUSE, "texture_diffuse");
            textures.addAll(diffuseMaps);
            /*List<Texture> specularMaps = loadMaterialTextures(scene, material, Assimp.aiTextureType_SPECULAR, "texture_specular");
            textures.addAll(specularMaps);*/
        }
        return textures;
    }

    private List<Texture> loadMaterialTextures(AIScene scene, AIMaterial material, int type, String typeName) throws IOException {
        List<Texture> textures = new ArrayList<>();
        for (int i = 0; i < scene.mNumMaterials(); i++ ) {
            AIString path = AIString.calloc();
            Assimp.aiGetMaterialTexture(material, type, 0, path, (IntBuffer) null, null, null, null, null, null);
            String textPath = path.dataString();
            boolean skip = false;
            for (Texture loadedTexture : loadedTextures) {
                if (loadedTexture.path.equals(textPath)) {
                    textures.add(loadedTexture);
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                System.out.println(filePath.substring(0, filePath.lastIndexOf("/")) + "/" + textPath);
                Texture texture = new Texture(filePath.substring(0, filePath.lastIndexOf("/")) + "/" + textPath);
                texture.type = typeName;
                texture.path = textPath;
                textures.add(texture);
                loadedTextures.add(texture);
            }
            path.free();
        }
        return textures;
    }

    private int[] processIndices(AIMesh mesh) {
        AIFace.Buffer aiFaces = mesh.mFaces();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
        int[] result = indices.stream().mapToInt(i -> i).toArray();
        return result;
    }

    private float[] processVertexAttribute3F(AIVector3D.Buffer buffer, int numberOfElements) {
        float[] attributes = new float[numberOfElements];
        for (int i = 0; i < numberOfElements; i += 3) {
            AIVector3D attribute = buffer.get();
            attributes[i] = attribute.x();
            attributes[i + 1] = attribute.y();
            attributes[i + 2] = attribute.y();
            i++;
        }
        return attributes;
    }

    private float[] processVertexAttribute2F(AIVector3D.Buffer buffer, int numberOfElements) {
        float[] attributes = new float[numberOfElements];
        for (int i = 0; i < numberOfElements; i += 2) {
            AIVector3D attribute = buffer.get();
            attributes[i] = attribute.x();
            attributes[i + 1] = attribute.y();
            i++;
        }
        return attributes;
    }

    private float[] processVertexAttribute3F2(AIVector3D.Buffer buffer) {
        List<Float> attributes = new ArrayList<>();
        while (buffer.remaining() > 0) {
            AIVector3D vector = buffer.get();
            attributes.add(vector.x());
            attributes.add(vector.y());
            attributes.add(vector.z());
        }
        float[] result = new float[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            result[i] = attributes.get(i);
        }
        return result;
    }

    private float[] processVertexAttribute2F2(AIVector3D.Buffer buffer) {
        List<Float> attributes = new ArrayList<>();
        while (buffer.remaining() > 0) {
            AIVector3D vector = buffer.get();
            attributes.add(vector.x());
            attributes.add(vector.y());
        }
        float[] result = new float[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            result[i] = attributes.get(i);
        }
        return result;
    }

}
