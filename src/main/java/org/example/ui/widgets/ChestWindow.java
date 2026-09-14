package org.example.ui.widgets;

import org.example.inventory.Container;
import org.example.inventory.SingleChestContainer;
import org.example.inventory.Inventory;
import org.example.ui.ContainerWindow;

// Окно ТОЛЬКО сундука (27 ячеек) — справа от окна инвентаря игрока.
public class ChestWindow extends ContainerWindow {

    public ChestWindow(Inventory chestInventory, int x, int y) {
        super(new SingleChestContainer(chestInventory), x, y, 224, 160, "Сундук");
        setBodyTexture("gui/container/chest");
    }

    public SingleChestContainer getChestContainer(){
        return (SingleChestContainer) container;
    }
}
