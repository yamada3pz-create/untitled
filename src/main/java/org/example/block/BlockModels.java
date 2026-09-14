package org.example.block;

import java.util.HashMap;

public class BlockModels {

    private static final HashMap<Integer, BlockModel> BY_ID = new HashMap<>();

    public static void register(BlockModel model){
        BY_ID.put(model.getGlobalId(), model);
    }

    public static BlockModel get(int globalId){
        return BY_ID.get(globalId);
    }

    public static void clear(){
        BY_ID.clear();
    }
}