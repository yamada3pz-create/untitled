package org.example.ui.widgets;

import org.example.inventory.Inventory;
import org.example.inventory.MachineContainer;
import org.example.ui.ContainerWindow;

// Окно машины: 3 слота (IN0/IN1/OUT) в один ряд.
public class MachineWindow extends ContainerWindow {

    public MachineWindow(Inventory machineInventory, int x, int y){
        super(new MachineContainer(machineInventory), x, y,
                16 + 3 * (org.example.inventory.Slot.SIZE + 4) , 24 + 16 + org.example.inventory.Slot.SIZE + 16, "Машина");
        setBodyTexture("gui/container/chest");
    }
}