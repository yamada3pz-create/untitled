package org.example.data;

import com.google.gson.*;
import org.example.block.*;
import org.example.generation.Biome;
import org.example.generation.Biomes;
import org.example.item.Item;
import org.example.item.Items;

import java.io.*;
import java.util.*;

public class DataLoader {

    private static final Gson GSON = new GsonBuilder().create();

    public static void loadAll(){
        loadItems("data/vanilla/item");
        loadBlocks("data/vanilla/block");
        loadBiomes("data/vanilla/biome");
        loadModels("data/vanilla/model/block");
        System.out.println("[DataLoader] Блоки, предметы, биомы и модели загружены.");
    }

    // ── Блоки ──────────────────────────────────────────────

    public static void loadBlocks(String folderPath){
        File dir = new File(folderPath);
        List<File> jsons = findJsons(dir);
        Collections.sort(jsons);

        for(File f : jsons){
            try {
                JsonObject obj = GSON.fromJson(new FileReader(f), JsonObject.class);
                if(!obj.has("id")) continue;
                obj = resolveParent(obj, dir);
                String id = getString(obj, "id", "unknown");
                BlockProperties props = buildProps(obj);

                // Если дроп не указан, но существует предмет с тем же id —
                // блок выпадает одноимённым предметом
                if(getString(obj, "drops", "").isEmpty() && Items.get(id) != null){
                    props.drops(id);
                }

                int idInt = Blocks.allocateId();
                Block block = new Block(idInt, id, props);

                if(block.getId().equals("air")){
                    Blocks.setAir(block);
                }
                Blocks.registerDynamic(idInt, block);

            } catch(Exception e){
                System.out.println("[DataLoader] Ошибка чтения блока " + f.getName() + ": " + e.getMessage());
            }
        }
    }

    private static BlockProperties buildProps(JsonObject obj){
        BlockProperties p = BlockProperties.create();
        p.name(getString(obj, "name", ""));
        p.solid(getBool(obj, "isSolid", true));
        p.layer(getString(obj, "layer", "object"));
        p.moveSpeed(getFloat(obj, "moveSpeed", 1.0f));
        p.drillTier(getInt(obj, "drillTier", 0));
        p.infinite(getBool(obj, "infinite", false));
        p.defaultAmount(getInt(obj, "defaultAmount", 0));
        p.drops(getString(obj, "drops", ""));
        p.hasRotation(getBool(obj, "hasRotation", false));
        p.hasBlockEntity(getBool(obj, "hasBlockEntity", false));
        p.destroyTime(getFloat(obj, "destroyTime", 1.0f));

        if(obj.has("connectsToTags")){
            JsonArray arr = obj.getAsJsonArray("connectsToTags");
            String[] tags = new String[arr.size()];
            for(int i = 0; i < arr.size(); i++) tags[i] = arr.get(i).getAsString();
            p.connectsToTags(tags);
        }
        return p;
    }

    // ── Предметы ───────────────────────────────────────────

    public static void loadItems(String folderPath){
        File dir = new File(folderPath);
        List<File> jsons = findJsons(dir);

        for(File f : jsons){
            try {
                JsonObject obj = GSON.fromJson(new FileReader(f), JsonObject.class);
                String id = getString(obj, "id", "unknown");
                String name = getString(obj, "name", id);
                int maxStack = getInt(obj, "maxStackSize", 64);

                Item item = new Item(id, name, maxStack);

                if(obj.has("placeBlock")){
                    item.setPlaceBlock(obj.get("placeBlock").getAsString());
                }

                Items.registerDynamic(item);

            } catch(Exception e){
                System.out.println("[DataLoader] Ошибка чтения предмета " + f.getName() + ": " + e.getMessage());
            }
        }
    }

    // ── Биомы ──────────────────────────────────────────────

    public static void loadBiomes(String folderPath){
        File dir = new File(folderPath);
        List<File> jsons = findJsons(dir);

        for(File f : jsons){
            try {
                JsonObject obj = GSON.fromJson(new FileReader(f), JsonObject.class);
                String id = getString(obj, "id", "unknown");

                float minTemp = getFloat(obj, "minTemperature", 0f);
                float maxTemp = getFloat(obj, "maxTemperature", 1f);
                float minHumid = getFloat(obj, "minHumidity", 0f);
                float maxHumid = getFloat(obj, "maxHumidity", 1f);
                String surface = getString(obj, "surfaceBlock", "grass");

                Biome.Builder builder = Biome.create(id)
                        .temperature(minTemp, maxTemp)
                        .humidity(minHumid, maxHumid)
                        .surfaceBlock(surface);

                if(obj.has("ores")){
                    JsonObject oresObj = obj.getAsJsonObject("ores");
                    for(Map.Entry<String, JsonElement> entry : oresObj.entrySet()){
                        String oreId = entry.getKey();
                        JsonObject oreData = entry.getValue().getAsJsonObject();
                        float rarity = getFloat(oreData, "rarity", 0.75f);
                        float width = getFloat(oreData, "width", 0.03f);
                        int minCount = getInt(oreData, "minCount", 1);
                        int maxCount = getInt(oreData, "maxCount", 4);
                        builder.ore(oreId, rarity, width, minCount, maxCount);
                    }
                }

                Biomes.register(builder.build());
                System.out.println("[DataLoader] Биом загружен: " + id);

            } catch(Exception e){
                System.out.println("[DataLoader] Ошибка чтения биома " + f.getName() + ": " + e.getMessage());
            }
        }
    }

    // ── Модели ─────────────────────────────────────────────

    public static void loadModels(String folderPath){
        File dir = new File(folderPath);
        if(!dir.exists()){
            System.out.println("[DataLoader] Папка моделей не найдена: " + folderPath);
            return;
        }
        List<File> jsons = findJsons(dir);

        for(File f : jsons){
            try {
                JsonObject obj = GSON.fromJson(new FileReader(f), JsonObject.class);
                String blockId = f.getName().replace(".json", "");
                Block block = Blocks.getByName(blockId);
                if(block == null || block.getId().equals("air")) continue;

                BlockModel model = new BlockModel(block.getGlobalId());

                if(obj.has("textures")){
                    JsonObject texObj = obj.getAsJsonObject("textures");
                    for(Map.Entry<String, JsonElement> entry : texObj.entrySet()){
                        String key = entry.getKey();
                        if(key.equals("variants")){
                            model.setVariantCount(entry.getValue().getAsInt());
                        } else {
                            model.setTexture(key, entry.getValue().getAsString());
                        }
                    }
                }

                if(obj.has("animation")){
                    JsonObject animObj = obj.getAsJsonObject("animation");
                    int frames = getInt(animObj, "frames", 1);
                    int speed = getInt(animObj, "speed", 10);
                    boolean loop = getBool(animObj, "loop", true);
                    model.setAnimation(new BlockModel.Animation(frames, speed, loop));
                }

                BlockModels.register(model);
                System.out.println("[DataLoader] Модель загружена: " + blockId);

            } catch(Exception e){
                System.out.println("[DataLoader] Ошибка чтения модели " + f.getName() + ": " + e.getMessage());
            }
        }
    }

    // ── Утилиты ────────────────────────────────────────────

    private static JsonObject resolveParent(JsonObject obj, File dir) throws Exception{
        if(!obj.has("parent")) return obj;
        String parentId = obj.get("parent").getAsString();
        File parentFile = new File(dir, parentId + ".json");
        if(!parentFile.exists()){
            System.out.println("[DataLoader] Parent не найден: " + parentId);
            return obj;
        }
        JsonObject parent = GSON.fromJson(new FileReader(parentFile), JsonObject.class);
        parent = resolveParent(parent, dir);
        for(Map.Entry<String, JsonElement> entry : obj.entrySet()){
            if(!entry.getKey().equals("parent")){
                parent.add(entry.getKey(), entry.getValue());
            }
        }
        return parent;
    }

    private static List<File> findJsons(File dir){
        List<File> result = new ArrayList<>();
        if(!dir.exists()) return result;
        File[] files = dir.listFiles();
        if(files == null) return result;
        for(File f : files){
            if(f.isDirectory()){
                result.addAll(findJsons(f));
            } else if(f.getName().endsWith(".json")){
                result.add(f);
            }
        }
        return result;
    }

    private static String getString(JsonObject obj, String key, String def){
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }
    private static boolean getBool(JsonObject obj, String key, boolean def){
        return obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }
    private static float getFloat(JsonObject obj, String key, float def){
        return obj.has(key) ? obj.get(key).getAsFloat() : def;
    }
    private static int getInt(JsonObject obj, String key, int def){
        return obj.has(key) ? obj.get(key).getAsInt() : def;
    }
}