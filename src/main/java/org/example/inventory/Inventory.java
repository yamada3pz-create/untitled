package org.example.inventory;

import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.item.Items;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class Inventory {

    private final ItemStack[] stacks;

    public Inventory(int size){
        stacks = new ItemStack[size];
        Arrays.fill(stacks, ItemStack.EMPTY);
    }

    public int getSize(){ return stacks.length; }

    public ItemStack getStack(int index){
        if(index < 0 || index >= stacks.length) return ItemStack.EMPTY;
        return stacks[index];
    }

    public void setStack(int index, ItemStack stack){
        if(index < 0 || index >= stacks.length) return;
        stacks[index] = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;
    }

    public void clearSlot(int index){
        if(index < 0 || index >= stacks.length) return;
        stacks[index] = ItemStack.EMPTY;
    }

    // Добавляем стэки, потом занимаем пустые слоты. Возвращает сколько не поместилось
    public int addItem(Item item, int count){
        int max = item.getMaxStackSize();

        for (int i = 0; i < stacks.length && count > 0; i++) {
            ItemStack s = stacks[i];
            if(!s.isEmpty() && s.getItem().getId().equals(item.getId()) && s.getCount() < max){
                int put = Math.min(count, max - s.getCount());
                s.grow(put);
                count -= put;

            }
        }
        for (int i = 0; i < stacks.length && count > 0; i++) {
            if (!stacks[i].isEmpty()) continue;
            int put = Math.min(count, max);
            stacks[i] = new ItemStack(item, put);
            count -= put;

        }
        return count;
    }


    // --- Сохранение / загрузка: id строкой + количество на каждый слот ---
    public void save(DataOutputStream out) throws Exception{
        for(ItemStack s : stacks){
            if(s.isEmpty()){
                out.writeUTF("");
                out.writeInt(0);
            }else{
                out.writeUTF(s.getItem().getId());
                out.writeInt(s.getCount());
            }
        }
    }

    public void load(DataInputStream in) throws Exception {
        for(int i = 0; i < stacks.length; i++){
            String itemId = in.readUTF();
            int count = in.readInt();
            if(itemId == null || itemId.isEmpty() || count <= 0){
                clearSlot(i);
            }else{
                Item item = Items.get(itemId);
                if(item != null) setStack(i, new ItemStack(item, count));
            }
        }
    }
}
