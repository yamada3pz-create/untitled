package org.example.inventory;

public class PlayerContainer extends Container {

    public static final int GRID = 27;
    public static final int TOTAL = 36;

    private final Inventory inventory;

    public PlayerContainer(Inventory inventory){
        this.inventory = inventory;
        for(int i = 0; i < TOTAL; i++){
            addSlot(new Slot(inventory, i, 0, 0));
        }
        setHotbarRange(GRID, TOTAL);
    }

    @Override
    protected Inventory getPrimaryInventory(){ return inventory; }

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
        int hotbarRow = winY + titleBar + pad + 3 * (Slot.SIZE + gap) + 8;
        for(int col = 0; col < 9; col++){
            slots.get(GRID + col).setX(winX + pad + col * (Slot.SIZE + gap));
            slots.get(GRID + col).setY(hotbarRow);
        }
    }
}