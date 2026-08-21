package org.example.block;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.example.Main;
import org.example.core.ResourceManager;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.*;
import java.util.List;

public class Blocks {

    // Статические ссылки на базовые блоки для быстрого доступа в коде
    public static Block AIR;
    public static Block GRASS;
    public static Block STONE;
    public static Block SAND;
    public static Block PIPE;
    public static Block CHEST;
    public static Block MACHINE;
    /**
     * Этот метод запускается один раз при старте игры
     */
    public static void register(){

        AIR     = register(0, "air",   BlockProperties.create().solid(false).color(new Color(0,0,0,0)));
        GRASS   = register(1, "grass", BlockProperties.create().solid(true).destroyTime(0.5f).color(new Color(50,150,50)));
        STONE   = register(2, "stone", BlockProperties.create().solid(true).destroyTime(1.5f).color(Color.GRAY));
        SAND    = register(3, "sand",  BlockProperties.create().solid(true).destroyTime(0.5f).color(new Color(210,200,140)));
        PIPE    = register(4, "pipe",  BlockProperties.create().solid(false).color(Color.DARK_GRAY).hasRotation(true).connectsToTags("pipe", "pump").hasBlockEntity(true));
        CHEST   = register(5, "chest", BlockProperties.create().solid(true).destroyTime(0.8f).color(new Color(139,90,43)).hasBlockEntity(true));
        MACHINE = register(6, "machine", BlockProperties.create().solid(true).destroyTime(1.0f).color(Color.ORANGE).hasRotation(true));



        System.out.println("[Blocks] Все базовые блоки успешно зарегистрированы.");
    }

    private static Block register(int globalId, String name, BlockProperties properties){
        Block block = new Block(globalId, name, properties);
        REGISTRY[globalId] = block;
        return block;
    }
    // Простой массив - индекс = globalId (максимум 266 блоков)
    private static final Block[] REGISTRY = new Block[256];




    // Получить блок по его id
    public static Block get(int globalId){
        if(globalId < 0 || globalId >= REGISTRY.length || REGISTRY[globalId] == null){
            return AIR;
        }
        return REGISTRY[globalId];
    }

    public static Block get(String id){
        for (int i = 0; i < REGISTRY.length; i++) {
            if(REGISTRY[i] != null && REGISTRY[i].getId().equals(id)){
                return REGISTRY[i];
            }
        }
        return AIR;
    }

    public static int getGlocalId(String id){
        Block block = get(id);
        return block.getGlobalId();
    }
}
