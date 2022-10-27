package com.diablominer.opengl.examples.learning;

import org.joml.Matrix4f;

import java.util.Map;

public class Skybox extends RenderComponent {

    public static final int convolutedTextureSize = 32;
    public static final int prefilteredTextureSize = 128;
    public static final int brdfLookUpTextureSize = 512;
    public static final AssimpModel model = new AssimpModel("./src/main/resources/models/HelloWorld/skyboxCube.obj", new Matrix4f().identity());
    public static Texture2D brdfLookUpTexture;
    static { try { brdfLookUpTexture = Texture2D.createBRDFLookUpTexture(brdfLookUpTextureSize).storedTexture; brdfLookUpTexture.bind(); } catch (Exception e) { e.printStackTrace(); } }

    public final CubeMap normalTexture;
    public final CubeMap convolutedTexture;
    public final CubeMap prefilteredTexture;

    public Skybox(String directory, String fileType, boolean flipImage) throws Exception {
        super();
        normalTexture = new CubeMap(directory, fileType, flipImage, true);
        convolutedTexture = CubeMap.convoluteCubeMap(normalTexture, convolutedTextureSize).storedTexture;
        prefilteredTexture = CubeMap.prefilterCubeMap(normalTexture, prefilteredTextureSize).storedTexture;
        bindTextures();
    }

    public Skybox(String filePath, int size, boolean flipImage) throws Exception {
        super();
        normalTexture = CubeMap.fromEquirectangularMap(filePath, size, flipImage).storedTexture;
        convolutedTexture = CubeMap.convoluteCubeMap(normalTexture, convolutedTextureSize).storedTexture;
        prefilteredTexture = CubeMap.prefilterCubeMap(normalTexture, prefilteredTextureSize).storedTexture;
        bindTextures();
    }

    private void bindTextures() {
        convolutedTexture.bind();
        prefilteredTexture.bind();
    }

    public int getConvolutedTextureIndex() {
        if (!convolutedTexture.isBound()) {
            convolutedTexture.bind();
        }
        return convolutedTexture.getIndex();
    }

    public int getPrefilteredTextureIndex() {
        if (!prefilteredTexture.isBound()) {
            prefilteredTexture.bind();
        }
        return prefilteredTexture.getIndex();
    }

    @Override
    public void draw(ShaderProgram shaderProgram, Map.Entry<RenderInto, RenderParameters> flags) {
        model.draw(shaderProgram, flags);
    }

    @Override
    public void destroy() {
        normalTexture.destroy();
        convolutedTexture.destroy();
        prefilteredTexture.destroy();
    }

    public static int getBrdfLookUpTextureIndex() {
        if (!brdfLookUpTexture.isBound()) {
            brdfLookUpTexture.bind();
        }
        return brdfLookUpTexture.getIndex();
    }

}
