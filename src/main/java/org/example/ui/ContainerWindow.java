package org.example.ui;

import org.example.core.TextureLoader;
import org.example.inventory.Container;
import org.example.inventory.Slot;
import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.ui.widgets.ButtonWidget;
import org.example.ui.widgets.SmallWindow;
import org.example.ui.widgets.Widget;

import java.awt.*;
import java.awt.event.MouseEvent;

// Окно, привязанное к контейнеру: рисует его слоты, обрабатывает клики по ним
public class ContainerWindow extends GuiWindow {

    protected final Container container;
    private static final Color SLOT_BG = new Color(60, 60, 60);
    private static final Color SLOT_BORDER = new Color(100, 100, 100);

    public ContainerWindow(Container container, int x, int y, int w, int h, String title){
        super(x, y, w, h, title);
        this.container = container;
        container.updateSlotPositions(x, y);
    }

    public Container getContainer(){ return container; }

    // Клик по слоту через базовое окно: ищем слот под курсором
    @Override
    public ItemStack slotClicked(int mx, int my, int button, boolean shift, ItemStack cursor){
        boolean leftClick  = (button == MouseEvent.BUTTON1);
        boolean rightClick = (button == MouseEvent.BUTTON3);
        if(!leftClick && !rightClick) return cursor;

        java.util.List<Slot> all = container.getSlots();
        for(int i = 0; i < all.size(); i++){
            Slot s = all.get(i);
            if(!s.isMouseOver(mx, my)) continue;

            if(leftClick && shift){          // быстрый перенос между зонами
                container.shiftClick(i);
                return cursor;
            }
            return container.handleSlotClick(i, rightClick, cursor); // i — ПОЗИЦИЯ в списке
        }
        return cursor;
    }

    @Override
    public void mouseDragged(int mx, int my){
        super.mouseDragged(mx, my);
        container.updateSlotPositions(x, y);
    }

    @Override
    public void render(Graphics2D g2d){
        if(!visible) return;
        super.render(g2d);

        // Сетка: фон каждой ячейки
        for(Slot s : container.getSlots()){
            g2d.setColor(SLOT_BG);
            g2d.fillRect(s.getX(), s.getY(), Slot.SIZE, Slot.SIZE);
            g2d.setColor(SLOT_BORDER);
            g2d.drawRect(s.getX(), s.getY(), Slot.SIZE, Slot.SIZE);
        }

        // Предметы поверх ячеек
        for(Slot s : container.getSlots()){
            drawStackAt(g2d, s.getStack(), s.getX(), s.getY());
        }
    }

    // --- Отрисовка одного стака (квадрат цвета предмета + количество) ---
    public static void drawStackAt(Graphics2D g2d, ItemStack stack, int x, int y){
        if(stack.isEmpty()) return;

        java.awt.image.BufferedImage tex = TextureLoader.getItemTexture(stack.getItem().getId());
        if(tex != null && tex != TextureLoader.getMissingTexture()){
            g2d.drawImage(tex, x + 4, y + 4, Slot.SIZE - 8, Slot.SIZE - 8, null);
        } else {
            g2d.setColor(getItemColor(stack.getItem()));
            g2d.fillRect(x + 4, y + 4, Slot.SIZE - 8, Slot.SIZE - 8);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x + 4, y + 4, Slot.SIZE - 8, Slot.SIZE - 8);
        }

        if(stack.getCount() > 1){
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.drawString(String.valueOf(stack.getCount()), x + Slot.SIZE - 14, y + Slot.SIZE - 4);
        }
    }

    public static Color getItemColor(Item item){
        if(item == null) return Color.WHITE;
        return switch (item.getId()) {
            case "grass" -> new Color(50, 150, 50);
            case "stone" -> Color.GRAY;
            case "sand" -> new Color(210, 200, 140);
            case "pipe" -> Color.DARK_GRAY;
            case "chest" -> new Color(139, 90, 43);
            case "iron_ore" -> new Color(190, 140, 100);
            case "copper_ore" -> new Color(200, 110, 60);
            case "wall" -> Color.LIGHT_GRAY;
            case "machine" -> new Color(180, 120, 40);
            default -> Color.WHITE;
        };
    }
}
