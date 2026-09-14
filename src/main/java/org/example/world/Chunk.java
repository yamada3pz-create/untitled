package org.example.world;

import org.example.block.BlockEntity;
import org.example.block.Blocks;

import java.util.HashMap;

public class Chunk {

    public static final int SIZE = 16;

    private final int chunkX;
    private final int chunkY;

    private final int[] floorIds     = new int[SIZE * SIZE];
    private final int[] oreIds       = new int[SIZE * SIZE];
    private final int[] objectIds    = new int[SIZE * SIZE];
    private final int[] objectStates = new int[SIZE * SIZE];
    private final int[] oreAmount    = new int[SIZE * SIZE];

    private final HashMap<Long, BlockEntity> blockEntity = new HashMap<>();
    private boolean dirty;
    private boolean generated;

    public Chunk(int chunkX, int chunkY){
        this.chunkX = chunkX;
        this.chunkY = chunkY;
    }

    // ── Floor ──

    public int getFloorId(int lx, int ly){
        if(!inBounds(lx,ly)) return Blocks.AIR.getGlobalId();
        return floorIds[ly * SIZE + lx];
    }
    public void setFloorId(int lx, int ly, int v){
        if(!inBounds(lx,ly)) return;
        floorIds[ly * SIZE + lx] = v;
        dirty = true;
    }

    // ── Ore ──

    public int getOreId(int lx, int ly){
        if(!inBounds(lx,ly)) return Blocks.AIR.getGlobalId();
        return oreIds[ly * SIZE + lx];
    }
    public void setOreId(int lx, int ly, int v){
        if(!inBounds(lx,ly)) return;
        oreIds[ly * SIZE + lx] = v;
        dirty = true;
    }

    // ── Ore Amount (новое) ──

    public int getOreAmount(int lx, int ly){
        if(!inBounds(lx,ly)) return 0;
        return oreAmount[ly * SIZE + lx];
    }
    public void setOreAmount(int lx, int ly, int v){
        if(!inBounds(lx,ly)) return;
        oreAmount[ly * SIZE + lx] = v;
        dirty = true;
    }

    // ── Object ──

    public int getObjectId(int lx, int ly){
        if(!inBounds(lx,ly)) return Blocks.AIR.getGlobalId();
        return objectIds[ly * SIZE + lx];
    }
    public void setObjectId(int lx, int ly, int v){
        if(!inBounds(lx,ly)) return;
        objectIds[ly * SIZE + lx] = v;
        dirty = true;
    }

    // ── Object State ──

    public int getObjectState(int lx, int ly){
        if(!inBounds(lx,ly)) return 0;
        return objectStates[ly * SIZE + lx];
    }
    public void setObjectState(int lx, int ly, int v){
        if(!inBounds(lx,ly)) return;
        objectStates[ly * SIZE + lx] = v;
        dirty = true;
    }

    // ── Координаты ──

    public int getChunkX() { return chunkX; }
    public int getChunkY() { return chunkY; }
    public int getWorldX(int lx) { return chunkX * SIZE + lx; }
    public int getWorldY(int ly) { return chunkY * SIZE + ly; }

    // ── Статус ──

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean v) { dirty = v; }
    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean v) { generated = v; }

    // ── Массивы для сохранения ──

    public int[] getFloorIds()     { return floorIds; }
    public int[] getOreIds()       { return oreIds; }
    public int[] getObjectIds()    { return objectIds; }
    public int[] getObjectStates() { return objectStates; }
    public int[] getOreAmounts()   { return oreAmount; }

    public void setLayers(int[] floor, int[] ore, int[] object, int[] states){
        for(int i = 0; i < SIZE * SIZE; i++){
            floorIds[i]     = floor[i];
            oreIds[i]       = ore[i];
            objectIds[i]    = object[i];
            objectStates[i] = states[i];
        }
        dirty = true;
    }

    public void setOreAmounts(int[] amounts){
        for(int i = 0; i < SIZE * SIZE; i++){
            oreAmount[i] = amounts[i];
        }
        dirty = true;
    }

    private boolean inBounds(int x, int y){
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    // ── Block Entities ──

    public BlockEntity getBlockEntity(int lx, int ly){
        if(!inBounds(lx,ly)) return null;
        return blockEntity.get(posKey(lx,ly));
    }
    public void setBlockEntity(int lx, int ly, BlockEntity e){
        if(!inBounds(lx,ly)) return;
        blockEntity.put(posKey(lx,ly), e);
        dirty = true;
    }
    public void removeBlockEntity(int lx, int ly){
        if(!inBounds(lx,ly)) return;
        blockEntity.remove(posKey(lx,ly));
        dirty = true;
    }
    public int getBlockEntityCount(){ return blockEntity.size(); }
    public HashMap<Long, BlockEntity> getAllBlockEntities(){ return blockEntity; }

    private long posKey(int x, int y){
        return ((long) y << 16 | (long) x);
    }
}