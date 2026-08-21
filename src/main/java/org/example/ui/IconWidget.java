package org.example.ui;

import java.awt.*;
import java.awt.image.BufferedImage;

public class IconWidget extends Widget{

    private BufferedImage image;

    public IconWidget(int x, int y, int size, BufferedImage image){
        super(x, y, size, size);
        this.image = image;
    }

    @Override
    public void render(Graphics2D g2d){
        if(!visible) return;
        g2d.drawImage(image, x, y, width, height, null);
    }
}
