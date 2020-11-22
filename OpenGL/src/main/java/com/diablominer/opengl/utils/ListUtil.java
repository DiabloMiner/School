package com.diablominer.opengl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ListUtil {

    public static float[] convertListToArray(List<Float> list) {
        float[] result = new float[list.size()];
        IntStream.range(0, list.size()).forEach(i -> result[i] = list.get(i));
        return result;
    }

    public static List<Float> createEmptyList(int size) {
        List<Float> result = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            result.add(0.0f);
        }
        return result;
    }
}
