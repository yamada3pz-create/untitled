package org.example.item;

import java.util.Objects;

public class ItemStack {

    public static final ItemStack EMPTY = new ItemStack(null, 0);

    private final Item item;
    private int count;

    public ItemStack(Item item, int count){
        this.item = item;
        this.count = count;
    }

    public Item getItem() { return item; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public void grow(int amount) { this.count += amount; }
    public void shrink(int amount) { this.count -= amount; }

    public boolean isEmpty() { return item == null || count < 0;}
    public int getMaxStackSize() { return item != null ? item.getMaxStackSize() : 64; }
    public boolean isStackable() { return count < getMaxStackSize(); }
    public int getStackLeft() { return getMaxStackSize() - count; }
    public ItemStack copy() { return new ItemStack(item, count); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStack other)) return false;
        return count == other.count && Objects.equals(item, other.item);
    }

    @Override
    public int hashCode() {
         return Objects.hash(item, count);
    }
}
