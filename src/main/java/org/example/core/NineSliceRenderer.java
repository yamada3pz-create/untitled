package org.example.core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class NineSliceRenderer {

    public static void draw(Graphics2D g2d, BufferedImage img, int x, int y, int w, int h, int border) {
        if (img == null || border <= 0) {
            g2d.drawImage(img, x, y, w, h, null);
            return;
        }

        int iw = img.getWidth();
        int ih = img.getHeight();

        int innerW = iw - border * 2;
        int innerH = ih - border * 2;

        if (innerW <= 0 || innerH <= 0) {
            g2d.drawImage(img, x, y, w, h, null);
            return;
        }

        // Слишком маленькая кнопка — просто обрезаем текстуру, не сжимаем
        if (w < border * 2 || h < border * 2) {
            g2d.drawImage(img, x, y, x + w, y + h, 0, 0, iw, ih, null);
            return;
        }

        // Corner regions
        BufferedImage topLeft     = img.getSubimage(0, 0, border, border);
        BufferedImage topRight    = img.getSubimage(iw - border, 0, border, border);
        BufferedImage bottomLeft  = img.getSubimage(0, ih - border, border, border);
        BufferedImage bottomRight = img.getSubimage(iw - border, ih - border, border, border);

        // Edge regions
        BufferedImage top    = img.getSubimage(border, 0, innerW, border);
        BufferedImage bottom = img.getSubimage(border, ih - border, innerW, border);
        BufferedImage left   = img.getSubimage(0, border, border, innerH);
        BufferedImage right  = img.getSubimage(iw - border, border, border, innerH);

        // Center region
        BufferedImage center = img.getSubimage(border, border, innerW, innerH);

        int rightX  = x + w - border;
        int bottomY = y + h - border;
        int centerW = w - border * 2;
        int centerH = h - border * 2;

        // Углы рисуем в натуральном размере, без масштабирования
        g2d.drawImage(topLeft, x, y, null);
        g2d.drawImage(topRight, rightX, y, null);
        g2d.drawImage(bottomLeft, x, bottomY, null);
        g2d.drawImage(bottomRight, rightX, bottomY, null);

        // Рёбра и середина — тайлинг: повторяем сегменты, последний обрезаем.
        // Ничего не растягивается и не сжимается (как тайлинг в Minecraft).
        drawTiled(g2d, top,    x + border, y,         centerW, border);
        drawTiled(g2d, bottom, x + border, bottomY,   centerW, border);
        drawTiled(g2d, left,   x,          y + border, border, centerH);
        drawTiled(g2d, right,  rightX,     y + border, border, centerH);
        drawTiled(g2d, center, x + border, y + border, centerW, centerH);
    }

    // Повторяет участок текстуры: длиннее — тайлы повторяются, короче — обрезается с края.
    private static void drawTiled(Graphics2D g2d, BufferedImage src,
                                  int dx, int dy, int dw, int dh){
        int sw = src.getWidth();
        int sh = src.getHeight();
        for(int ty = 0; ty < dh; ty += sh){
            for(int tx = 0; tx < dw; tx += sw){
                int rw = Math.min(sw, dw - tx);
                int rh = Math.min(sh, dh - ty);
                g2d.drawImage(src, dx + tx, dy + ty, dx + tx + rw, dy + ty + rh,
                        0, 0, rw, rh, null);
            }
        }
    }
}