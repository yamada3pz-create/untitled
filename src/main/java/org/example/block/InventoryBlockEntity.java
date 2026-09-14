package org.example.block;

import org.example.inventory.Inventory;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class InventoryBlockEntity extends BlockEntity {

    private final Inventory inventory;

    protected InventoryBlockEntity(int x, int y, int slotCount){
        super(x, y);
        this.inventory = new Inventory(slotCount);
    }

    public Inventory getInventory(){ return inventory; }

    @Override
    public void save(DataOutputStream out) throws Exception{
        inventory.save(out);
    }

    @Override
    public void load(DataInputStream in) throws Exception{
        inventory.load(in);
    }
}