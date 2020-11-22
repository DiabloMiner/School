package com.diablominer.opengl.render;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TextureCache {

    private static AtomicReference<TextureCache> cache = new AtomicReference<>();

    public static TextureCache getInstance() {
        if(cache.get() == null) {
            cache.set(new TextureCache());
        }

        return cache.get();
    }


    private Map<String, Texture> textureCache = new ConcurrentHashMap<>();

    private TextureCache() {}

    public void registerTexture(String texturePath, Texture texture) {
        this.textureCache.put(texturePath, texture);
    }

    public Texture getTexture(String texturePath) {
        return this.textureCache.get(texturePath);
    }
}