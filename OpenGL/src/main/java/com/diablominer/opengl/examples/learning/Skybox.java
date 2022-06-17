package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;

public class Skybox extends Renderable {

    public static final int convolutedTextureSize = 32;
    public static final int prefilteredTextureSize = 128;
    public static final int brdfLookUpTextureSize = 512;
    public static final AssimpModel model = new AssimpModel("./src/main/resources/models/HelloWorld/skyboxCube.obj", new Matrix4f().identity(), false);

    private final CubeMap skyboxTexture;
    private final CubeMap convolutedTexture;
    private final CubeMap prefilteredTexture;
    private final Texture2D brdfLookUpTexture;

    public Skybox(String directory, String fileType, boolean flipImage) throws Exception {
        super(false);
        skyboxTexture = new CubeMap(directory, fileType, flipImage, true);
        convolutedTexture = CubeMap.convoluteCubeMap(skyboxTexture, convolutedTextureSize).storedTexture;
        prefilteredTexture = CubeMap.prefilterCubeMap(skyboxTexture, prefilteredTextureSize).storedTexture;
        brdfLookUpTexture = Texture2D.createBRDFLookUpTexture(brdfLookUpTextureSize).storedTexture;
    }

    public Skybox(String filePath, int size, boolean flipImage) throws Exception {
        super(false);
        skyboxTexture = CubeMap.fromEquirectangularMap(filePath, size, flipImage).storedTexture;
        convolutedTexture = CubeMap.convoluteCubeMap(skyboxTexture, convolutedTextureSize).storedTexture;
        prefilteredTexture = CubeMap.prefilterCubeMap(skyboxTexture, prefilteredTextureSize).storedTexture;
        brdfLookUpTexture = Texture2D.createBRDFLookUpTexture(brdfLookUpTextureSize).storedTexture;
    }

    @Override
    public void draw(ShaderProgram shaderProgram) {
        model.draw(shaderProgram);
    }

    @Override
    public void destroy() {
        skyboxTexture.destroy();
    }

    public void bindTexture() {
        skyboxTexture.bind();
    }

    public void unbindTexture() {
        skyboxTexture.unbind();
    }

    public int getTextureIndex() {
        return skyboxTexture.getIndex();
    }

    public void bindConvolutedTexture() {
        convolutedTexture.bind();
    }

    public void unbindConvolutedTexture() {
        convolutedTexture.unbind();
    }

    public int getConvolutedTextureIndex() {
        return convolutedTexture.getIndex();
    }

    public void bindPrefilteredTexture() {
        prefilteredTexture.bind();
    }

    public void unbindPrefilteredTexture() {
        prefilteredTexture.unbind();
    }

    public int getPrefilteredTextureIndex() {
        return prefilteredTexture.getIndex();
    }

    public void bindBrdfLookUpTextureTexture() {
        brdfLookUpTexture.bind();
    }

    public void unbindBrdfLookUpTextureTexture() {
        brdfLookUpTexture.unbind();
    }

    public int getBrdfLookUpTextureIndex() {
        return brdfLookUpTexture.getIndex();
    }

    public static void destroyModel() {
        if (!(model.meshes.isEmpty() && model.loadedTexture2DS.isEmpty())) {
            model.destroy();
        }
    }

}
