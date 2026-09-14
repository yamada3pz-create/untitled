package org.example.core;

import com.google.gson.*;
import org.example.block.Block;
import org.example.block.BlockModel;
import org.example.block.BlockModels;
import org.example.block.Blocks;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class TextureLoader {

    private static final Gson GSON = new GsonBuilder().create();

    private static final Map<String, BufferedImage> textureCache = new HashMap<>();
    private static final Map<String, Integer> guiBorderCache = new HashMap<>();
    private static BufferedImage missingTexture;

    // ── Точка входа ───────────────────────────────────────

    public static void preloadAll() {
        generateMissingTexture();
        loadBlockTextures();
        loadItemTextures();
        loadGuiTextures();
        System.out.println("[TextureLoader] Все текстуры загружены. GUI-элементов: " + guiBorderCache.size());
    }

    // ── Block textures ────────────────────────────────────

    private static void loadBlockTextures() {
        String basePath = ResourceManager.DATA_PATH + File.separator + "texture" + File.separator + "block";
        File dir = new File(basePath);
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        // 1. Загружаем все PNG по именам файлов
        for (File f : files) {
            if (f.getName().endsWith(".png")) {
                String name = f.getName().replace(".png", "");
                BufferedImage img = loadPng(f);
                if (img != null) {
                    textureCache.put("block/" + name, img);
                }
            }
        }

        // 2. Для каждого блока с моделью создаём ключ "block/{blockId}" → нужная текстура
        for (int i = 0; i < 256; i++) {
            Block block = Blocks.get(i);
            if (block == null || block.getId().equals("air")) continue;
            BlockModel model = BlockModels.get(block.getGlobalId());
            if (model == null) {
                // Нет модели — ищем block/{blockId}.png
                if (!textureCache.containsKey("block/" + block.getId())) {
                    textureCache.put("block/" + block.getId(), textureCache.get("block/" + block.getId()));
                }
                continue;
            }

            String texName = model.getTexture("base");
            if (texName == null) texName = block.getId();

            if (model.hasVariants()) {
                // Берём первый вариант как основной для GUI
                BufferedImage variant0 = textureCache.get("block/" + texName + "_0");
                if (variant0 != null) {
                    textureCache.put("block/" + block.getId(), variant0);
                }
            } else if (model.isAnimated()) {
                // Берём первый кадр
                BufferedImage frame0 = textureCache.get("block/" + texName + "_0");
                if (frame0 != null) {
                    textureCache.put("block/" + block.getId(), frame0);
                }
            } else {
                // Статичная текстура
                BufferedImage tex = textureCache.get("block/" + texName);
                if (tex != null) {
                    textureCache.put("block/" + block.getId(), tex);
                }
            }
        }

        System.out.println("[TextureLoader] Block текстур: " + textureCache.size());
    }

    // ── Item textures ─────────────────────────────────────

    private static void loadItemTextures() {
        String basePath = ResourceManager.DATA_PATH + File.separator + "texture" + File.separator + "item";
        File dir = new File(basePath);
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.getName().endsWith(".png")) {
                String name = f.getName().replace(".png", "");
                BufferedImage img = loadPng(f);
                if (img != null) {
                    textureCache.put("item/" + name, img);
                }
            }
        }
        System.out.println("[TextureLoader] Item текстур: " + countPrefix("item/"));
    }

    // ── GUI textures + nine-slice config ──────────────────

    private static void loadGuiTextures() {
        String basePath = ResourceManager.DATA_PATH + File.separator + "texture" + File.separator + "gui";
        File dir = new File(basePath);
        if (!dir.exists()) return;

        loadGuiRecursive(dir, basePath);
        System.out.println("[TextureLoader] GUI элементов: " + guiBorderCache.size());
    }

    private static void loadGuiRecursive(File dir, String basePath) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                loadGuiRecursive(f, basePath);
            } else if (f.getName().endsWith(".json")) {
                loadGuiConfig(f, basePath);
            } else if (f.getName().endsWith(".png")) {
                String rel = f.getPath().replace(basePath + File.separator, "")
                        .replace("\\", "/").replace(".png", "");
                BufferedImage img = loadPng(f);
                if (img != null) {
                    textureCache.put("gui/" + rel, img);
                }
            }
        }
    }

    private static void loadGuiConfig(File jsonFile, String basePath) {
        try {
            JsonObject root = GSON.fromJson(new FileReader(jsonFile), JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                JsonObject elem = entry.getValue().getAsJsonObject();
                String texPath = elem.get("texture").getAsString();
                int border = 0;
                if (elem.has("scaling")) {
                    JsonObject scaling = elem.getAsJsonObject("scaling");
                    if (scaling.has("border")) {
                        border = scaling.get("border").getAsInt();
                    }
                }
                guiBorderCache.put(texPath, border);
            }
        } catch (Exception e) {
            System.out.println("[TextureLoader] Ошибка чтения GUI JSON " + jsonFile.getName() + ": " + e.getMessage());
        }
    }

    // ── Публичные методы ──────────────────────────────────

    /** Текстура для блока (по имени файла из texture/block/) */
    public static BufferedImage getBlockTexture(String name) {
        return textureCache.getOrDefault("block/" + name, missingTexture);
    }

    /** Текстура для предмета: ищет item/{id}.png, потом block/{id}.png */
    public static BufferedImage getItemTexture(String id) {
        BufferedImage tex = textureCache.get("item/" + id);
        if (tex != null) return tex;
        tex = textureCache.get("block/" + id);
        return tex != null ? tex : missingTexture;
    }

    /** Текстура GUI элемента (по пути из JSON, например "gui/sprites/widget/button") */
    public static BufferedImage getGuiTexture(String path) {
        return textureCache.getOrDefault(path, missingTexture);
    }

    /** Размер nine-slice border для GUI элемента */
    public static int getGuiBorder(String path) {
        return guiBorderCache.getOrDefault(path, 0);
    }

    /** Missing texture: розово-чёрная клетка 16×16 */
    public static BufferedImage getMissingTexture() {
        return missingTexture;
    }

    // ── Утилиты ───────────────────────────────────────────

    private static BufferedImage loadPng(File file) {
        try {
            if (file.exists()) {
                return ImageIO.read(file);
            }
        } catch (Exception e) {
            System.out.println("[TextureLoader] Ошибка загрузки " + file.getPath() + ": " + e.getMessage());
        }
        return null;
    }

    private static void generateMissingTexture() {
        missingTexture = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = missingTexture.createGraphics();
        for (int dy = 0; dy < 16; dy += 4) {
            for (int dx = 0; dx < 16; dx += 4) {
                g.setColor(((dx / 4 + dy / 4) % 2 == 0) ? new Color(255, 0, 255) : Color.BLACK);
                g.fillRect(dx, dy, 4, 4);
            }
        }
        g.dispose();
    }

    private static int countPrefix(String prefix) {
        int count = 0;
        for (String key : textureCache.keySet()) {
            if (key.startsWith(prefix)) count++;
        }
        return count;
    }

    public static void clearCache() {
        textureCache.clear();
        guiBorderCache.clear();
    }
}