package org.example.world;

import org.example.block.BlockEntity;
import org.example.block.BlockEntityType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Region {

    public static final int REGION_SIZE = 16;

    private final int regionX;
    private final int regionY;
    private final String worldName;
    private final String savePath;

    private final HashMap<String, Chunk> chunks = new HashMap<>();

    public Region(int regionX, int regionY, String worldName){
        this.regionX = regionX;
        this.regionY = regionY;
        this.worldName = worldName;
        this.savePath = "saves" + File.separator + worldName + File.separator + "regions" + File.separator;
        new File(savePath).mkdirs();
    }

    public int getRegionX() { return regionX; }
    public int getRegionY() { return regionY; }

    public Chunk getChunk(int chunkX, int chunkY){
        String key = chunkKey(chunkX, chunkY);
        Chunk found = chunks.get(key);
        if(found != null) return found;
        return null;
    }

    public Chunk getOrCreateChunk(int chunkX, int chunkY){
        String key = chunkKey(chunkX, chunkY);
        Chunk found = chunks.get(key);
        if(found != null) return found;
        Chunk chunk = new Chunk(chunkX, chunkY);
        chunks.put(key, chunk);
        return chunk;
    }

    public void putChunk(Chunk chunk){
        chunks.put(chunkKey(chunk.getChunkX(), chunk.getChunkY()), chunk);
    }

    public void removeChunk(int chunkX, int chunkY){
        chunks.remove(chunkKey(chunkX, chunkY));
    }

    public int getChunkCount(){ return chunks.size(); }

    // ── Сохранение ──────────────────────────────────────────

    public void save(){
        String path = savePath + "r." + regionX + "." + regionY + ".dat";
        try{
            DataOutputStream out = new DataOutputStream(new FileOutputStream(path));

            out.writeInt(chunks.size());
            for(Chunk chunk : chunks.values()){
                out.writeInt(chunk.getChunkX());
                out.writeInt(chunk.getChunkY());

                // Block entities
                out.writeInt(chunk.getBlockEntityCount());
                for(Map.Entry<Long, BlockEntity> entry : chunk.getAllBlockEntities().entrySet()){
                    long key = entry.getKey();
                    BlockEntity entity = entry.getValue();
                    out.writeInt((int)(key & 0xFFFF));
                    out.writeInt((int)(key >> 16));
                    out.writeUTF(entity.getType());
                    entity.save(out);
                }

                // Tile layers
                int[] floor  = chunk.getFloorIds();
                int[] ore    = chunk.getOreIds();
                int[] object = chunk.getObjectIds();
                int[] states = chunk.getObjectStates();
                int[] amounts = chunk.getOreAmounts();
                for(int i = 0; i < Chunk.SIZE * Chunk.SIZE; i++){
                    out.writeInt(floor[i]);
                    out.writeInt(ore[i]);
                    out.writeInt(object[i]);
                    out.writeInt(states[i]);
                    out.writeInt(amounts[i]);
                }
                chunk.setDirty(false);
            }
            out.close();
            System.out.println("[Region] Сохранён регион " + regionX + "," + regionY + " (" + chunks.size() + " чанков)");
        }catch(Exception e){
            System.out.println("[Region] Ошибка сохранения: " + e.getMessage());
        }
    }

    // ── Загрузка ────────────────────────────────────────────

    public boolean loadFromFile(){
        String path = savePath + "r." + regionX + "." + regionY + ".dat";
        File file = new File(path);
        if(!file.exists()) return false;

        try{
            DataInputStream in = new DataInputStream(new FileInputStream(file));

            int count = in.readInt();
            for(int c = 0; c < count; c++){
                int cx = in.readInt();
                int cy = in.readInt();
                Chunk chunk = new Chunk(cx, cy);

                // Block entities
                int entityCount = in.readInt();
                for(int i = 0; i < entityCount; i++){
                    int localX = in.readInt();
                    int localY = in.readInt();
                    String type = in.readUTF();
                    BlockEntity entity = BlockEntityType.create(type, chunk.getWorldX(localX), chunk.getWorldY(localY));
                    if(entity != null){
                        entity.load(in);
                        chunk.setBlockEntity(localX, localY, entity);
                    }
                }

                // Tile layers
                int[] floor  = new int[Chunk.SIZE * Chunk.SIZE];
                int[] ore    = new int[Chunk.SIZE * Chunk.SIZE];
                int[] object = new int[Chunk.SIZE * Chunk.SIZE];
                int[] states = new int[Chunk.SIZE * Chunk.SIZE];
                int[] amounts = new int[Chunk.SIZE * Chunk.SIZE];

                for(int i = 0; i < Chunk.SIZE * Chunk.SIZE; i++){
                    floor[i]  = in.readInt();
                    ore[i]    = in.readInt();
                    object[i] = in.readInt();
                    states[i] = in.readInt();
                    try {
                        amounts[i] = in.readInt();
                    } catch (Exception ignored) {
                        amounts[i] = 0;
                    }
                }
                chunk.setLayers(floor, ore, object, states);
                chunk.setOreAmounts(amounts);
                chunk.setGenerated(true);
                chunks.put(chunkKey(cx, cy), chunk);
            }
            in.close();
            System.out.println("[Region] Загружен регион " + regionX + "," + regionY + " (" + count + " чанков)");
            return true;
        }catch(Exception e){
            System.out.println("[Region] Ошибка загрузки: " + e.getMessage());
            return false;
        }
    }

    // ── Утилиты ────────────────────────────────────────────

    public Iterable<Chunk> getAllChunks(){ return chunks.values(); }

    public static int toRegionCoord(int chunkCoord){
        return Math.floorDiv(chunkCoord, REGION_SIZE);
    }

    private String chunkKey(int chunkX, int chunkY){
        return chunkX + "," + chunkY;
    }
}