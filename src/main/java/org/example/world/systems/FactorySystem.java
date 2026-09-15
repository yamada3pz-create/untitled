package org.example.world.systems;

import org.example.block.Machine.MachineEntity;
import org.example.world.World;

import java.util.ArrayList;
import java.util.List;

// Логика крафта вынесена из MachineEntity: данные остаются в энтити,
// машинами управляет система единой пачкой за тик.
public class FactorySystem {

    private final List<MachineEntity> machines = new ArrayList<>();

    public void clear(){
        machines.clear();
    }

    public void add(MachineEntity e){
        machines.add(e);
    }

    public void tick(World world){
        for(MachineEntity m : machines){
            // Рецепт машины могут поменять в настройках/через блок — подтягиваем из блока
            m.resolveRecipe(world);

            // Готовый выход выталкиваем: лента/сундук рядом забирают результат
            m.ejectOutput(world);

            if(!m.isRunning() && m.canCraft()){
                m.setRunning(true);
                m.setCraftProgress(0);
            }
            if(m.isRunning()){
                m.setCraftProgress(m.getCraftProgress() + 1);
                if(m.getCraftProgress() >= m.getCraftTime()){
                    m.craft();
                    m.setCraftProgress(0);
                    if(!m.canCraft()){
                        m.setRunning(false);
                    }
                }
            }
        }
    }
}