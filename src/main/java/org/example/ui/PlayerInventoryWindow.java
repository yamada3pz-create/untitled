package org.example.ui;

import org.example.inventory.Inventory;
import org.example.inventory.PlayerContainer;
import org.example.ui.widgets.SmallWindow;

public class PlayerInventoryWindow extends ContainerWindow {
    SmallWindow smallWindow;

    public PlayerInventoryWindow(Inventory playerInventory){
        super(new PlayerContainer(playerInventory), 200, 150, 320, 210, " Инвентарь");
        setBodyTexture("gui/container/inventory");
    }

    public PlayerContainer getPlayerContainer(){
        return (PlayerContainer) container;
    }
}
