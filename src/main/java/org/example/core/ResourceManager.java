package org.example.core;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {

    // Основные корневые папки игры
    public static final String DATA_PATH = "data" + File.separator + "vanilla";
    public static final String SAVES_PATH = "saves";

    //Имя текущего активного сохранения
    private static String currentWorldName = "world_1";

    //Кэш для загруженных картинок (чтобы не читать диск каждый раз)
    private static final Map<String, BufferedImage> textureCache = new HashMap<>();
    /**
     * Инициализация структуры папок при старте игры.
     * Если папок нет, игра сама их создаст.
     */
    public static void initFolderStructure(){
        createFolder(DATA_PATH + File.separator + "biome");
        createFolder(DATA_PATH + File.separator + "block");
        createFolder(DATA_PATH + File.separator + "item");
        createFolder(DATA_PATH + File.separator + "texture" + File.separator + "block");
        createFolder(DATA_PATH + File.separator + "texture" + File.separator + "item");
        createFolder(DATA_PATH + File.separator + "texture" + File.separator + "entity");
        createFolder(SAVES_PATH);

        System.out.println("[ResourceManager] Структура папок была проверенна/создана.");
    }

    private static void createFolder(String path){
        File folder = new File(path);
        if(!folder.exists()){
            folder.mkdirs();
        }
    }
    /**
     * Загружаем и кэшируем картинку (текстуру) по указанному пути.
     */
    public static BufferedImage loadTextures(String path){
        if(textureCache.containsKey(path)){
            return textureCache.get(path);
        }
        try{
            File file = new File(path);
            if(file.exists()){
                BufferedImage image = ImageIO.read(file);
                textureCache.put(path, image);
                return image;
            }else{
                System.out.println("[ResourceManager] Предупреждение: текстура не найдена -> " + path);
            }
        }catch (Exception e){
            System.out.println("[ResourceManager] Ошибка загрузки текстуры " + path + ": " + e.getMessage());
        }
        return null;
    }
    /**
     * Возвращать путь к текстурам блока.
     */
    public static String getBlockTexturePath(String blockName){
        return DATA_PATH + File.separator + "texture" + File.separator + "block" + File.separator + blockName + ".png";
    }

    public static String getEntityTexturePath(String name){
        return DATA_PATH + File.separator + "texture" + File.separator + "entity" + File.separator + name + ".png";
    }
    /**
     * Возвращение путь к json-файлу блока (описание его свойств)
     */
    public static String getBlockJsonPath(String blockName){
        return DATA_PATH + File.separator + "block" + File.separator + blockName + ".json";
    }
    /**
     * Возвращает папки для работы с контентом
     */
    public static File getBlocksFolder(){
        return new File(DATA_PATH + File.separator + "block");
    }
    public static File getItemsFolder(){
        return new File(DATA_PATH + File.separator + "item");
    }
    /**
     * Управление мирами (сохранениями)
     */
    public static String getCurrentWorldSavePath(){
        return SAVES_PATH + File.separator + currentWorldName;
    }
    public static void setCurrentWorldName(String worldName){
        currentWorldName = worldName;
    }
    public static String getCurrentWorldName(){
        return currentWorldName;
    }
    /**
     * Очистка кэша (например, если меняется ресурс-пак)
     */
    public static void clearCache(){
        textureCache.clear();
    }
}