package org.example.inventory;

// Контейнер машины: 3 слота в ряд — I0, I1, O.
public class MachineContainer extends Container {

    public static final int MACHINE_SLOTS = 3;

    private final Inventory machine;

    public MachineContainer(Inventory machine){
        this.machine = machine;
        for(int i = 0; i < MACHINE_SLOTS; i++) addSlot(new Slot(machine, i, 0, 0));
    }

    @Override
    protected Inventory getPrimaryInventory(){ return machine; }

    @Override
    public void updateSlotPositions(int winX, int winY){
        final int pad = 16;
        final int gap = 4;
        int titleBar = 24;

        for(int i = 0; i < MACHINE_SLOTS; i++){
            int col = i % 3;
            slots.get(i).setX(winX + pad + col * (Slot.SIZE + gap));
            slots.get(i).setY(winY + titleBar + pad);
        }
    }
}