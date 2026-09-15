package org.example.block.Machine;

public class MachineBlockEntity extends MachineEntity {

    public MachineBlockEntity(int x, int y) {
        super(x, y);
    }

    @Override
    public String getType(){
        return "machine";
    }

    // canCraft/craft реализованы в MachineEntity
    // (рецепт назначается из поля "recipe" блока через resolveRecipe)
}