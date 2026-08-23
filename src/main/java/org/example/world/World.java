package org.example.world;

import org.example.block.BlockEntityType;
import org.example.block.Block;
import org.example.block.BlockEntity;
import org.example.block.Blocks;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class World {

    // Загруженные чанки: ключ = упакованные координаты чанка
    private HashMap<Long, Chunk> chunks = new HashMap<>();
    private String savePath;

    public World(String worldName){
        this.savePath = "saves" + File.separator + worldName + File.separator + "chunk" + File.separator;
        new File(savePath).mkdirs();
    }

    // Упаковка координат чанка в один ключ (координаты бывают отрицательными!)
    private long chunkKey(int chunkX, int chunkY){
        return ((long) chunkY << 32) | (chunkX & 0xFFFFFFFFL);
    }

    // --- Получаем чанки ---

    // Найти или создать чанк по координатам чанка
    public Chunk getChunk(int chunkX, int chunkY){
        long key = chunkKey(chunkX, chunkY);

        // Ищем среди загруженных — O(1)
        Chunk found = chunks.get(key);
        if(found != null) return found;

        // Пробуем загрузить из файла
        Chunk loaded = loadChunk(chunkX, chunkY);
        if(loaded == null){
            loaded = new Chunk(chunkX, chunkY);   // файла нет — пустой
        }
        chunks.put(key, loaded);
        return loaded;
    }

    // --- Доступ по мировым координатам (по слоям) ---

    public int getFloorIdAt(int worldX, int worldY) {
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getFloorId(loc[2], loc[3]);
    }

    public int getOreIdAt(int worldX, int worldY) {
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getOreId(loc[2], loc[3]);
    }

    public int getObjectIdAt(int worldX, int worldY) {
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getObjectId(loc[2], loc[3]);
    }

    public void setObjectIdAt(int worldX, int worldY, int globalId){
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).setObjectId(loc[2], loc[3], globalId);
    }

    public void setOreIdAt(int worldX, int worldY, int globalId){
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).setOreId(loc[2], loc[3], globalId);
    }

    // Мировые -> [chunkX, chunkY, localX, localY]
    private int[] toLocal(int worldX, int worldY) {
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE);
        int chunkY = Math.floorDiv(worldY, Chunk.SIZE);
        return new int[]{
                chunkX, chunkY,
                worldX - chunkX * Chunk.SIZE,
                worldY - chunkY * Chunk.SIZE
        };
    }

    // --- Сохранение и загрузка ---

    public void saveChunk (Chunk chunk) {
        String path = savePath + "c." + chunk.getChunkX() + "." + chunk.getChunkY() + ".dat";
        try{
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path));

            // Сначала энтити
            out.writeInt(chunk.getBlockEntityCount());
            for (Map.Entry<Long, BlockEntity> entry : chunk.getAllBlockEntities().entrySet()){
                long key = entry.getKey();
                BlockEntity entity = entry.getValue();

                out.writeInt((int) (key & 0xFFFF));
                out.writeInt((int) (key >> 16));
                out.writeUTF(entity.getType());
                entity.save(out);
            }
            // Потом четыре слоя одним проходом
            int[] floor  = chunk.getFloorIds();
            int[] ore    = chunk.getOreIds();
            int[] object = chunk.getObjectIds();
            int[] states = chunk.getObjectStates();
            for (int i = 0; i < Chunk.SIZE * Chunk.SIZE; i++) {
                out.writeInt(floor[i]);
                out.writeInt(ore[i]);
                out.writeInt(object[i]);
                out.writeInt(states[i]);
            }
            out.close();
            chunk.setDirty(false);
            System.out.println("[World] Сохранение чанка " + chunk.getChunkX() + "," + chunk.getChunkY());
        }catch (Exception e){
            System.out.println("[World] Ошибка сохранения: " + e.getMessage());
        }
    }

    public Chunk loadChunk(int chunkX, int chunkY){
        String path = savePath + "c." + chunkX + "." + chunkY + ".dat";
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        try {
            Chunk chunk = new Chunk(chunkX,chunkY);
            DataInputStream in = new DataInputStream(new FileInputStream(path));

            int[] floor  = new int[Chunk.SIZE * Chunk.SIZE];
            int[] ore    = new int[Chunk.SIZE * Chunk.SIZE];
            int[] object = new int[Chunk.SIZE * Chunk.SIZE];
            int[] states = new int[Chunk.SIZE * Chunk.SIZE];

            int entityCount = in.readInt();
            for (int i = 0; i < entityCount; i++) {
                int localX = in.readInt();
                int localY = in.readInt();
                String type = in.readUTF();
                BlockEntity entity = BlockEntityType.create(type, chunk.getWorldX(localX), chunk.getWorldY(localY));
                if (entity != null) {
                    entity.load(in);
                    chunk.setBlockEntity(localX, localY, entity);
                }
            }
            for (int i = 0; i < Chunk.SIZE * Chunk.SIZE; i++) {
                floor[i]  = in.readInt();
                ore[i]    = in.readInt();
                object[i] = in.readInt();
                states[i] = in.readInt();
            }
            chunk.setLayers(floor, ore, object, states);
            chunk.setGenerated(true);
            in.close();
            System.out.println("[World] Загружен чанк " + chunkX + "," + chunkY);
            return chunk;
        } catch (Exception e) {
            System.out.println("[World] Ошибка загрузки: " + e.getMessage());
            return null;
        }
    }

    // Сохранить все чанки, которые изменились
    public void saveAll() {
        for (Chunk c : chunks.values()) {
            if (c.isDirty()) {
                saveChunk(c);
            }
        }
    }

    // Выгрузить чанки дальше игрока
    public void unloadDistant(float playerX, float playerY, int radius) {
        int pcx = (int) Math.floor(playerX / Chunk.SIZE);
        int pcy = (int) Math.floor(playerY / Chunk.SIZE);

        Iterator<HashMap.Entry<Long, Chunk>> it = chunks.entrySet().iterator();
        while (it.hasNext()) {
            Chunk c = it.next().getValue();
            int dx = Math.abs(c.getChunkX() - pcx);
            int dy = Math.abs(c.getChunkY() - pcy);
            if (dx > radius || dy > radius) {
                if (c.isDirty()) {
                    saveChunk(c);
                }
                it.remove();   // безопасное удаление во время обхода
            }
        }
    }

    public int getLoadedCount() {
        return chunks.size();
    }

    public BlockEntity getBlockEntityAt(int worldX, int worldY) {
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getBlockEntity(loc[2], loc[3]);
    }

    public void setBlockEntityAt(int worldX, int worldY, BlockEntity entity) {
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).setBlockEntity(loc[2], loc[3], entity);
    }

    public void removeBlockEntityAt(int worldX, int worldY) {
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).removeBlockEntity(loc[2], loc[3]);
    }

    public void tickBlockEntity() {
        for (Chunk c : chunks.values()) {
            c.tickBlockEntity();
        }
    }
}

