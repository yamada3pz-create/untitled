package org.example.block;

import org.example.inventory.SimpleContainer;
import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.item.Items;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ChestBlockEntity extends BlockEntity{

    private int slotCount = 27;
    private final SimpleContainer inventory = new SimpleContainer(slotCount);


    public ChestBlockEntity(int x, int y){
        super(x, y);
    }

    public SimpleContainer getInventory() { return inventory; }
    public int getSlotCount(){ return slotCount;}

    public void cleatSlot(int slot){
        inventory.clearSlot(slot);
    }


    @Override
    public String getType() {
        return "chest";
    }

    @Override
    public void save(DataOutputStream out) throws Exception{
        for (int i = 0; i < 27; i++) {
            ItemStack stack = inventory.getItem(i);
            if(stack.isEmpty()){
                out.writeUTF("");
                out.writeInt(0);
            }else{
                out.writeUTF(stack.getItem().getId());
                out.writeInt(stack.getCount());
            }
        }

    }

    @Override
    public void load(DataInputStream in) throws Exception{

        for (int i = 0; i < 27; i++) {
            String itemId = in.readUTF();
            int count = in.readInt();
            if(itemId == null || itemId.isEmpty() || count <=0){
                inventory.clearSlot(i);
            }else {
                Item item = Items.get(itemId);
                if(item != null){
                    inventory.setItem(i, new ItemStack(item, count));
                }
            }
        }
    }
}
