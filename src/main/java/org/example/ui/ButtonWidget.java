package org.example.ui;

import javax.swing.plaf.basic.BasicOptionPaneUI;
import java.awt.*;

public class ButtonWidget extends Widget{

    private String text;
    private Color bgColor;
    private Color textColor;
    private Color hoverColor;
    private boolean hovered;

    // Ссылка на действие (вызывается при клике)
    private ButtonAction action;

    public ButtonWidget(int x, int y, int w, int h, String text, Color bgColor, Color textColor){
        super(x, y, w, h);
        this.text = text;
        this.bgColor = bgColor;
        this.textColor = textColor;
        this.hoverColor = bgColor.brighter();
        this.hovered = false;
    }

    public void setAction (ButtonAction action){
        this.action = action;
    }

    public void setHovered(boolean hovered){
        this.hovered = hovered;
    }

    public void click(){
        if(action != null) action.onClick();
    }

    @Override
    public void render(Graphics2D g2d){
        if(!visible) return;

        // Фон
        g2d.setColor(hovered ? hoverColor : bgColor);
        g2d.fillRect(x, y, width, height);

        // Рамка
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);

        // Текст по центру
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + (height + fm.getAscent() - fm.getDescent()) / 2;
        g2d.drawString(text, textX, textY);
    }
    // Интерфейс для действия
    public interface ButtonAction{
        void onClick();
    }

}
