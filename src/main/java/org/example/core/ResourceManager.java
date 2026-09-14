package org.example.core;

import org.example.block.BlockModel;
import org.example.block.BlockModels;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {

    public static final String DATA_PATH = "data" + File.separator + "vanilla";
    public static final String SAVES_PATH = "saves";

    private static String currentWorldName = "world_1";

    private static final Map<String, BufferedImage> textureCache = new HashMap<>();
    private static final Map<Integer, BufferedImage> blockTextureCache = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> animatedTextureCache = new HashMap<>();
    private static final Map<Integer, BufferedImage[]> variantTextureCache = new HashMap<>();

    public static void initFolderStructure(){
        createFolder(DATA_PATH + File.separator + "biome");
        createFolder(DATA_PATH + File.separator + "block");
        createFolder(DATA_PATH + File.separator + "item");
        createFolder(DATA_PATH + File.separator + "model" + File.separator + "block");
        createFolder(DATA_PATH + File.separator + "texture" + File.separator + "block");
        createFolder(DATA_PATH + File.separator + "texture" + File.separator + "item");
        createFolder(DATA_PATH + File.separator + "texture" + File.separator + "entity");
        createFolder(SAVES_PATH);
        System.out.println("[ResourceManager] Структура папок проверена/создана.");
    }

    private static void createFolder(String path){
        File folder = new File(path);
        if(!folder.exists()) folder.mkdirs();
    }

    // ── Основной метод: модельный резолвер ──────────────────

    public static BufferedImage getBlockTexture(int globalId, String blockName, String layer, int worldX, int worldY, long tick){
        BlockModel model = BlockModels.get(globalId);

        if(model != null){
            String texBase = model.getTexture(layer);
            if(texBase == null) texBase = model.getTexture("base");

            if(texBase != null){
                if(model.isAnimated()){
                    return getAnimatedTexture(globalId, texBase, model.getAnimation(), tick);
                }
                if(model.hasVariants()){
                    return getVariantTexture(globalId, texBase, model.getVariantCount(), worldX, worldY);
                }
                return getStaticTexture(texBase);
            }
        }

        // Fallback: старый способ по имени блока
        return getStaticTexture(blockName);
    }

    // ── Статичная текстура ─────────────────────────────────

    private static BufferedImage getStaticTexture(String texName){
        String path = DATA_PATH + File.separator + "texture" + File.separator + "block"
                + File.separator + texName + ".png";
        return loadAndCache(path);
    }

    // ── Анимация ───────────────────────────────────────────

    private static BufferedImage getAnimatedTexture(int globalId, String texName, BlockModel.Animation anim, long tick){
        BufferedImage[] frames = animatedTextureCache.get(globalId);
        if(frames == null){
            frames = new BufferedImage[anim.frames];
            for(int i = 0; i < anim.frames; i++){
                String path = DATA_PATH + File.separator + "texture" + File.separator + "block"
                        + File.separator + texName + "_" + i + ".png";
                frames[i] = loadAndCache(path);
            }
            animatedTextureCache.put(globalId, frames);
        }
        int frame = (int)((tick / anim.speed) % anim.frames);
        return frames[frame];
    }

    // ── Вариативность ──────────────────────────────────────

    private static BufferedImage getVariantTexture(int globalId, String texName, int variantCount, int worldX, int worldY){
        BufferedImage[] variants = variantTextureCache.get(globalId);
        if(variants == null){
            variants = new BufferedImage[variantCount];
            for(int i = 0; i < variantCount; i++){
                String path = DATA_PATH + File.separator + "texture" + File.separator + "block"
                        + File.separator + texName + "_" + i + ".png";
                variants[i] = loadAndCache(path);
            }
            variantTextureCache.put(globalId, variants);
        }
        int variant = tileHash(worldX, worldY) % variantCount;
        return variants[variant];
    }

    // ── Хеш координат для вариативности ────────────────────

    private static int tileHash(int x, int y){
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >> 13)) * 1274126177;
        return Math.abs(h ^ (h >> 16));
    }

    // ── Загрузка с кэшированием ────────────────────────────

    private static BufferedImage loadAndCache(String path){
        if(textureCache.containsKey(path)){
            return textureCache.get(path);
        }
        try{
            File file = new File(path);
            if(file.exists()){
                BufferedImage image = ImageIO.read(file);
                textureCache.put(path, image);
                return image;
            }
        }catch(Exception e){
            System.out.println("[ResourceManager] Ошибка загрузки " + path + ": " + e.getMessage());
        }
        textureCache.put(path, null);
        return null;
    }

    // ── Пути ───────────────────────────────────────────────

    public static String getBlockTexturePath(String blockName){
        return DATA_PATH + File.separator + "texture" + File.separator + "block" + File.separator + blockName + ".png";
    }

    public static String getEntityTexturePath(String name){
        return DATA_PATH + File.separator + "texture" + File.separator + "entity" + File.separator + name + ".png";
    }

    public static String getBlockJsonPath(String blockName){
        return DATA_PATH + File.separator + "block" + File.separator + blockName + ".json";
    }

    public static String getBlockstatePath(String blockName){
        return DATA_PATH + File.separator + "blockstate" + File.separator + blockName + ".json";
    }

    public static File getBlocksFolder(){
        return new File(DATA_PATH + File.separator + "block");
    }

    public static File getItemsFolder(){
        return new File(DATA_PATH + File.separator + "item");
    }

    // ── Мир ────────────────────────────────────────────────

    public static String getCurrentWorldSavePath(){
        return SAVES_PATH + File.separator + currentWorldName;
    }
    public static void setCurrentWorldName(String worldName){
        currentWorldName = worldName;
    }
    public static String getCurrentWorldName(){
        return currentWorldName;
    }

    // ── Кэш ────────────────────────────────────────────────

    public static void clearCache(){
        textureCache.clear();
        blockTextureCache.clear();
        animatedTextureCache.clear();
        variantTextureCache.clear();
    }
}