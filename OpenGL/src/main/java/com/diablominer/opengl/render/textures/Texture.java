package com.diablominer.opengl.render.textures;

import java.util.ArrayList;
import java.util.List;

public interface Texture {

    List<Texture> allTextures = new ArrayList<>();
    List<Texture> alreadyBound = new ArrayList<>();

    void bind();

    void unbind();

    void nonModifyingUnbind();

    void destroy();

    static void unbindAllTextures() {
        for (Texture texture : alreadyBound) {
            texture.nonModifyingUnbind();
        }
        alreadyBound.clear();
    }

    static void destroyAllTextures() {
        for (Texture texture : allTextures) {
            texture.destroy();
        }
        allTextures.clear();
    }

}
