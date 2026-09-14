package org.example.generation;

import java.util.ArrayList;
import java.util.List;

public class Biome {

    public static class OreConfig {
        private final String blockId;
        private final float rarity;
        private final float width;
        private final int minCount;
        private final int maxCount;

        public OreConfig(String blockId, float rarity, float width, int minCount, int maxCount){
            this.blockId = blockId;
            this.rarity = rarity;
            this.width = width;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
        public String getBlockId()  { return blockId; }
        public float getRarity()    { return rarity; }
        public float getWidth()     { return width; }
        public int getMinCount()    { return minCount; }
        public int getMaxCount()    { return maxCount; }
    }

    private final String id;
    private final float minTemp;
    private final float maxTemp;
    private final float minHumid;
    private final float maxHumid;
    private final String surfaceBlock;
    private final List<OreConfig> ores;

    private Biome(String id, float minTemp, float maxTemp, float minHumid, float maxHumid, String surfaceBlock, List<OreConfig> ores){
        this.id = id;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minHumid = minHumid;
        this.maxHumid = maxHumid;
        this.surfaceBlock = surfaceBlock;
        this.ores = ores;
    }

    public String getId()           { return id;}
    public float getMinTemp()       { return minTemp; }
    public float getMaxTemp()       { return maxTemp; }
    public float getMinHumid()      { return minHumid; }
    public float getMaxHumid()      { return maxHumid; }
    public String getSurfaceBlock() { return surfaceBlock; }
    public List<OreConfig> getOres(){ return ores; }

    public boolean matches(float temp, float humid){
        return temp >= minTemp && temp <= maxTemp && humid >= minHumid && humid <= maxHumid;
    }

    public static Builder create(String id){
        return new Builder(id);
    }

    public static class Builder{
        private final String id;
        private float minTemp = 0f, maxTemp = 1f;
        private float minHumid = 0f, maxHumid = 1f;
        private String surfaceBlock = "grass";
        private final  List<OreConfig> ores = new ArrayList<>();

        private Builder(String id){
            this.id = id;
        }
        public Builder temperature(float min, float max){ this.minTemp = min; this.maxTemp = max; return this; }
        public Builder humidity(float min, float max){ this.minHumid = min; this.maxHumid = max; return this; }
        public Builder surfaceBlock(String v){ this.surfaceBlock = v; return this; }
        public Builder ore(String blockId, float rarity, float width, int minCount, int maxCount){
            ores.add(new OreConfig(blockId, rarity, width, minCount, maxCount));
            return this;
        }

        public Biome build(){
            return new Biome(id, minTemp, maxTemp, minHumid, maxHumid, surfaceBlock, ores);
        }
    }
}
