package org.example.inventory;

// Контейнер ТОЛЬКО сундука (27 ячеек) — без инвентаря игрока.
// Используется для отдельного окна сундука справа от экрана.
public class SingleChestContainer extends Container {

    public static final int CHEST_SLOTS = 27;

    private final Inventory chest;

    public SingleChestContainer(Inventory chest){
        this.chest = chest;
        for(int i = 0; i < CHEST_SLOTS; i++) addSlot(new Slot(chest, i, 0, 0));
    }

    @Override
    protected Inventory getPrimaryInventory(){ return chest; }

    @Override
    public void updateSlotPositions(int winX, int winY){
        final int pad = 16;
        final int gap = 4;
        int titleBar = 24;

        for(int row = 0; row < 3; row++){
            for(int col = 0; col < 9; col++){
                int index = row * 9 + col;
                slots.get(index).setX(winX + pad + col * (Slot.SIZE + gap));
                slots.get(index).setY(winY + titleBar + pad + row * (Slot.SIZE + gap));
            }
        }
    }
}
