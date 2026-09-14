package org.example.inventory;

import org.example.item.ItemStack;

// Связка: хранилище + номер ячейки + позиция на экране
public class Slot {

    public static final int SIZE = 32;

    private final Inventory inventory; // К какому хранилищу относится слот
    private final int index;           // Номер ячейки внутри хранилища
    private int x, y;                  // Координаты на экране

    public Slot(Inventory inventory, int index, int x, int y){
         this.inventory = inventory;
         this.index = index;
         this.x = x;
         this.y = y;
    }

    // Посредники: просим инвентарь дать/принять предмет этого слота
    public ItemStack getStack()        { return inventory.getStack(index); }
    public void setStack(ItemStack stack) { inventory.setStack(index, stack);}

    public int getX() { return x; }
    public int getY() { return y; }
    public int getIndex() { return index; }

    // Попали курсором в этот слот
    public boolean isMouseOver(int mouseX, int mouseY){
        return mouseX >= x && mouseX <= x + SIZE && mouseY >= y && mouseY <= y + SIZE;
    }

    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }

    // Сдвиг при перетаскивании
    public void moveBy(int dx, int dy){ x += dx; y += dy; }
}
