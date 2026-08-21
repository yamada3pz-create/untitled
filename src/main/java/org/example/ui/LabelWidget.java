package org.example.ui;

import java.awt.*;

public class LabelWidget extends Widget{

    private String text;
    private Color color;
    private Font font;

    public LabelWidget(int x, int y, String text, Color color, Font font) {
        super(x, y, 0, 0);
        this.text = text;
        this.color = color;
        this.font = font;
    }

    public void setText(String text) { this.text = text; }

    @Override
    public void render(Graphics2D g2d){
        if(visible){
            g2d.setFont(font);
            g2d.setColor(color);
            g2d.drawString(text, x, y);
        }
    }
}
