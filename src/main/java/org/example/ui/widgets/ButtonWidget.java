package org.example.ui.widgets;

import org.example.core.NineSliceRenderer;
import org.example.core.TextureLoader;

import java.awt.*;

public class ButtonWidget extends Widget {

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
    public void onMouseMoved(int mx, int my){
        this.hovered = isMouseOver(mx, my);
    }

    @Override
    public boolean onMousePressed(int mx, int my, int button){
        if(!isMouseOver(mx, my)) return false;
        if(button == java.awt.event.MouseEvent.BUTTON1){
            click();
            return true; // клик поглощён кнопкой
        }
        return false;
    }

    @Override
    public void render(Graphics2D g2d){
        if(!visible) return;

        String texPath = hovered ? "gui/sprites/widget/button_hover" : "gui/sprites/widget/button";
        java.awt.image.BufferedImage tex = TextureLoader.getGuiTexture(texPath);
        int border = TextureLoader.getGuiBorder(texPath);
        NineSliceRenderer.draw(g2d, tex, x, y, width, height, border);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(textColor);
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (width - fm.stringWidth(text)) / 2;
        int textY = y + (height - fm.getHeight()) / 2 + fm.getAscent();
        g2d.drawString(text, textX, textY);
    }
    // Интерфейс для действия
    public interface ButtonAction{
        void onClick();
    }

}
