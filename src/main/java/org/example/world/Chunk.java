package org.example.world;

import org.example.block.BlockEntity;
import org.example.block.Blocks;

import java.util.HashMap;


public class Chunk {

    public static final int SIZE = 16;

    private final int chunkX;
    private final int chunkY;

    private final int[] blockIds = new int[SIZE * SIZE];    // globalId блока
    private final int[] blockStates = new int[SIZE * SIZE];  // ID состояния

    private final HashMap<Long, BlockEntity> blockEntity = new HashMap<>();
    private boolean dirty;

    public Chunk(int chunkX,int chunkY){
        this.chunkX = chunkX;
        this.chunkY = chunkY;

        // Заполняем воздухом
        for (int i = 0; i < SIZE * SIZE; i++) {
            blockIds[i] = Blocks.AIR.getGlobalId();
            blockStates[i] = 0;
        }
    }
    // --- Доступ по локальным координатам (0-15) ---

    public int getBlockId(int localX, int localY){
        if (!inBounds(localX,localY)) return Blocks.AIR.getGlobalId();
        return blockIds[localY * SIZE + localX];
    }

    public void setBlockId(int localX, int localY, int globalId){
        if(!inBounds(localX,localY)) return;
        blockIds[localY * SIZE + localX] = globalId;
        dirty = true;
    }

    public int getBlockState(int localX, int localY) {
        if (!inBounds(localX, localY)) return 0;
        return blockStates[localY * SIZE + localX];
    }

    public void setBlockState(int localX, int localY, int state) {
        if (!inBounds(localX, localY)) return;
        blockStates[localY * SIZE + localX] = state;
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

    // --- Доступ к сырым массивам (для сохранения/загрузки) ---

    public int[] getBlockIds()    { return blockIds; }
    public int[] getBlockStates() { return blockStates; }

    public void setBlockIds(int[] ids) {
        for (int i = 0; i < SIZE * SIZE && i < ids.length; i++) {
            blockIds[i] = ids[i];
        }
        dirty = true;
    }

    public void setBlockStates(int[] states) {
        for (int i = 0; i < SIZE * SIZE && i < states.length; i++) {
            blockStates[i] = states[i];
        }
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
