package org.example.world;

import org.example.block.Block;
import org.example.block.BlockEntity;
import org.example.block.Blocks;
import org.example.world.systems.GameSystems;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

public class World {

    private final HashMap<Long, Region> regions = new HashMap<>();
    private final GameSystems systems = new GameSystems();
    private final String worldName;
    private final String worldDir;

    public World(String worldName){
        this.worldName = worldName;
        this.worldDir = "saves" + File.separator + worldName;
        new File(worldDir).mkdirs();
    }

    public String getWorldName(){ return worldName; }
    public File getWorldDir(){ return new File(worldDir); }

    // ── Сид ─────────────────────────────────────────────────

    public void saveSeed(long seed){
        try{
            DataOutputStream out = new DataOutputStream(new FileOutputStream(worldDir + File.separator + "level.dat"));
            out.writeLong(seed);
            out.close();
        }catch(Exception e){
            System.out.println("[World] Ошибка level.dat: " + e.getMessage());
        }
    }

    public long loadSeed(long fallback){
        File f = new File(worldDir + File.separator + "level.dat");
        if(!f.exists()) return fallback;
        try{
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            long seed = in.readLong();
            in.close();
            return seed;
        }catch(Exception e){
            return fallback;
        }
    }

    // ── Регионы ────────────────────────────────────────────

    private long regionKey(int regionX, int regionY){
        return ((long) regionY << 32) | (regionX & 0xFFFFFFFFL);
    }

    public Region getRegion(int regionX, int regionY){
        long key = regionKey(regionX, regionY);
        Region found = regions.get(key);
        if(found != null) return found;
        found = new Region(regionX, regionY, worldName);
        found.loadFromFile();
        regions.put(key, found);
        return found;
    }

    // ── Чанки (публичный интерфейс — без изменений) ─────────

    public Chunk getChunk(int chunkX, int chunkY){
        int rx = Region.toRegionCoord(chunkX);
        int ry = Region.toRegionCoord(chunkY);
        Region region = getRegion(rx, ry);
        Chunk chunk = region.getChunk(chunkX, chunkY);
        if(chunk == null){
            chunk = new Chunk(chunkX, chunkY);
            region.putChunk(chunk);
        }
        return chunk;
    }

    // ── Доступ по мировым координатам ───────────────────────

    public int getFloorIdAt(int worldX, int worldY){
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getFloorId(loc[2], loc[3]);
    }

    public int getOreIdAt(int worldX, int worldY){
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getOreId(loc[2], loc[3]);
    }

    public int getOreAmountAt(int worldX, int worldY){
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getOreAmount(loc[2], loc[3]);
    }

    public void setOreAmountAt(int worldX, int worldY, int v){
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).setOreAmount(loc[2], loc[3], v);
    }

    public int getObjectIdAt(int worldX, int worldY){
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

    public void setFloorIdAt(int worldX, int worldY, int globalId){
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).setFloorId(loc[2], loc[3], globalId);
    }

    public void setObjectStateAt(int worldX, int worldY, int v){
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).setObjectState(loc[2], loc[3], v);
    }

    public BlockEntity getBlockEntityAt(int worldX, int worldY){
        int[] loc = toLocal(worldX, worldY);
        return getChunk(loc[0], loc[1]).getBlockEntity(loc[2], loc[3]);
    }

    public void setBlockEntityAt(int worldX, int worldY, BlockEntity entity){
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).setBlockEntity(loc[2], loc[3], entity);
    }

    public void removeBlockEntityAt(int worldX, int worldY){
        int[] loc = toLocal(worldX, worldY);
        getChunk(loc[0], loc[1]).removeBlockEntity(loc[2], loc[3]);
    }

    private int[] toLocal(int worldX, int worldY){
        int chunkX = Math.floorDiv(worldX, Chunk.SIZE);
        int chunkY = Math.floorDiv(worldY, Chunk.SIZE);
        return new int[]{
                chunkX, chunkY,
                worldX - chunkX * Chunk.SIZE,
                worldY - chunkY * Chunk.SIZE
        };
    }

    // ── Сохранение ──────────────────────────────────────────

    public void saveAll(){
        for(Region r : regions.values()){
            if(r.getChunkCount() > 0){
                r.save();
            }
        }
    }

    public void unloadDistant(int playerChunkX, int playerChunkY, int radius){
        int playerRX = Region.toRegionCoord(playerChunkX);
        int playerRY = Region.toRegionCoord(playerChunkY);
        int regionRadius = Math.floorDiv(radius, Region.REGION_SIZE) + 1;

        Iterator<HashMap.Entry<Long, Region>> it = regions.entrySet().iterator();
        while(it.hasNext()){
            Region r = it.next().getValue();
            int dx = Math.abs(r.getRegionX() - playerRX);
            int dy = Math.abs(r.getRegionY() - playerRY);
            if(dx > regionRadius || dy > regionRadius){
                if(r.getChunkCount() > 0){
                    r.save();
                }
                it.remove();
            }
        }
    }

    public int getLoadedCount(){
        int total = 0;
        for(Region r : regions.values()) total += r.getChunkCount();
        return total;
    }

    // Обход всех блок-энтити загруженных чанков (используется для sync систем)
    public void forEachLoadedBlockEntity(Consumer<BlockEntity> consumer){
        for(Region r : regions.values()){
            for(Chunk c : r.getAllChunks()){
                for(BlockEntity e : c.getAllBlockEntities().values()){
                    consumer.accept(e);
                }
            }
        }
    }

    // ── Системы ────────────────────────────────────────────────

    public void tickSystems(){
        systems.sync(this);
        systems.tick(this);
    }
}