package org.example.item;

import org.example.core.Registry;

public class Items {

    private static final Registry<Item> REGISTRY = new Registry<>();

    public static void registerDynamic(Item item){
        REGISTRY.register(item.getId(), item);
    }

    public static Item get(String id){
        return REGISTRY.get(id);
    }
}