package org.example.inventory;

import org.example.item.ItemStack;

public class SimpleContainer implements Container{

    private final ItemStack[] slots;

    public SimpleContainer(int size){
        this.slots = new ItemStack[size];
        for(int i = 0; i < size; i++){
            slots[i] = ItemStack.EMPTY;
        }
    }
    @Override
    public int getContainerSize() {
        return slots.length;
    }

    @Override
    public ItemStack getItem(int slot) {
        if(slot < 0 || slot >= slots.length) return ItemStack.EMPTY;
        return slots[slot];
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if(slot < 0 || slot >= slots.length) return;
        slots[slot] = (stack == null) ? ItemStack.EMPTY : stack;

    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if(slot < 0 || slot >= slots.length) return ItemStack.EMPTY;
        ItemStack stack = slots[slot];
        if(stack.isEmpty()) return ItemStack.EMPTY;

        int removed = Math.min(amount, stack.getCount());
        ItemStack result = new ItemStack(stack.getItem(), removed);
        if(stack.isEmpty()) slots[slot] = ItemStack.EMPTY;
        return result;
    }

    @Override
    public void clearSlot(int slot) {
        if(slot < 0 || slot >= slots.length) return;
        slots[slot] = ItemStack.EMPTY;
    }

    public static boolean transfer(Container from, int fromSlot, Container to , int toSlot){
        ItemStack fromStack = from.getItem(fromSlot);
        if(fromStack.isEmpty()) return false;

        ItemStack toStack = to.getItem(toSlot);

        if(toStack.isEmpty()){
            to.setItem(toSlot, fromStack.copy());
            from.clearSlot(fromSlot);
            return true;
        }

        if(!toStack.getItem().getId().equals(fromStack.getItem().getId())) return false;

        int total = toStack.getCount() + fromStack.getCount();
        int max = toStack.getMaxStackSize();

        if(total <= max){
            toStack.setCount(total);
            from.clearSlot(fromSlot);
        }else{
            toStack .setCount(max);
            fromStack.shrink(max - toStack.getCount());
        }
        return true;
    }
}
