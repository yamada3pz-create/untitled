package org.example.ui;

import java.awt.*;
import org.example.inventory.Container;
import org.example.inventory.SimpleContainer;
import org.example.item.Item;
import org.example.item.ItemStack;

public class SlotWidget extends Widget{

    private final Container container;
    private final int slotIndex;
    private boolean selected;

    private static final int SLOT_SIZE = 32;
    private static final Color BG_COLOR = new Color(60, 60, 60);
    private static final Color BORDER_COLOR = new Color(100, 100, 100);
    private static final Color SELECTED_COLOR = new Color(200, 200, 100);

    public SlotWidget(int x, int y, Container container, int slotIndex) {
        super(x, y, SLOT_SIZE, SLOT_SIZE);
        this.container = container;
        this.slotIndex = slotIndex;
        this.selected = false;
    }

    public Container getContainer() { return container; }
    public int getSlotIndex() { return slotIndex; }
    public ItemStack getItemStack() { return container.getItem(slotIndex); }
    public void setSelected(boolean selected){
        this.selected = selected;
    }

    // Своп предметов между двумя слотами
    public static boolean swap(SlotWidget a, SlotWidget b) {
        return SimpleContainer.transfer(a.container, a.slotIndex, b.container, b.slotIndex);
    }

    @Override
    public void render(Graphics2D g2d){
        if(!visible) return;

        // Фон слота
        g2d.setColor(BG_COLOR);
        g2d.fillRect(x, y, width, height);

        // Рамка
        g2d.setColor(selected ? SELECTED_COLOR : BORDER_COLOR);
        g2d.drawRect(x, y, width, height);

        // Предмет (заглушка - рисуем цветной квадрат по itemId)
        ItemStack stack = container.getItem(slotIndex);
        if(!stack.isEmpty()){
            g2d.setColor(getItemColor(stack.getItem()));
            g2d.drawRect(x + 4, y + 4, width - 8, height - 8);

            // Количество
            if(stack.getCount() > 1){
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 19));
                g2d.drawString(String.valueOf(stack.getCount()), x + width - 12, y + height - 4);
            }
        }
    }

    // Заглушка: itemId -> цвет(потом заменим на текстуры)
    private Color getItemColor(Item item){
        if(item == null) return Color.WHITE;
        return switch (item.getId()) {
            case "grass" -> new Color(50, 150, 50);
            case "stone" -> Color.GRAY;
            case "sand" -> new Color(210, 200, 140);
            case "pipe" -> Color.DARK_GRAY;
            case "chest" -> new Color(139, 90, 43);
            default -> Color.WHITE;
        };
    }
}
