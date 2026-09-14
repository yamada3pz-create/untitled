package org.example.block.Machine;

import org.example.block.InventoryBlockEntity;
import org.example.item.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class MachineEntity extends InventoryBlockEntity {

    private int craftProgress;
    private int craftTime;
    private boolean isRunning;

    public MachineEntity(int x, int y, int craftTime){
        super(x, y, 2);
        this.craftProgress = 0;
        this.craftTime = craftTime;
        this.isRunning = false;
    }

    public ItemStack getInput(){ return getInventory().getStack(0); }
    public ItemStack getOutput(){ return getInventory().getStack(1); }

    public void setInput(ItemStack slot){ getInventory().setStack(0, slot); }
    public void setOutput(ItemStack slot){ getInventory().setStack(1, slot); }

    public int getCraftProgress(){ return craftProgress; }
    public void setCraftProgress(int v){ craftProgress = v; }
    public int getCraftTime(){ return craftTime; }
    public boolean isRunning(){ return isRunning; }
    public void setRunning(boolean v){ isRunning = v; }

    public abstract boolean canCraft();
    public abstract void craft();

    @Override
    public void save(DataOutputStream out) throws Exception{
        super.save(out);
    }

    @Override
    public void load(DataInputStream in) throws Exception{
        super.load(in);
    }
}