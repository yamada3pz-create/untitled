package org.example.recipe;

import org.example.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * Рецепт фабрики: набор входов, один выход и время крафта (в тиках).
 * Парсится из data/vanilla/recipes/*.json.
 */
public class Recipe {

    private final String id;
    private final List<ItemStack> inputs;
    private final ItemStack output;
    private final int craftTimeTicks;

    public Recipe(String id, List<ItemStack> inputs, ItemStack output, int craftTimeTicks){
        this.id = id;
        this.inputs = inputs;
        this.output = output;
        this.craftTimeTicks = craftTimeTicks;
    }

    public String getId(){ return id; }
    public List<ItemStack> getInputs(){ return inputs; }
    public ItemStack getOutput(){ return output; }
    public int getCraftTimeTicks(){ return craftTimeTicks; }

    /** Есть один входной предмет (первый из списка) — для простой машины на 1 слот. */
    public ItemStack getPrimaryInput(){
        return inputs.isEmpty() ? ItemStack.EMPTY : inputs.get(0);
    }

    @Override
    public String toString(){
        return "Recipe[" + id + " " + inputs + " -> " + output + " (" + craftTimeTicks + " ticks)]";
    }
}