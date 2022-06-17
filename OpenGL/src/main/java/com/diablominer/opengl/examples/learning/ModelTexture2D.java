package com.diablominer.opengl.examples.learning;

public class ModelTexture2D extends Texture2D {

    public String path;
    public String type;

    public ModelTexture2D(String filePath, String type, boolean isInSRGBColorSpace, boolean flipImage) {
        super(filePath, isInSRGBColorSpace, flipImage);
        this.path = filePath;
        this.type = type;
    }

}
