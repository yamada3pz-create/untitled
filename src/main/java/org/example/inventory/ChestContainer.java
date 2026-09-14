package org.example.inventory;

public class ChestContainer extends Container {

    public static final int CHEST_SLOTS = 27;
    public static final int TOTAL = CHEST_SLOTS + 36;

    private final Inventory chest;
    private final Inventory player;

    public ChestContainer(Inventory chest, Inventory player){
        this.chest = chest;
        this.player = player;

        for(int i = 0; i < CHEST_SLOTS; i++) addSlot(new Slot(chest, i, 0, 0));
        for(int i = 0; i < 36; i++)         addSlot(new Slot(player, i, 0, 0));
    }

    @Override
    protected Inventory getPrimaryInventory(){ return chest; }

    @Override
    public void shiftClick(int idx){
        if(idx < 0 || idx >= slots.size()) return;
        boolean fromChest = idx < CHEST_SLOTS;
        int from = fromChest ? CHEST_SLOTS : 0;
        int to   = fromChest ? slots.size() : CHEST_SLOTS;

        Slot src = slots.get(idx);
        moveIntoZone(src, from, to);
    }

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

        int pStartY = winY + titleBar + pad + 3 * (Slot.SIZE + gap) + 14;
        for(int row = 0; row < 3; row++){
            for(int col = 0; col < 9; col++){
                int index = CHEST_SLOTS + row * 9 + col;
                slots.get(index).setX(winX + pad + col * (Slot.SIZE + gap));
                slots.get(index).setY(pStartY + row * (Slot.SIZE + gap));
            }
        }

        int hotbarRow = pStartY + 3 * (Slot.SIZE + gap) + 8;
        for(int col = 0; col < 9; col++){
            int index = CHEST_SLOTS + 27 + col;
            slots.get(index).setX(winX + pad + col * (Slot.SIZE + gap));
            slots.get(index).setY(hotbarRow);
        }
    }
}