package com.diablominer.opengl.render.textures;

import java.util.ArrayList;
import java.util.List;

public interface Texture {

    List<Texture> allTextures = new ArrayList<>();
    List<Texture> alreadyBound = new ArrayList<>();

    void bind();

    void unbind();

    void nonModifyingUnbind();
    // The implementation may not alter alreadyBound

    void destroy();

    void nonModifyingDestroy();
    // The implementation may not alter allTextures

    static void unbindAllTextures() {
        for (Texture texture : alreadyBound) {
            texture.nonModifyingUnbind();
        }
        alreadyBound.clear();
    }

    static void destroyAllTextures() {
        for (Texture texture : allTextures) {
            texture.nonModifyingDestroy();
        }
        allTextures.clear();
    }

}
