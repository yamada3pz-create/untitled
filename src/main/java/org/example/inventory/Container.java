package org.example.inventory;

import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.item.Items;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class Container {

    protected final List<Slot> slots = new ArrayList<>();

    protected void addSlot(Slot slot) { slots.add(slot); }
    public List<Slot> getSlots()      { return slots; }

    protected int hotbarFrom = 0, hotbarTo = 0;
    protected void setHotbarRange(int from, int to){ hotbarFrom = from; hotbarTo = to; }

    protected abstract Inventory getPrimaryInventory();

    public int addItem(String itemId, int count){
        Item item = Items.get(itemId);
        if(item == null) return count;
        return getPrimaryInventory().addItem(item, count);
    }

    public void save(DataOutputStream out) throws Exception{
        getPrimaryInventory().save(out);
    }

    public void load(DataInputStream in) throws Exception{
        getPrimaryInventory().load(in);
    }

    public ItemStack handleSlotClick(int idx, boolean rightClick, ItemStack cursor) {
        if (cursor == null) cursor = ItemStack.EMPTY;
        if (idx < 0 || idx >= slots.size()) return cursor;
        Slot slot = slots.get(idx);
        ItemStack inSlot = slot.getStack();

        if (!rightClick) {
            if (cursor.isEmpty()) {
                if (!inSlot.isEmpty()) {
                    cursor = inSlot.copy();
                    slot.setStack(ItemStack.EMPTY);
                }
            } else {
                if (inSlot.isEmpty()) {
                    slot.setStack(cursor.copy());
                    cursor = ItemStack.EMPTY;
                } else if (inSlot.getItem().getId().equals(cursor.getItem().getId())) {
                    int max = inSlot.getItem().getMaxStackSize();
                    int space = max - inSlot.getCount();
                    int put = Math.min(cursor.getCount(), space);
                    inSlot.grow(put);
                    cursor.shrink(put);
                    if (cursor.isEmpty()) cursor = ItemStack.EMPTY;
                } else {
                    ItemStack tmp = inSlot.copy();
                    slot.setStack(cursor.copy());
                    cursor = tmp;
                }
            }
        } else {
            if (cursor.isEmpty()) {
                if (!inSlot.isEmpty()) {
                    int half = (inSlot.getCount() + 1) / 2;
                    cursor = new ItemStack(inSlot.getItem(), half);
                    inSlot.shrink(half);
                    if (inSlot.isEmpty()) slot.setStack(ItemStack.EMPTY);
                }
            } else {
                if (inSlot.isEmpty()) {
                    slot.setStack(new ItemStack(cursor.getItem(), 1));
                    cursor.shrink(1);
                } else if (inSlot.getItem().getId().equals(cursor.getItem().getId())
                        && inSlot.getCount() < inSlot.getItem().getMaxStackSize()) {
                    inSlot.grow(1);
                    cursor.shrink(1);
                } else {
                    ItemStack tmp = inSlot.copy();
                    slot.setStack(cursor.copy());
                    cursor = tmp;
                }
                if (cursor.isEmpty()) cursor = ItemStack.EMPTY;
            }
        }
        return cursor;
    }

    public void shiftClick(int idx) {
        if (idx < 0 || idx >= slots.size()) return;
        if (hotbarTo <= hotbarFrom) return;

        Slot src = slots.get(idx);
        if (src.getStack().isEmpty()) return;

        boolean inHotbar = idx >= hotbarFrom && idx < hotbarTo;
        int from = inHotbar ? 0 : hotbarFrom;
        int to   = inHotbar ? hotbarFrom : slots.size();

        moveIntoZone(src, from, to);
    }

    protected void moveIntoZone(Slot src, int from, int to){
        ItemStack stack = src.getStack();
        if(stack.isEmpty()) return;

        for(int i = from; i < to && !stack.isEmpty(); i++){
            Slot dst = slots.get(i);
            ItemStack dstStack = dst.getStack();
            int max = stack.getItem().getMaxStackSize();

            if(!dstStack.isEmpty() && dstStack.getItem().getId().equals(stack.getItem().getId())
                    && dstStack.getCount() < max){
                int space = max - dstStack.getCount();
                int put = Math.min(stack.getCount(), space);
                dstStack.grow(put);
                stack.shrink(put);
            }
        }
        for(int i = from; i < to && !stack.isEmpty(); i++){
            Slot dst = slots.get(i);
            if(dst.getStack().isEmpty()){
                dst.setStack(stack.copy());
                stack.setCount(0);
            }
        }
        if(stack.isEmpty()) src.setStack(ItemStack.EMPTY);
    }

    public abstract void updateSlotPositions(int winX, int winY);
}