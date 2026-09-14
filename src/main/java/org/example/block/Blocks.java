package org.example.block;

import java.util.HashMap;

public class Blocks {

    public static Block AIR;

    private static final Block[] REGISTRY = new Block[256];
    private static final HashMap<String, Block> BY_NAME = new HashMap<>();
    private static int nextId = 0;

    public static void registerDynamic(int globalId, Block block){
        REGISTRY[globalId] = block;
        BY_NAME.put(block.getId(), block);
    }

    public static int allocateId(){
        return nextId++;
    }

    public static Block get(int globalId){
        if(globalId < 0 || globalId >= REGISTRY.length || REGISTRY[globalId] == null) return AIR;
        return REGISTRY[globalId];
    }

    public static Block getByName(String id){
        return BY_NAME.getOrDefault(id, AIR);
    }

    public static void setAir(Block air){
        AIR = air;
    }

    public static Iterable<Block> getAll() {
        return BY_NAME.values();
    }
}
