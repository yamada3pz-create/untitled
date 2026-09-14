package org.example.core;

/**
 * Глобальные UI-настройки (аналог GuiSettings в Minecraft).
 *
 * Здесь хранится МАСШТАБ интерфейса (UI Scale) и "виртуальные" размеры экрана.
 *
 * Идея как в Minecraft:
 *  - Все HUD-виджеты живут в "виртуальных" координатах (UI-пикселях).
 *  - При scale > 1 весь интерфейс просто увеличивается (и позиции, и размеры).
 *  - Благодаря этому масштабирование не ломает логику виджетов.
 *
 * Пример: экран 1000x1000, scale = 2.
 *   virtualW = 500, virtualH = 500.
 *   Хотбар с width=320 рисуется реально 640px и центрируется по виртуальным 500.
 */
public class UI {

    /** Максимально допустимый масштаб: при окне 1000x1000 больше не влезает без прокрутки. */
    public static final int MAX_SCALE = 2;

    /** Масштаб интерфейса (1x, 2x). Заполняется из Settings.uiScale при старте. */
    public static int scale = 1;

    /**
     * "Виртуальные" размеры экрана.
     * Фактический экран ДЕЛИТСЯ на scale, чтобы виджеты оперировали
     * в координатах, которые потом умножаются обратно при отрисовке.
     */
    public static int virtualW = 1000;
    public static int virtualH = 1000;

    /** Масштабировать число из "виртуальных" пикселей в реальные (px * scale). */
    public static int scale(int px) {
        return px * scale;
    }

    /** Обратное преобразование: реальные пиксели -> "виртуальные" (px / scale). */
    public static int unscaled(int px) {
        return px / scale;
    }

    /**
     * Вызывается каждый кадр перед отрисовкой HUD.
     * Пересчитывает virtualW/virtualH под текущий scale.
     */
    public static void update(int screenW, int screenH) {
        // Делим на scale. Если scale<1 (защита от деления на ноль) — берём 1.
        int s = scale < 1 ? 1 : scale;
        virtualW = screenW / s;
        virtualH = screenH / s;
    }
}
