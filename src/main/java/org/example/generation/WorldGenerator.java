package org.example.generation;

import org.example.block.Block;
import org.example.block.Blocks;
import org.example.world.Chunk;

import java.util.Random;

public class WorldGenerator {

    private long seed;
    private final Random random = new Random();

    public WorldGenerator(Long seed){
        this.seed = seed;
        PerlinNoise.setSeed(seed);
    }

    public void generateChunk(Chunk chunk) {
        for (int y = 0; y < Chunk.SIZE; y++) {
            for (int x = 0; x < Chunk.SIZE; x++) {
                int wx = chunk.getWorldX(x);
                int wy = chunk.getWorldY(y);

                // Климат → биом
                double temp  = PerlinNoise.getTemperature(wx * 0.01, wy * 0.01);
                double humid = PerlinNoise.getHumidity(wx * 0.015, wy * 0.015);
                float tempF  = (float) ((temp + 1.0) * 0.5);
                float humidF = (float) ((humid + 1.0) * 0.5);

                Biome biome = Biomes.matchBiome(tempF, humidF);

                // Пол из биома
                Block surfaceBlock = Blocks.getByName(biome.getSurfaceBlock());
                chunk.setFloorId(x, y, surfaceBlock.getGlobalId());

                // Руды из биома — жильная генерация (как в v0.2)
                for (Biome.OreConfig ore : biome.getOres()) {
                    // Нормализуем имя: блок "iron_ore" -> базовое "iron" для per-ore шума
                    String oreName = stripOreSuffix(ore.getBlockId());

                    // Жила-шум конкретной руды [0..1] (независимые жилы на каждую руду)
                    double oreNoise = PerlinNoise.getOreNoise(oreName, wx, wy);

                    // FBM-маска редкости [0..1] — чтобы жилы шли не сплошным полем
                    double rawMask = PerlinNoise.getFractalNoise(wx * 0.01, wy * 0.01);
                    double mask = (rawMask + 1.0) * 0.5;

                    // Условие жилы: близко к центру шума И достаточно редкая маска
                    if (Math.abs(oreNoise - 0.5) < ore.getWidth() && mask > ore.getRarity()) {
                        Block oreBlock = Blocks.getByName(ore.getBlockId());
                        if (oreBlock != Blocks.AIR) {
                            chunk.setOreId(x, y, oreBlock.getGlobalId());

                            // Количество: чем ближе к центру жилы — тем больше
                            double factor = (ore.getWidth() - Math.abs(oreNoise - 0.5)) / ore.getWidth();
                            int amount = ore.getMinCount() + (int)(factor * (ore.getMaxCount() - ore.getMinCount()));
                            chunk.setOreAmount(x, y, Math.max(ore.getMinCount(), Math.min(amount, ore.getMaxCount())));
                        }
                        break; // Один тайл — максимум один тип руды
                    }
                }

                // Валуны: очень редко, не поверх руды
                if (chunk.getOreId(x, y) == Blocks.AIR.getGlobalId()
                        && hash(wx, wy, 777) < 0.02f) {
                    Block wall = Blocks.getByName("wall");
                    chunk.setObjectId(x, y, wall.getGlobalId());
                }
            }
        }
        chunk.setGenerated(true);
    }

    /**
     * Убирает суффикс "_ore" из имени блока руды, чтобы получить базовое имя
     * для пер-ой шума. Пример: "iron_ore" -> "iron", "gold_ore" -> "gold".
     */
    private static String stripOreSuffix(String blockId){
        if(blockId != null && blockId.endsWith("_ore")){
            return blockId.substring(0, blockId.length() - 4); // отрезаем "_ore"
        }
        return blockId;
    }

    private float hash(int x, int y, int offset) {
        long n = (long)(x * 374761393L + y * 668265263L + seed * 1274126177L + offset * 1000003L);
        n = (n ^ (n >> 13)) * 1274126177L;
        n = n ^ (n >> 16);
        return (float)((n & 0x7FFFFFFFL) / (double) 0x7FFFFFFFL);
    }
}