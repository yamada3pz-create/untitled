package org.example.block;

import org.example.item.ItemStack;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;

/**
 * Конвейерная лента (item-версия; трубы с жидкостью живут отдельно).
 *
 * Ёмкость одной ленты — несколько "позиций" вдоль тайла (SLOTS_PER_BELT).
 * Пакетная модель: за тик вся линия сдвигается на одну позицию одновременно
 * (BeltSystem считает новое состояние по состоянию ДО тика). Предметы не
 * являются отдельными сущностями — это просто стаки в фиксированных слотах.
 *
 * Авто-углы: если соседний (перпендикулярный) конвейер направлен в эту ленту,
 * BeltSystem помечает её "corner" — визуально спрайт угловой, но перемещение
 * предметов идёт тем же линейным сдвигом.
 */
public class BeltBlockEntity extends BlockEntity {

    public static final int SLOTS_PER_BELT = 4;

    private final ItemStack[] slots = new ItemStack[SLOTS_PER_BELT];
    private Direction direction = Direction.SOUTH;
    private boolean corner = false;

    public BeltBlockEntity(int x, int y){
        super(x, y);
        Arrays.fill(slots, ItemStack.EMPTY);
    }

    public Direction getDirection(){ return direction; }
    public void setDirection(Direction direction){
        this.direction = direction;
    }

    public boolean isCorner(){ return corner; }
    public void setCorner(boolean corner){
        this.corner = corner;
    }

    public ItemStack getSlot(int i){
        return (i < 0 || i >= slots.length) ? ItemStack.EMPTY : slots[i];
    }

    public ItemStack getLastSlot(){
        return slots[slots.length - 1];
    }

    // Состояние линии: массив стаков (важно: возвращаем внутреннее состояние,
    // BeltSystem работает с ним двухфазно и мутирует только в фазе применения).
    public ItemStack[] getSlots(){
        return slots;
    }

    // Полностью свободная (пустая) лента
    public boolean isEmpty(){
        for(ItemStack s : slots){
            if(!s.isEmpty()) return false;
        }
        return true;
    }

    // Полностью заполненная
    public boolean isFull(){
        for(ItemStack s : slots){
            if(s.isEmpty()) return false;
        }
        return true;
    }

    // Есть ли место под новый стак
    public boolean hasFreeSlot(){
        for(ItemStack s : slots){
            if(s.isEmpty()) return true;
        }
        return false;
    }

    // Принять стак снаружи (машина/рука/добавление игроком):
    // кладём в первый пустой слот от входа. Возвращает принято ли целиком.
    public boolean receive(ItemStack stack){
        if(stack == null || stack.isEmpty()) return true;
        for(int i = 0; i < slots.length; i++){
            if(slots[i].isEmpty()){
                slots[i] = stack.copy();
                return true;
            }
        }
        return false;
    }

    @Override
    public String getType(){
        return "belt";
    }

    @Override
    public void save(DataOutputStream out) throws Exception{
        out.writeUTF(direction.name());
        out.writeBoolean(corner);
        for(ItemStack s : slots){
            if(s.isEmpty()){
                out.writeUTF("");
                out.writeInt(0);
            }else{
                out.writeUTF(s.getItem().getId());
                out.writeInt(s.getCount());
            }
        }
    }

    @Override
    public void load(DataInputStream in) throws Exception{
        direction = Direction.fromString(in.readUTF());
        corner = in.readBoolean();
        for(int i = 0; i < slots.length; i++){
            String itemId = in.readUTF();
            int count = in.readInt();
            if(itemId == null || itemId.isEmpty() || count <= 0){
                slots[i] = ItemStack.EMPTY;
            }else{
                org.example.item.Item item = org.example.item.Items.get(itemId);
                if(item != null) slots[i] = new ItemStack(item, count);
                else slots[i] = ItemStack.EMPTY;
            }
        }
    }
}