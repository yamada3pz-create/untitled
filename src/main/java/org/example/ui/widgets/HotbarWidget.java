package org.example.ui.widgets;

import org.example.inventory.Container;
import org.example.item.ItemStack;

import java.awt.*;

public class HotbarWidget {

    public static final int SLOTS = 9;

    private static final int SLOT_SIZE = 32;
    private static final int GAP = 4;
    private static final Color BG_COLOR = new Color(60, 60, 60);
    private static final Color BORDER_COLOR = new Color(100, 100, 100);
    private static final Color SELECTED_COLOR = new Color(220, 220, 120);

    private final Container container;
    private final int firstSlotIndex;   // Индекс первого слота в контейнере
    private int selected;               // 0..8

    public HotbarWidget (Container container , int firstSlotIndex){
        this.container = container;
        this.firstSlotIndex = firstSlotIndex;
    }

    public int getSelectedContainerIndex()  { return firstSlotIndex + selected; }
    public ItemStack getSelectedItem()      { return container.getItem(getSelectedContainerIndex()); }
    public int getSelected()                { return selected; }

    // Клавиши 1-9
    public void selectByKey(int number){
        if(number >= 1 && number <= SLOTS) selected = number - 1;
    }

    // Колесо мыши: +1 вправо, -1 влево
    public void scroll(int direction){
        selected = Math.floorMod(selected + direction, SLOTS);
    }

    public void render(Graphics2D g2d, int screenWidth, int screenHeight){
        int totalW = SLOTS * SLOT_SIZE + (SLOTS -1) * GAP;
        int startX = (screenWidth - totalW) / 2; // Центрируем по ширине экрана
        int y = screenHeight - SLOT_SIZE - 8;

        for(int i = 0; i < SLOTS; i++){
            int x = startX + i * (SLOT_SIZE + GAP);

            g2d.setColor(BG_COLOR);
            g2d.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);
            g2d.setColor(i == selected ? SELECTED_COLOR : BORDER_COLOR);
            g2d.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);

            ItemStack stack = container.getItem(firstSlotIndex + i);
            if (!stack.isEmpty()) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.PLAIN, 11));
                g2d.drawString(stack.getItem().getId(), x + 3, y + 13);

                if (stack.getCount() > 1) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString(String.valueOf(stack.getCount()), x + SLOT_SIZE - 14, y + SLOT_SIZE - 4);
                }
            }
        }
    }
}
