package org.example.ui.widgets;

import org.example.core.TextureLoader;
import org.example.inventory.Container;
import org.example.item.ItemStack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SmallWindow extends Widget {

    private String title;
    private boolean dragging;
    private int dragOffsetX, dragOffsetY;

    // текстура изображения
    private BufferedImage texture;
    private BufferedImage titleWindow;
    private BufferedImage titleClose;
    private BufferedImage titleCloseHover;
    protected int titleBarHeight;
    protected ArrayList<Widget> widgets;

    public SmallWindow(int x, int y, String title) {
        super(x, y, 0,0 );

        this.texture = TextureLoader.getGuiTexture("gui/container/inventory");
        titleWindow = TextureLoader.getGuiTexture("gui/sprite/widget/title_window");
        titleClose = TextureLoader.getGuiTexture("gui/sprite/widget/title_close");
        titleCloseHover = TextureLoader.getGuiTexture("gui/sprite/widget/title_close_hover");

        this.x = x;
        this.y = y;
        this.title = title;
        this.dragging = true;
        this.titleBarHeight = titleWindow.getHeight();
        this.width = titleWindow.getWidth();
        this.height = titleWindow.getHeight() + this.texture.getHeight();
        widgets = new ArrayList<>();

    }

    public void open() { visible = true; }
    public void close() { visible = false; }
    public boolean isOpen() { return visible; }
    public void addWidgets(Widget w ) { this.widgets.add(w); }

    private boolean overCloseButton(int mx, int my){
        return mx >= x + width - 20 && mx <= x + width - 4
                && my >= y + 4 && my <= y + 20;
    }

    public ItemStack mousePressed(int mx, int my, int button, boolean shift, ItemStack cursor){
        if(!visible) return cursor;

        if(overCloseButton(mx, my)){
            close();
            return cursor;
        }

        if(mx >= x && mx <= x + width && my >= y && my <= y + titleBarHeight){
            dragging = true;
            dragOffsetX = mx - x;
            dragOffsetY = my - y;
        }

        for(int i = 0; i < widgets.size(); i++){
            Widget w = widgets.get(i);
            if(w instanceof ButtonWidget){
                ButtonWidget btn = (ButtonWidget) w;
                if(btn.contains(mx, my)){
                    btn.click();
                }
            }
        }

        return slotClicked(mx, my, button, shift, cursor);
    }
    public ItemStack slotClicked(int mx, int my, int button, boolean shift, ItemStack cursor){
        return cursor;
    }


    public void mouseDragged(int mx, int my) {
        if (!visible || !dragging) return;
        int newX = mx - dragOffsetX;
        int newY = my - dragOffsetY;
        int dx = newX - this.x;
        int dy = newY - this.y;
        this.x = newX;
        this.y = newY;

        for(int i = 0; i < widgets.size(); i++){
            Widget w = widgets.get(i);
            w.setPosition(w.getX() + dx, w.getY() + dy);
        }
    }

    public void mouseReleased(int mx, int my) {
        dragging = false;
    }

    public void mouseMoved(int mx, int my) {
        if (!visible) return;
        for (int i = 0; i < widgets.size(); i++) {
            Widget w = widgets.get(i);
            if (w instanceof ButtonWidget) {
                ButtonWidget btn = (ButtonWidget) w;
                btn.setHovered(btn.contains(mx, my));
            }
        }
    }

    public void render(Graphics2D g2d){
        if (!visible) return;
        if(texture != null){
          if(dragging){
            g2d.drawImage(titleWindow, x, y, width, titleWindow.getHeight(), null);

          }
        }
    }
}
