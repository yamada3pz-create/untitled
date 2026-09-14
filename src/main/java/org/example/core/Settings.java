package org.example.core;

import java.io.*;

/**
 * Настройки игры (графика, UI scale и т.д.).
 *
 * Хранятся как статические поля, сохраняются/загружаются из файла settings.txt.
 * Формат строки: uiScale,resW,resH,fullscreen  (через запятую, по порядку).
 *
 * Пример содержимого файла:
 *   1,1000,1000,false
 */
public class Settings {

    /** Масштаб интерфейса (1x, 2x, 3x). */
    public static int uiScale = 1;

    /** Разрешение окна. */
    public static int resW = 1000;
    public static int resH = 1000;

    /** Полноэкранный режим (пока не используется, заглушка под будущее). */
    public static boolean fullscreen = false;

    /**
     * Сохраняет текущие настройки в файл settings.txt.
     * Каждое поле через запятую — чтобы просто читать обратно.
     */
    public static void save() {
        try (PrintWriter out = new PrintWriter("settings.txt")) {
            out.println(uiScale + "," + resW + "," + resH + "," + fullscreen);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает настройки из файла settings.txt.
     * Если файла нет — оставляет значения по умолчанию.
     */
    public static void load() {
        File f = new File("settings.txt");
        if (!f.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line = reader.readLine();
            if (line == null) return;
            String[] parts = line.split(",");
            if (parts.length >= 1) uiScale   = Integer.parseInt(parts[0]);
            if (parts.length >= 2) resW      = Integer.parseInt(parts[1]);
            if (parts.length >= 3) resH      = Integer.parseInt(parts[2]);
            if (parts.length >= 4) fullscreen = Boolean.parseBoolean(parts[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
