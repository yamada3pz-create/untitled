package org.example.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PerlinNoise {

    private static double seedX, seedY;
    private static double tempSeedX, tempSeedY;
    private static double humSeedX, humSeedY;
    private static double oreSeedX, oreSeedY;

    /**
     * Смещения и масштаб для каждой руды ОТДЕЛЬНО.
     * Формат: имя руды ("iron", "coal", ...) -> { offsetX, offsetY, scale }.
     * Благодаря разным offset-ам жилы разных руд НЕ совпадают друг с другом.
     * Карта универсальная: руда, которой нет в map, создастся автоматически
     * детерминированно из сида (см. getOreNoise). Очищается при setSeed.
     */
    private static final Map<String, double[]> oreSeeds = new HashMap<>();

    // Масштаб шума для конкретных известных руд (чем больше число, тем "чаще" жилы)
    private static final Map<String, Double> ORE_DEFAULT_SCALE = new HashMap<>();
    static {
        ORE_DEFAULT_SCALE.put("iron",   0.035);
        ORE_DEFAULT_SCALE.put("coal",   0.030);
        ORE_DEFAULT_SCALE.put("gold",   0.055);
        ORE_DEFAULT_SCALE.put("copper", 0.040);
    }

    public static void setSeed(long seed){
        Random r = new Random(seed);
        seedX = r.nextDouble() * 100000;
        seedY = r.nextDouble() * 100000;
        tempSeedX = r.nextDouble() * 100000; tempSeedY = r.nextDouble() * 100000;
        humSeedX  = r.nextDouble() * 100000; humSeedY  = r.nextDouble() * 100000;
        oreSeedX = r.nextDouble() * 100000; oreSeedY = r.nextDouble() * 100000;

        // Сбрасываем per-ore смещения — их пересоздадим детерминированно
        // из текущего сида при первом обращении к getOreNoise.
        oreSeeds.clear();
    }
    public static double noise(double x, double y) {
        x += seedX; y += seedY;
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x); y -= Math.floor(y);
        double u = fade(x), v = fade(y);
        int A = P[X] + Y, B = P[X+1] + Y;
        return lerp(v,
                lerp(u, grad(P[A],   x,   y), grad(P[B],   x-1, y)),
                lerp(u, grad(P[A+1], x, y-1), grad(P[B+1], x-1, y-1)));
    }

    private static double noiseAt(double x, double y, double ox, double oy) {
        x += ox; y += oy;
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x); y -= Math.floor(y);
        double u = fade(x), v = fade(y);
        int A = P[X] + Y, B = P[X+1] + Y;
        return lerp(v,
                lerp(u, grad(P[A],   x,   y), grad(P[B],   x-1, y)),
                lerp(u, grad(P[A+1], x, y-1), grad(P[B+1], x-1, y-1)));
    }

    private static double fade(double t) { return t*t*t*(t*(t*6-15)+10); }
    private static double lerp(double t, double a, double b) { return a + t*(b-a); }
    private static double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = h < 8 ? x : y;
        double v = h < 4 ? y : (h==12||h==14 ? x : 0);
        return ((h&1)==0?u:-u) + ((h&2)==0?v:-v);
    }

    private static final int[] P = new int[512];
    static {
        int[] perm = {151,160,137,91,90,15,131,13,201,95,96,53,194,233,7,225,
                140,36,103,30,69,142,8,99,37,240,21,10,23,190,6,148,247,120,234,75,0,26,
                197,62,94,252,219,203,117,35,11,32,57,177,33,88,237,149,56,87,174,20,125,
                136,171,168,68,175,74,165,71,134,139,48,27,166,77,146,158,231,83,111,229,
                122,60,211,133,230,220,105,92,41,55,46,245,40,244,102,143,54,65,25,63,161,
                1,216,80,73,209,76,132,187,208,89,18,169,200,196,135,130,116,188,159,86,
                164,100,109,198,173,186,3,64,52,217,226,250,124,123,5,202,38,147,118,126,
                255,82,85,212,207,206,59,227,47,16,58,17,182,189,28,42,223,183,170,213,
                119,248,152,2,44,165,163,121,242,203,181,205,49,138,24,49,29,181,32,154,
                199,166,137,153,146,127,127,101,176,19,51,63,9,110,135,60,221,149,116,18,
                132,45,202,33,103,179,93,81,14,5,242,72,21,110,50,70,12,22,238,26,201,
                104,253,52,182,218,180,33,4,24,56,35,64,114,211,10,121,31,144,122,31,145,
                135,185,153,249,159,8,181,114,232,147,210,54,40,111,115,224,115,42,106,139,
                121,245,145,214,158,110,235,125,180,129,242,43,215,69,144,252,201,112,105,
                132,41,202,147,178,186,167,104,114,248,246,185,117,115,221,254,149,114,131,
                234,162,112,234,157};
        for (int i = 0; i < 256; i++) P[256+i] = P[i] = perm[i];
    }

    public static double getTemperature(double x, double y) {
        return noiseAt(x, y, tempSeedX, tempSeedY);
    }

    public static double getHumidity(double x, double y) {
        return noiseAt(x, y, humSeedX, humSeedY);
    }

    public static double getFractalNoise(double x, double y) {
        double v = 0, a = 1, f = 1;
        for (int i = 0; i < 4; i++) {
            v += a * noise(x*f, y*f);
            a *= 0.5;
            f *= 2;
        }
        return v;
    }

    /**
     * Жила-подобный шум для КОНКРЕТНОЙ руды.
     * Возвращает значение в [0..1]; близкие к 0.5 значения — "ядро" жилы.
     *
     * Используются НЕЗАВИСИМЫЕ смещения на каждую руду, поэтому жилы разных
     * руд не накладываются друг на друга. Имя руды задаётся базовым
     * ("iron", "coal"), без суффикса "_ore".
     */
    public static double getOreNoise(String oreName, double x, double y) {
        double[] data = oreSeeds.get(oreName);
        if (data == null) {
            // Детерминированно создаём смещения для этой руды из базовых ore-сидов
            Random r = new Random((long) (oreSeedX + oreSeedY) + oreName.hashCode());
            double scale = ORE_DEFAULT_SCALE.containsKey(oreName)
                    ? ORE_DEFAULT_SCALE.get(oreName)
                    : 0.04;
            data = new double[]{
                    oreSeedX + r.nextDouble() * 100000,
                    oreSeedY + r.nextDouble() * 100000,
                    scale
            };
            oreSeeds.put(oreName, data);
        }
        double ox = data[0], oy = data[1], scale = data[2];

        // FBM из 3 октав с независимым смещением
        double v = 0, a = 1, f = 1;
        for (int i = 0; i < 3; i++) {
            v += a * noiseAt(x * scale * f, y * scale * f, ox, oy);
            a *= 0.5;
            f *= 2;
        }
        // Нормализуем из примерно [-1..1] в [0..1]
        return (v + 1.0) * 0.5;
    }
}
