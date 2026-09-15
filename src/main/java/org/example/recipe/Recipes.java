package org.example.recipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Реестр рецептов по id. Заполняется RecipeLoader'ом из data/vanilla/recipes/*.json.
 */
public class Recipes {

    private static final HashMap<String, Recipe> REGISTRY = new HashMap<>();

    public static void register(Recipe recipe){
        REGISTRY.put(recipe.getId(), recipe);
    }

    public static Recipe get(String id){
        return REGISTRY.get(id);
    }

    public static boolean exists(String id){
        return REGISTRY.containsKey(id);
    }

    public static Map<String, Recipe> getAll(){
        return new HashMap<>(REGISTRY);
    }

    public static void clear(){
        REGISTRY.clear();
    }
}