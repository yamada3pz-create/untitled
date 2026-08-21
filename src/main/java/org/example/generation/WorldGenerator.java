package org.example.generation;

import org.example.block.Blocks;
import org.example.world.Chunk;

public class WorldGenerator {

    private long seed;

    public WorldGenerator(Long seed){

        this.seed = seed;
        PerlinNoise.setSeed(seed);
    }

    // Заполнить чанк блоками на основе шума
    public void generateChunk(Chunk chunk) {
        for (int y = 0; y < Chunk.SIZE; y++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                int worldX = chunk.getWorldX(x);
                int worldY = chunk.getWorldY(y);

                // Перлин-шум вместо старого хеша
                double temp  = PerlinNoise.getTemperature(worldX * 0.01, worldY * 0.01);
                double humid = PerlinNoise.getHumidity(worldX * 0.015, worldY * 0.015);

                // Приводим к [0..1]
                float tempF  = (float) ((temp + 1.0) * 0.5);
                float humidF = (float) ((humid + 1.0) * 0.5);

                int blockId = selectBlock(tempF, humidF);
                chunk.setBlockId(x, y, blockId);
            }
        }
    }

    // Выбор блока по температуре и влажности
    private int selectBlock(float temp, float humid) {
        if (temp > 0.6f && humid < 0.4f) {
            return Blocks.SAND.getGlobalId();
        }
        if (temp < 0.3f) {
            return Blocks.STONE.getGlobalId();
        }
        return Blocks.GRASS.getGlobalId();
    }
    // --- Простой шум ---

    // Возвращает число от 0.0 до 1.0 для координат (x, y)
    private float getNoise(double x, double y, int offset) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        double fx = x - ix;
        double fy = y - iy;

        // Значения в четырёх углах клетки
        float v00 = hash(ix,     iy,     offset);
        float v10 = hash(ix + 1, iy,     offset);
        float v01 = hash(ix,     iy + 1, offset);
        float v11 = hash(ix + 1, iy + 1, offset);

        // Плавное среднее между ними
        float v0 = v00 + (float)(fx * (v10 - v00));
        float v1 = v01 + (float)(fx * (v11 - v01));
        return v0 + (float)(fy * (v1 - v0));
    }

    // Превращает координаты (x, y) в число от 0.0 до 1.0
    private float hash(int x, int y, int offset) {
        long n = (long)(x * 374761393 + y * 668265263 + seed * 1274126177 + offset * 1000003);
        n = (n ^ (n >> 13)) * 1274126177;
        n = n ^ (n >> 16);
        return (float)((n & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL);
    }
}
