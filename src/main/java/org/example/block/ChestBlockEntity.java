package org.example.block;

public class ChestBlockEntity extends InventoryBlockEntity {

    private static final int SLOT_COUNT = 27;

    public ChestBlockEntity(int x, int y){
        super(x, y, SLOT_COUNT);
    }

    public int getSlotCount(){ return SLOT_COUNT; }

    public void clearSlot(int slot){
        getInventory().clearSlot(slot);
    }

    @Override
    public String getType(){
        return "chest";
    }
}