package org.example.ui;

import org.example.core.NineSliceRenderer;
import org.example.core.TextureLoader;
import org.example.item.ItemStack;
import org.example.ui.widgets.ButtonWidget;
import org.example.ui.widgets.Widget;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GuiWindow extends Widget {

    protected String title;
    protected boolean dragging;
    protected int dragOffsetX, dragOffsetY;
    protected boolean closeHovered;
    protected String bodyTexturePath;
    protected boolean draggable = true;      // можно ли перетаскивать за title bar
    protected boolean closeable = true;      // показывать ли крестик и обрабатывать клик по нему
    protected boolean showTitleBar = true;   // показывать ли полоску заголовка и title-текст

    protected ArrayList<Widget> widgets;
    protected int titleBarHeight;

    public GuiWindow(int x, int y, int width, int height, String title){
        super(x, y, width, height);
        this.visible = false;
        this.title = title;
        this.dragging = false;
        this.closeHovered = false;
        this.widgets = new ArrayList<>();
    }

    public void open() { visible = true; }
    public void close() { visible = false; }
    public boolean isOpen() { return visible; }
    public GuiWindow setDraggable(boolean v)    { this.draggable = v;    return this; }
    public GuiWindow setCloseable(boolean v)    { this.closeable = v;    return this; }
    public GuiWindow setShowTitleBar(boolean v) { this.showTitleBar = v; return this; }

    public void setBodyTexture(String path) {
        this.bodyTexturePath = path;
    }

    public void addWidget(Widget w) { widgets.add(w); }

    private boolean overCloseButton(int mx, int my){
        BufferedImage closeTex = TextureLoader.getGuiTexture("gui/sprites/widget/title_close");
        int bw = closeTex != null ? closeTex.getWidth() : 16;
        int bh = closeTex != null ? closeTex.getHeight() : 16;
        return mx >= x + width - bw - 1 && mx <= x + width - 1
                && my >= y + 1 && my <= y + 1 + bh;
    }

    public ItemStack mousePressed(int mx, int my, int button, boolean shift, ItemStack cursor){
        if(!visible) return cursor;

        if(closeable && overCloseButton(mx,my)){
            close();
            return cursor;
        }

        if(draggable && showTitleBar && mx >= x && mx <= x + width && my >= y && my <= y + titleBarHeight){
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
        closeHovered = overCloseButton(mx, my);
    }

    public void render(Graphics2D g2d) {
        if (!visible) return;

        if(showTitleBar){
            // Title bar — nine-slice
            BufferedImage titleTex = TextureLoader.getGuiTexture("gui/sprites/widget/title_window");
            this.titleBarHeight = titleTex.getHeight();
            g2d.drawImage(titleTex, x, y, width, titleBarHeight, null);

            // Title text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(title, x + 4, y + 10);

            // Close button — drawImage (no nine-slice)
            String closePath = closeHovered
                    ? "gui/sprites/widget/title_close_hover"
                    : "gui/sprites/widget/title_close";
            BufferedImage closeTex = TextureLoader.getGuiTexture(closePath);
            if (closeTex != null) {
                int cx = x + width - closeTex.getWidth() - 1;
                int cy = y + 1;
                g2d.drawImage(closeTex, cx, cy, null);
            }
        }

        // Body — fillRect
        BufferedImage bodyTex = bodyTexturePath != null
                ? TextureLoader.getGuiTexture(bodyTexturePath) : null;
        if (bodyTex != null) {
            g2d.drawImage(bodyTex, x, y + titleBarHeight, width, height - titleBarHeight, null);
        } else {
            g2d.setColor(new Color(40, 40, 40));
            g2d.fillRect(x, y + titleBarHeight, width, height - titleBarHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, y, width, height);
        }

        // Widgets
        for (int i = 0; i < widgets.size(); i++) {
            widgets.get(i).render(g2d);
        }
    }
}
