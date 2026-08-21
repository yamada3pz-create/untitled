package org.example.ui;

import java.awt.*;
import java.util.ArrayList;

public class GuiWindow {

    protected int x, y, width, height;
    protected String title;
    protected boolean visible;
    protected boolean dragging;
    protected int dragOffsetX, dragOffsetY;

    protected ArrayList<Widget> widgets;
    protected int titleBarHeight = 24;

    public GuiWindow(int x, int y, int width, int height, String title){

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.title = title;
        this.visible = false;
        this.dragging = false;
        this.widgets = new ArrayList<>();
    }


    public void open() { visible = true; }
    public void close() { visible = false; }
    public boolean isOpen() { return visible; }

    public void addWidget (Widget w) { widgets.add(w); }

    // --- Ввод ---

    public void mousePressed(int mx, int my){
        if(!visible) return;

        // Проверяем заголовок для dragg
        if(mx >= x && mx <= x + width && my >= y && my <= y + titleBarHeight){
            dragging = true;
            dragOffsetX = mx - x;
            dragOffsetY = my - y;
        }

        // Клик по виджетам
        for( int i = 0; i < widgets.size(); i++){
            Widget w = widgets.get(i);
            if(w instanceof ButtonWidget){
                ButtonWidget btn = (ButtonWidget) w;
                if(btn.contains(mx, my)){
                    btn.click();
                }
            }
        }
    }

    public void mouseDragged(int mx, int my) {
        if (!visible || !dragging) return;
        x = mx - dragOffsetX;
        y = my - dragOffsetY;
    }

    public void mouseReleased(int mx, int my) {
        dragging = false;
    }

    public void mouseMoved(int mx, int my) {
        if (!visible) return;
        // Hover для кнопок
        for (int i = 0; i < widgets.size(); i++) {
            Widget w = widgets.get(i);
            if (w instanceof ButtonWidget) {
                ButtonWidget btn = (ButtonWidget) w;
                btn.setHovered(btn.contains(mx, my));
            }
        }
    }

    // --- Отрисовка ---

    public void render(Graphics2D g2d) {
        if (!visible) return;

        // Заголовок
        g2d.setColor(new Color(50, 50, 80));
        g2d.fillRect(x, y, width, titleBarHeight);
        g2d.setColor(new Color(80, 80, 120));
        g2d.drawRect(x, y, width, titleBarHeight);

        // Текст заголовка
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(title, x + 8, y + 18);

        // Кнопка закрытия (X)
        int cx = x + width - 20;
        int cy = y + 4;
        g2d.setColor(new Color(180, 50, 50));
        g2d.fillRect(cx, cy, 16, 16);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("X", cx + 4, cy + 13);

        // Фон содержимого
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(x, y + titleBarHeight, width, height - titleBarHeight);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);

        // Виджеты
        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).render(g2d);
        }
    }


}
