package org.example.generation;

import org.example.block.Blocks;
import org.example.world.Chunk;

public class WorldGenerator {

    private long seed;

    public WorldGenerator(Long seed){
        this.seed = seed;
        PerlinNoise.setSeed(seed);
    }

    public void generateChunk(Chunk chunk) {
        for (int y = 0; y < Chunk.SIZE; y++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                int wx = chunk.getWorldX(x);
                int wy = chunk.getWorldY(y);

                // Пол по температуре и влажности
                double temp  = PerlinNoise.getTemperature(wx * 0.01, wy * 0.01);
                double humid = PerlinNoise.getHumidity(wx * 0.015, wy * 0.015);
                float tempF  = (float) ((temp + 1.0) * 0.5);
                float humidF = (float) ((humid + 1.0) * 0.5);
                chunk.setFloorId(x, y, selectFloor(tempF, humidF));

                // Руда: отдельный шум, редкие пятна выше порога
                double ore = PerlinNoise.getOre(wx * 0.11, wy * 0.11);
                float oreF = (float) ((ore + 1.0) * 0.5);
                if (oreF > 0.72f) {
                    chunk.setOreId(x, y, selectOre(wx, wy));
                }

                // Валуны: очень редко, не поверх руды
                if (chunk.getOreId(x, y) == Blocks.AIR.getGlobalId()
                        && hash(wx, wy, 777) < 0.02f) {
                    chunk.setObjectId(x, y, Blocks.WALL.getGlobalId());
                }
            }
        }
        chunk.setGenerated(true);
    }

    private int selectFloor(float temp, float humid) {
        if (temp > 0.6f && humid < 0.4f) return Blocks.SAND.getGlobalId();
        if (temp < 0.3f)                 return Blocks.STONE.getGlobalId();
        return Blocks.GRASS.getGlobalId();
    }

    private int selectOre(int wx, int wy) {
        // Тип руды по стабильному хешу координат: медь чуть чаще железа
        return hash(wx, wy, 555) < 0.55f
                ? Blocks.IRON_ORE.getGlobalId()
                : Blocks.COPPER_ORE.getGlobalId();
    }

    // Детерминированный хеш точки в [0..1], зависит от seed
    private float hash(int x, int y, int offset) {
        long n = (long)(x * 374761393L + y * 668265263L + seed * 1274126177L + offset * 1000003L);
        n = (n ^ (n >> 13)) * 1274126177L;
        n = n ^ (n >> 16);
        return (float)((n & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL);
    }
}
