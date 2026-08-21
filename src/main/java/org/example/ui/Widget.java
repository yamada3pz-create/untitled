package org.example.ui;

import java.awt.*;

public class Widget {

    protected int x, y, width, height;
    protected boolean visible = true;

    public Widget(int x, int y, int width, int height){
        this.x = x;
        this.y =y;
        this.width = width;
        this.height = height;
    }

    public void render(Graphics2D g2d) {

    }

    public boolean contains(int mx, int my){
        return mx >= x && mx <= x + width && my >= y && my <= y + height;
    }

    // Геттеры/сеттеры
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public boolean isVisible() { return visible; }
    public void setVisible(boolean v) { this.visible = v; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
}
