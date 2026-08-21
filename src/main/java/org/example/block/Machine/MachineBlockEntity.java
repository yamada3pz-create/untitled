package org.example.block.Machine;

public class MachineBlockEntity extends  MachineEntity{

    public MachineBlockEntity(int x, int y) {
        super(x, y, 100);
    }

    @Override
    public String getType(){
        return "machine";
    }

    @Override
    public boolean canCraft(){
        return false; // Заглушка нечего не крафтит
    }

    @Override
    public void craft(){
        // Заглушка
    }
}

