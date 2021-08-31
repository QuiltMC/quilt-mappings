package quilt.internal.util;

import java.util.Arrays;
import java.util.Map;

import groovy.json.JsonSlurper;

public class JsonUtils {
    @SuppressWarnings("unchecked")
    public static <T> T getFromTree(String json, String... tree) {
        Object o = new JsonSlurper().parseText(json);
        if (tree.length == 0) {
            return (T) new JsonSlurper().parseText(json);
        }
        return getFromTree((Map<String, ?>) o, tree);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFromTree(Map<String, ?> json, String... tree) {
        if (tree.length == 0) {
            return (T) json;
        }

        Map<String, ?> current = json;
        for(int i = 0; i < tree.length - 1; i++) {
            current = (Map<String, ?>) current.get(tree[i]);
            if (current == null) {
                throw new NullPointerException("Json Tree does not contain key " + Arrays.toString(tree));
            }
        }
        return (T) current.get(tree[tree.length - 1]);
    }
}
