package org.example.block.Machine;

import org.example.block.Block;
import org.example.block.Blocks;
import org.example.block.InventoryBlockEntity;
import org.example.item.ItemStack;
import org.example.recipe.Recipe;
import org.example.recipe.Recipes;
import org.example.world.World;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * Машина: 3 слота — 0/1 вход (под рецепт), 2 выход.
 * Рецепт приходит из блока (поле "recipe" в block JSON).
 */
public abstract class MachineEntity extends InventoryBlockEntity {

    private static final int INPUT0 = 0;
    private static final int INPUT1 = 1;
    private static final int OUTPUT = 2;

    private int craftProgress;
    private boolean isRunning;
    private String recipeId;

    public MachineEntity(int x, int y){
        super(x, y, 3);
        this.craftProgress = 0;
        this.isRunning = false;
        this.recipeId = "";
    }

    public ItemStack getInput0(){ return getInventory().getStack(INPUT0); }
    public ItemStack getInput1(){ return getInventory().getStack(INPUT1); }
    public ItemStack getOutput(){ return getInventory().getStack(OUTPUT); }

    public void setInput0(ItemStack slot){ getInventory().setStack(INPUT0, slot); }
    public void setInput1(ItemStack slot){ getInventory().setStack(INPUT1, slot); }
    public void setOutput(ItemStack slot){ getInventory().setStack(OUTPUT, slot); }

    /** Рецепт из реестра по назначенному id (поле "recipe" блока машины). */
    public Recipe getRecipe(){
        return recipeId == null || recipeId.isEmpty() ? null : Recipes.get(recipeId);
    }

    public void setRecipeId(String id){
        this.recipeId = id == null ? "" : id;
    }

    /** Если рецепт ещё не назначен — берём из блока, стоящего на этом тайле. */
    public void resolveRecipe(World world){
        if(recipeId != null && !recipeId.isEmpty()) return;
        Block block = Blocks.get(world.getObjectIdAt(getX(), getY()));
        if(block != null && !block.getRecipe().isEmpty()){
            recipeId = block.getRecipe();
        }
    }

    public int getCraftProgress(){ return craftProgress; }
    public void setCraftProgress(int v){ craftProgress = v; }

    /** Время крафта — из рецепта (или 40 по умолчанию). */
    public int getCraftTime(){
        Recipe r = getRecipe();
        return r != null ? r.getCraftTimeTicks() : 40;
    }

    public boolean isRunning(){ return isRunning; }
    public void setRunning(boolean v){ isRunning = v; }

    private ItemStack getStack(int slot){ return getInventory().getStack(slot); }

    /** Есть ли нужное количество входных предметов по рецепту. */
    public boolean canCraft(){
        Recipe recipe = getRecipe();
        if(recipe == null) return false;
        boolean s0Used = false, s1Used = false;

        for(ItemStack need : recipe.getInputs()){
            if(need == null || need.isEmpty()) continue;
            boolean found = false;
            if(!s0Used && hasEnoughInSlot(INPUT0, need)){ s0Used = true; found = true; }
            else if(!s1Used && hasEnoughInSlot(INPUT1, need)){ s1Used = true; found = true; }
            if(!found) return false;
        }

        // Место под выход
        ItemStack out = getOutput();
        ItemStack prod = recipe.getOutput();
        if(!out.isEmpty()
                && !(out.getItem().getId().equals(prod.getItem().getId())
                     && out.getCount() + prod.getCount() <= out.getMaxStackSize())){
            return false;
        }
        return true;
    }

    protected boolean hasEnoughInSlot(int slot, ItemStack need){
        if(need == null || need.isEmpty()) return true;
        ItemStack have = getInventory().getStack(slot);
        return !have.isEmpty()
                && have.getItem().getId().equals(need.getItem().getId())
                && have.getCount() >= need.getCount();
    }

    /** Списывает входные предметы и кладёт выход. Каждый вызов = один крафт. */
    public void craft(){
        Recipe recipe = getRecipe();
        if(recipe == null) return;
        boolean s0Used = false, s1Used = false;

        for(ItemStack need : recipe.getInputs()){
            if(need == null || need.isEmpty()) continue;
            int slot = -1;
            if(!s0Used && hasEnoughInSlot(INPUT0, need)){ slot = INPUT0; s0Used = true; }
            else if(!s1Used && hasEnoughInSlot(INPUT1, need)){ slot = INPUT1; s1Used = true; }
            if(slot < 0) return; // нечего списывать

            ItemStack have = getInventory().getStack(slot);
            have.shrink(need.getCount());
            if(have.getCount() <= 0) getInventory().clearSlot(slot);
        }

        ItemStack out = getOutput();
        ItemStack prod = recipe.getOutput().copy();
        if(out.isEmpty()){
            setOutput(prod);
        }else{
            out.grow(prod.getCount());
        }
    }

    // ── Выталкивание результата ─────────────────────────────
    // Готовый стак в слоте OUTPUT ищет приёмник среди соседей:
    // сначала конвейерная лента, затем инвентарь (сундук).
    public void ejectOutput(World world){
        ItemStack out = getOutput();
        if(out == null || out.isEmpty()) return;

        for(org.example.block.Direction d : org.example.block.Direction.values()){
            int nx = getX() + d.getOffsetX();
            int ny = getY() + d.getOffsetY();

            org.example.block.BlockEntity e = world.getBlockEntityAt(nx, ny);
            if(e instanceof org.example.block.BeltBlockEntity belt){
                if(belt.hasFreeSlot()){
                    belt.receive(out);
                    clearOutput(out);
                    return;
                }
                continue;
            }
            if(e instanceof InventoryBlockEntity invEntity){
                // кладём в сундук/инвентарь
                int remaining = invEntity.getInventory().addItem(out.getItem(), out.getCount());
                out.setCount(remaining);
                if(remaining <= 0) clearOutput(out);
            }
        }
    }

    private void clearOutput(ItemStack out){
        if(out.getCount() <= 0){
            getInventory().clearSlot(OUTPUT);
        }
    }

    // ── Приём с конвейера ───────────────────────────────────
    // Лента кладёт предмет в слот входа, если он нужен рецепту и есть место.
    public boolean canAcceptInput(ItemStack stack){
        if(stack == null || stack.isEmpty()) return false;
        for(ItemStack need : recipeInputs()){
            if(need.getItem().getId().equals(stack.getItem().getId())){
                // входы: слот может быть пустым или частично заполненным тем же предметом
                return inputSlotWithSpace(stack);
            }
        }
        return false;
    }

    private java.util.List<ItemStack> recipeInputs(){
        Recipe r = getRecipe();
        if(r == null) return java.util.List.of();
        return r.getInputs();
    }

    private boolean inputSlotWithSpace(ItemStack stack){
        for(int slot : new int[]{INPUT0, INPUT1}){
            ItemStack have = getStack(slot);
            if(have.isEmpty()) return true;
            if(have.getItem().getId().equals(stack.getItem().getId())
                    && have.getCount() < have.getMaxStackSize()) return true;
        }
        return false;
    }

    public void receiveInput(ItemStack stack){
        if(stack == null || stack.isEmpty()) return;
        int remaining = stack.getCount();
        for(int slot : new int[]{INPUT0, INPUT1}){
            ItemStack have = getStack(slot);
            if(have.isEmpty()){
                setStack(slot, new ItemStack(stack.getItem(), Math.min(remaining, stack.getMaxStackSize())));
                remaining -= Math.min(remaining, stack.getMaxStackSize());
            }else if(have.getItem().getId().equals(stack.getItem().getId())){
                int put = Math.min(remaining, have.getMaxStackSize() - have.getCount());
                have.grow(put);
                remaining -= put;
            }
            if(remaining <= 0) return;
        }
    }

    private void setStack(int slot, ItemStack s){ getInventory().setStack(slot, s); }

    @Override
    public void save(DataOutputStream out) throws Exception{
        super.save(out);
        out.writeUTF(recipeId == null ? "" : recipeId);
        out.writeInt(craftProgress);
        out.writeBoolean(isRunning);
    }

    @Override
    public void load(DataInputStream in) throws Exception{
        super.load(in);
        recipeId = in.readUTF();
        craftProgress = in.readInt();
        isRunning = in.readBoolean();
    }
}