package org.example.ui.widgets;

import org.example.core.UI;
import org.example.ui.Anchor;

import java.awt.*;

/**
 * Базовый виджет.
 *
 * Помимо координат и размеров теперь поддерживает:
 *   - Anchor (точку привязки к экрану);
 *   - offsetX/offsetY (отступ от точки привязки);
 *   - UI scale (масштаб интерфейса) при рисовании через render(g2d, screenW, screenH).
 *
 * Размеры width/height всегда хранятся в "виртуальных" (UI) пикселях.
 * Если виджет рисуется через render(g2d, screenW, screenH) — он увеличивается на UI.scale.
 * Это работает как в Minecraft: и позиции, и размеры масштабируются.
 */
public class Widget {

    /** Координаты и размеры (в виртуальных/UI пикселях). */
    protected int x, y, width, height;

    /** Виден ли виджет на экране. */
    protected boolean visible = true;

    /** Точка привязки (по умолчанию — верхний левый угол, ничего не сдвигает). */
    protected Anchor anchor = Anchor.TOP_LEFT;

    /** Отступ от точки привязки (в виртуальных пикселях). */
    protected int offsetX, offsetY;

    public Widget(int x, int y, int width, int height){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Базовая отрисовка. Подклассы переопределяют.
     * Этот метод работает в виртуальных координатах (без scale).
     */
    public void render(Graphics2D g2d) {
        // пусто по умолчанию
    }

    /**
     * Отрисовка с UI scale и автоматическим позиционированием.
     * Используется для HUD-виджетов (например хотбар).
     *
     * Алгоритм:
     *   1) Пересчитываем virtualW/virtualH под текущий scale (UI.update).
     *   2) Пересчитываем x/y из anchor+offset (updatePosition).
     *   3) Рисуем в виртуальных координатах, но с g2d.scale(UI.scale),
     *      чтобы весь виджет (и текст, и слоты) увеличился равномерно.
     */
    public void render(Graphics2D g2d, int screenW, int screenH){
        // Защита от scale < 1
        int s = UI.scale < 1 ? 1 : UI.scale;

        // 1) Виртуальные размеры экрана под текущий scale
        UI.update(screenW, screenH);

        // 2) Позиционирование по anchor/offset
        updatePosition(UI.virtualW, UI.virtualH);

        // 3) Рисуем через временную копию с применённым scale
        Graphics2D copy = (Graphics2D) g2d.create();
        copy.scale(s, s);
        render(copy);        // подклассы рисуют этим render
        copy.dispose();
    }

    /**
     * Пересчитывает x, y из anchor и offset с учётом размеров экрана.
     * Для Anchor.DRAG координаты НЕ меняются (остаются ручными).
     */
    public void updatePosition(int screenW, int screenH){
        switch (anchor) {
            case DRAG ->
                // Драг-окно: x/y уже заданы (инвентарь/сундук), ничего не делаем
                { }

            case TOP_LEFT ->
                { x = offsetX;                      y = offsetY; }

            case TOP_CENTER ->
                { x = (screenW - width)  / 2 + offsetX;  y = offsetY; }

            case TOP_RIGHT ->
                { x = screenW - width - offsetX;     y = offsetY; }

            case CENTER ->
                { x = (screenW - width)  / 2 + offsetX;
                  y = (screenH - height) / 2 + offsetY; }

            case BOTTOM_LEFT ->
                { x = offsetX;                      y = screenH - height - offsetY; }

            case BOTTOM_CENTER ->
                { x = (screenW - width) / 2 + offsetX;
                  y = screenH - height - offsetY; }

            case BOTTOM_RIGHT ->
                { x = screenW - width - offsetX;
                  y = screenH - height - offsetY; }
        }
    }

    /** Проверка попадания точки внутрь виджета (без учёта видимости). */
    public boolean contains(int mx, int my){
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    /** Попадание курсора в видимый виджет (MC: isMouseOver). */
    public boolean isMouseOver(int mx, int my){
        return visible && contains(mx, my);
    }

    /** Наведение мыши: подклассы обновляют hover-состояние. */
    public void onMouseMoved(int mx, int my){ }

    /**
     * Обработка клика (MC: mouseClicked). Возвращает true, если клик "поглощён"
     * этим виджетом и не должен передаваться дальше по цепочке (окну/миру).
     */
    public boolean onMousePressed(int mx, int my, int button){ return false; }

    // ---- Геттеры/сеттеры ----

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { this.visible = v; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    /** Задать точку привязки (chainable). */
    public Widget setAnchor(Anchor a) { this.anchor = a; return this; }
    public Anchor getAnchor() { return anchor; }

    /** Задать отступ от точки привязки (chainable). */
    public Widget setOffset(int ox, int oy) { this.offsetX = ox; this.offsetY = oy; return this; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
}
