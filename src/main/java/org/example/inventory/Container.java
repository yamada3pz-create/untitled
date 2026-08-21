package org.example.inventory;

import org.example.item.ItemStack;

public interface Container {

    int getContainerSize();
    ItemStack getItem(int slot);
    void setItem(int slot, ItemStack stack);
    ItemStack removeItem(int slot, int amount);
    void clearSlot(int slot);
}
