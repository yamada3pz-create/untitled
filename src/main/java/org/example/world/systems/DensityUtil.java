package org.example.world.systems;

import org.example.generation.PerlinNoise;

// Плотность жилы [0.1..1.0] для бесконечной руды (ТЗ раздел 4).
// Вычисляется детерминированно из того же шума, что и генерация, и не хранится,
// поэтому добыча не помечает чанк dirty и руда не тратится.
public final class DensityUtil {

    private DensityUtil(){}

    /** Плотность жилы в клетке (wx, wy) для блока руды [0.1..1.0]. */
    public static double oreDensity(String oreBlockId, int wx, int wy){
        String base = oreBlockId.endsWith("_ore")
                ? oreBlockId.substring(0, oreBlockId.length() - 4)
                : oreBlockId;
        // Жила-шум [0..1], ядро жилы ≈ 0.5
        double n = PerlinNoise.getOreNoise(base, wx, wy);
        // 0 у краёв жилы, 1 в самом центре
        double factor = 1.0 - Math.min(1.0, Math.abs(n - 0.5) * 2.0);
        return 0.1 + 0.9 * factor;
    }
}