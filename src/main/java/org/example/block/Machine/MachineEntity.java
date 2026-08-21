package org.example.block.Machine;

import org.example.block.BlockEntity;
import org.example.inventory.SimpleContainer;
import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.item.Items;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public abstract class MachineEntity extends BlockEntity {

    private final SimpleContainer inventory = new SimpleContainer(2);

    // Таймер крафта
    private int craftProgress;
    private int craftTime;

    // Статус
    private boolean isRunning;

    public MachineEntity(int x, int y, int craftTime){
        super(x, y);
        this.craftProgress = 0;
        this.craftTime = craftTime;
        this.isRunning = false;
    }

    // --- Геттеры ---

    public SimpleContainer getInventory() { return inventory; }

    public ItemStack getInput() { return inventory.getItem(0); }
    public ItemStack getOutput() { return inventory.getItem(1); }

    public void setInput(ItemStack slot) { inventory.setItem(0, slot); }
    public void setOutput(ItemStack slot) { inventory.setItem(1, slot); }

    public int getCraftProgress() { return craftProgress; }
    public int getCraftTime()   { return craftTime; }
    public boolean isRunning()  { return isRunning; }


    // --- Абстрактные методы (конкретная машина определяет) ---

    // Можно ли крафтить? Проверяет входной слот и рецепт
    public abstract boolean canCraft();

    // Выполняет крафт: забирает со входа, кладет на выход
    public abstract void craft();

    // --- Тик ---

    @Override
    public void tick() {
        // Если можно крафтить запускаем
        if(!isRunning && canCraft()){
            isRunning = true;
            craftProgress = 0;
        }
        // Если работает - увеличиваем таймер
        if(isRunning){
            craftProgress ++;
            if(craftProgress >= craftTime){
                craft();
                craftProgress = 0;

                // Если больше не можем крафтить останавливаемся
                if(!canCraft()){
                    isRunning = false;
                }
            }
        }
    }

    // --- Сохранение / Загрузка ---
    @Override
    public void save(DataOutputStream out) throws Exception{
        for (int i = 0; i < 2; i++) {
            ItemStack stack = inventory.getItem(i);
            if(stack.isEmpty()){
                out.writeUTF("");
                out.writeInt(0);
            }else{
                out.writeUTF(stack.getItem().getId());
                out.writeInt(stack.getCount());
            }
        }
        out.writeInt(craftProgress);
        out.writeInt(craftTime);
        out.writeBoolean(isRunning);
    }

    @Override
    public void load(DataInputStream in) throws Exception {
        for (int i = 0; i < 2; i++) {
            String itemId = in.readUTF();
            int count = in.readInt();
            if(itemId == null || itemId.isEmpty() || count <= 0){
                inventory.clearSlot(i);
            }else{
                Item item = Items.get(itemId);
                if(item != null){
                    inventory.setItem(i, new ItemStack(item, count));
                }
            }
        }
        craftProgress = in.readInt();
        craftTime = in.readInt();
        isRunning = in.readBoolean();
    }
}
