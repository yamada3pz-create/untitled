package org.example.ui;

import org.example.inventory.Container;

import java.awt.*;

public class ChestInventoryWindow extends GuiWindow {

    private SlotWidget[] chestSlots;
    private SlotWidget[] playerSlots;

    public ChestInventoryWindow(int chestX, int chestY, Container chestContainer, Container playerContainer) {
        super(chestX, chestY - 250, 320, 370, "Сундук");

        int startX = x + 16;
        int startY = y + titleBarHeight + 16;
        int slotSize = 32;
        int gap = 4;

        // Слоты сундука 0-26 (3 строки по 9)
        chestSlots = new SlotWidget[27];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                int sx = startX + col * (slotSize + gap);
                int sy = startY + row * (slotSize + gap);
                chestSlots[index] = new SlotWidget(sx, sy, chestContainer, index);
                addWidget(chestSlots[index]);
            }
        }

        // Инвентарь игрока внизу
        int playerY = startY + 3 * (slotSize + gap) + 16;
        playerSlots = new SlotWidget[36];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                int sx = startX + col * (slotSize + gap);
                int sy = playerY + row * (slotSize + gap);
                playerSlots[index] = new SlotWidget(sx, sy, playerContainer, index);
                addWidget(playerSlots[index]);
            }
        }
        int hotbarY = playerY + 3 * (slotSize + gap) + 4;
        for (int col = 0; col < 9; col++) {
            int index = 27 + col;
            int sx = startX + col * (slotSize + gap);
            playerSlots[index] = new SlotWidget(sx, hotbarY, playerContainer, index);
            addWidget(playerSlots[index]);
        }
    }

    public SlotWidget[] getChestSlots() { return chestSlots; }
    public SlotWidget[] getPlayerSlots() { return playerSlots; }
}
