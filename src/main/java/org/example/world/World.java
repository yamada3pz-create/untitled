package org.example.world;

import org.example.block.BlockEntityType;
import org.example.block.Block;
import org.example.block.BlockEntity;
import org.example.block.Blocks;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class World {

    // Список загруженных чанков ( как массив, но размер растет сам)
    private ArrayList<Chunk> chunks = new ArrayList<>();
    private String savePath;

    public World(String worldName){
        this.savePath = "saves" + File.separator + worldName + File.separator + "chunk" + File.separator;
        new File(savePath).mkdirs();
    }

    // --- Получаем чанки ---

    // Найти или создать чанки по координатам чанка
    public Chunk getChunk(int chunkX, int chunkY){
        // Ищем среди загруженных
        for(int i = 0; i < chunks.size(); i++){
            Chunk c = chunks.get(i);
            if(c.getChunkX() == chunkX && c.getChunkY() == chunkY){
                return c;
            }
        }

        // Пробуем загрузить из файла
        Chunk loader = loadChunk(chunkX, chunkY);
        if(loader != null){
            chunks.add(loader);
            return loader;
        }
        // Файла нет - создает пустой
        Chunk newChunk = new Chunk(chunkX, chunkY);
        chunks.add(newChunk);
        return newChunk;
    }

    // --- Доступ по мировым координатам ---

    public int getBlockIdAt(int worldX, int worldY) {
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE);
        int chunkY = Math.floorDiv(worldY, Chunk.SIZE);
        int localX = worldX - chunkX * Chunk.SIZE;
        int localY = worldY - chunkY * Chunk.SIZE;
        return getChunk(chunkX, chunkY).getBlockId(localX, localY);
    }

    public void setBlockIdAt(int worldX, int worldY, int globalId){
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE);
        int chunkY = Math.floorDiv(worldY, Chunk.SIZE);
        int localX = worldX - chunkX * Chunk.SIZE;
        int localY = worldY - chunkY * Chunk.SIZE;
        getChunk(chunkX,chunkY).setBlockId(localX, localY, globalId);
    }

    public Block getBlockAt(int worldX, int worldY) {
        return Blocks.get(getBlockIdAt(worldX, worldY));
    }

    // --- Сохранение и загрузка ---

    public void saveChunk (Chunk chunk) {
        String path = savePath + "c." + chunk.getChunkX() + "." + chunk.getChunkY() + ".dat";
        try{
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path));
            int[] ids = chunk.getBlockIds();
            int[] states = chunk.getBlockStates();

            int entityCount = chunk.getBlockEntityCount();
            out.writeInt(entityCount);

            HashMap<Long, BlockEntity> entities = chunk.getAllBlockEntities();
            for (Map.Entry<Long, BlockEntity> entry : entities.entrySet()){
                long key = entry.getKey();
                BlockEntity entity = entry.getValue();

                int localX = (int) (key & 0xFFFF);
                int localY = (int) (key >> 16);

                out.writeInt(localX);
                out.writeInt(localY);
                out.writeUTF(entity.getType());
                entity.save(out);
            }
            for (int i = 0; i < Chunk.SIZE * Chunk.SIZE; i++) {
                out.writeInt(ids[i]);
                out.writeInt(states[i]);

            }
            out.close();
            System.out.println("[World] Сохранение чанка " + chunk.getChunkX() + "," + chunk.getChunkY());
        }catch (Exception e){
            System.out.println("[World] Ошибка сохранения: " + e.getMessage());
        }
    }

    // Загрузка чанка из файла (возвращает null если файлы нет)
    public Chunk loadChunk(int chunkX, int chunkY){
        String path = savePath + "c." + chunkX + "." + chunkY + ".dat";
        File file = new File(path);
        if(!file.exists()){
            return null;
        }
        try {
            Chunk chunk = new Chunk(chunkX,chunkY);
            DataInputStream in = new DataInputStream(new FileInputStream(path));
            int[] ids = new int[Chunk.SIZE * Chunk.SIZE];
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
                ids[i] = in.readInt();
                states[i] = in.readInt();
            }
            chunk.setBlockIds(ids);
            chunk.setBlockStates(states);
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
        for (int i = 0; i < chunks.size(); i++) {
            Chunk c = chunks.get(i);
            if (c.isDirty()) {
                saveChunk(c);
            }
        }
    }

    // Выгрузить чанки дальше игрока
    public void unloadDistant(float playerX, float playerY, int radius) {
        int pcx = (int) Math.floor(playerX / Chunk.SIZE);
        int pcy = (int) Math.floor(playerY / Chunk.SIZE);

        ArrayList<Chunk> toRemove = new ArrayList<Chunk>();
        for (int i = 0; i < chunks.size(); i++) {
            Chunk c = chunks.get(i);
            int dx = Math.abs(c.getChunkX() - pcx);
            int dy = Math.abs(c.getChunkY() - pcy);
            if (dx > radius || dy > radius) {
                if (c.isDirty()) {
                    saveChunk(c);
                }
                toRemove.add(c);
            }
        }
        chunks.removeAll(toRemove);
    }

    public int getLoadedCount() {
        return chunks.size();
    }

    public BlockEntity getBlockEntityAt(int worldX, int worldY) {
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE);
        int chunkY = Math.floorDiv(worldY, Chunk.SIZE);
        int localX = worldX - chunkX * Chunk.SIZE;
        int localY = worldY - chunkY * Chunk.SIZE;
        return getChunk(chunkX, chunkY).getBlockEntity(localX, localY);
    }
    // Вызывает tick() у всех BlockEntity в чанке
    public void tickBlockEntity() {
        for(int i = 0; i < chunks.size(); i++){
            chunks.get(i).tickBlockEntity();
        }
    }
}
