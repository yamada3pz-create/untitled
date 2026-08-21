package org.example.ui;

import org.example.inventory.Container;

public class PlayerInventoryWindow extends GuiWindow{

    private SlotWidget[] slot;

    public PlayerInventoryWindow(Container playerContainer) {
        super(200, 150, 320, 200, " Инвентарь");
        this.slot = new SlotWidget[36];

        int startX = x + 16;
        int startY = y + titleBarHeight + 16;
        int slotSize = 32;
        int gap = 4;

        // Основные слоты 0-26(3 строки по 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                int sx = startX + col * (slotSize + gap);
                int sy = startY + row * (slotSize + gap);
                slot[index] = new SlotWidget(sx, sy, playerContainer, index);
                addWidget(slot[index]);
            }
        }

        // Хотбар 27-36 (1 строка по 9, с отступом)
        int hotbarY = startY + 3 * (slotSize + gap);
        for (int col = 0; col < 9; col++) {
            int index = 27 + col;
            int sx = startX + col * (slotSize + gap);
            slot[index] = new SlotWidget(sx, hotbarY, playerContainer, index);
            addWidget(slot[index]);
        }
    }

    public SlotWidget[] getSlot() {
        return slot;
    }
}
