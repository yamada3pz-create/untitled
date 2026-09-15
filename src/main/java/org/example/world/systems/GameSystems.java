package org.example.world.systems;

import org.example.block.BeltBlockEntity;
import org.example.block.Machine.MachineEntity;
import org.example.block.PipeBlockEntity;
import org.example.world.World;

// Контейнер всех систем. Каждый тик сначала sync() сверяет списки
// с картами блок-энтити загруженных чанков (установка/ломание/загрузка/
// выгрузка регионов подхватываются автоматически), затем tick() обрабатывает их.
public class GameSystems {

    private final FactorySystem factory = new FactorySystem();
    private final ConveyorSystem conveyor = new ConveyorSystem();
    private final BeltSystem belt = new BeltSystem();

    public void sync(World world){
        factory.clear();
        conveyor.clear();
        belt.clear();
        world.forEachLoadedBlockEntity(e -> {
            if(e instanceof PipeBlockEntity pipe){
                conveyor.add(pipe);
            }else if(e instanceof MachineEntity machine){
                factory.add(machine);
            }else if(e instanceof BeltBlockEntity b){
                belt.add(b);
            }
        });
        belt.resolveCorners(world);
    }

    public void tick(World world){
        // Порядок важен: сначала конвейеры двигают жидкость, потом заводы её потребляют
        conveyor.tick(world);
        belt.tick(world);
        factory.tick(world);
    }
}