package org.example.world;

import org.example.PlayingState;
import org.example.block.BlockEntity;
import org.example.block.Blocks;

import java.util.HashMap;


public class Chunk {

    public static final int SIZE = 16;

    private final int chunkX;
    private final int chunkY;

    private final int[] floorIds    = new int[SIZE * SIZE];
    private final int[] oreIds      = new int[SIZE * SIZE];
    private final int[] objectIds    = new int[SIZE * SIZE];  // globalId блока
    private final int[] objectStates = new int[SIZE * SIZE];  // ID состояния



    private final HashMap<Long, BlockEntity> blockEntity = new HashMap<>();
    private boolean dirty;
    private boolean generated; // Заполнение ли

    public Chunk(int chunkX,int chunkY){
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    // --- Пол ---
    public int getFloorId(int localX, int localY){
        if(!inBounds(localX,localY)) return Blocks.AIR.getGlobalId();
        return floorIds[localY * SIZE + localX];
    }
    public void setFloorId(int localX, int localY, int globalId){
        if(!inBounds(localX,localY)) return;
        floorIds[localY * SIZE + localX] = globalId;
        dirty = true;
    }

    // --- Руда ---
    public int getOreId(int localX, int localY){
        if(!inBounds(localX,localY)) return Blocks.AIR.getGlobalId();
        return oreIds[localY * SIZE + localX];
    }
    public void setOreId(int localX, int localY, int globalId){
        if(!inBounds(localX,localY)) return;
        oreIds[localY * SIZE + localX] = globalId;
        dirty = true;
    }

    // --- Объекты ---

    public int getObjectId(int localX, int localY){
        if(!inBounds(localX,localY)) return Blocks.AIR.getGlobalId();
        return objectIds[localY * SIZE + localX];
    }
    public void setObjectId(int localX, int localY, int globalId){
        if(!inBounds(localX,localY)) return;
        objectIds[localY * SIZE + localX] = globalId;
        dirty = true;
    }

    public int getObjectState(int localX, int localY){
        if(!inBounds(localX,localY)) return 0;
        return objectStates[localY * SIZE + localX];
    }
    public void setObjectState(int localX, int localY, int state){
        if(!inBounds(localX,localY)) return;
        objectStates[localY * SIZE + localX] = state;
        dirty = true;
    }

    // --- Координаты ---

    public int getChunkX() { return chunkX; }
    public int getChunkY() { return chunkY; }

    public int getWorldX(int localX) { return chunkX * SIZE + localX; }
    public int getWorldY(int localY) { return chunkY * SIZE + localY; }

    // --- Статус ---

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean gen) { this.generated = gen; }

    // --- Сырые массивы (для сохранения/загрузки) ---

    public int[] getFloorIds()     { return floorIds; }
    public int[] getOreIds()       { return oreIds; }
    public int[] getObjectIds()    { return objectIds; }
    public int[] getObjectStates() { return objectStates; }

    public void setLayers(int[] floor, int[] ore,  int[] object, int[] states){
        for (int i = 0; i < SIZE * SIZE; i++) {
            floorIds[i]     = floor[i];
            oreIds[i]       = ore[i];
            objectIds[i]    = object[i];
            objectStates[i] = states[i];
        }
        dirty = true;
    }

    // --- Утилита ---

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    // --- BlockEntity методы ---

    public BlockEntity getBlockEntity (int localX, int localY){
        if(!inBounds(localX, localY)) return null;
        long key = posKey(localX,localY);
        return blockEntity.get(key);
    }

    public void setBlockEntity(int localX, int localY, BlockEntity entity){
        if(!inBounds(localX, localY)) return;
        long key = posKey(localX,localY);
        blockEntity.put(key, entity);
        dirty = true;
    }

    public void removeBlockEntity(int localX, int localY){
        if(!inBounds(localX, localY)) return;
        long key = posKey(localX,localY);
        blockEntity.remove(key);
        dirty = true;
    }

    // Количество BlockEntity в чанке
    public int getBlockEntityCount(){
        return blockEntity.size();
    }

    // Все BlockEntity (для сохранения)
    public HashMap<Long, BlockEntity> getAllBlockEntities(){
        return blockEntity;
    }

    // Вызывает tick() у всех BlockEntity в чанке
    public void tickBlockEntity() {
        for(BlockEntity entity : blockEntity.values()){
            entity.tick();
        }
    }

    // Ключ позиции: два int -> один long
    private long posKey(int x, int y){
        return ((long) y << 16 | (long) x);
    }
}
