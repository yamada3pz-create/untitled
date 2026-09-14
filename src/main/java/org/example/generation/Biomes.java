package org.example.generation;

import org.example.core.Registry;

import java.util.Collection;

public class Biomes {

    private static final Registry<Biome> REGISTRY = new Registry<>();
    private static Biome fallback;

    public static void register(Biome biome){
        REGISTRY.register(biome.getId(), biome);
        if(fallback == null) fallback = biome;
    }

    public static Biome get(String id){
        Biome b = REGISTRY.get(id);
        return b != null ? b : fallback;
    }

    public static Collection<Biome> getAll(){
        return REGISTRY.all();
    }

    public static Biome matchBiome(float temp, float humid){
        for(Biome b : REGISTRY.all()){
            if(b.matches(temp, humid)) return b;
        }
        return fallback;
    }
}