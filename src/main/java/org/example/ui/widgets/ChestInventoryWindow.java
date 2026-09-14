package org.example.ui.widgets;

import org.example.inventory.ChestContainer;
import org.example.inventory.Container;
import org.example.inventory.Inventory;
import org.example.ui.ContainerWindow;

public class ChestInventoryWindow extends ContainerWindow {


    public ChestInventoryWindow(Inventory chestInventory, Inventory playerInventory) {
        super(new ChestContainer(chestInventory, playerInventory),
                240,80, 352,339, "Сундук");
        setBodyTexture("gui/container/chest");
    }

    public ChestContainer getChestContainer(){
        return(ChestContainer) container;
    }
}
