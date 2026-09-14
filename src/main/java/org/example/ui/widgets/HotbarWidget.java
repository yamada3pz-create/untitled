package org.example.ui.widgets;

import org.example.core.TextureLoader;
import org.example.inventory.Inventory;
import org.example.item.ItemStack;
import org.example.ui.Anchor;

import java.awt.*;
import java.awt.image.BufferedImage;

public class HotbarWidget extends Widget {

    public static final int SLOTS = 9;

    private static final int SLOT_SIZE = 32;
    private static final int GAP = 4;
    private static final Color BG_COLOR = new Color(60, 60, 60);
    private static final Color BORDER_COLOR = new Color(100, 100, 100);
    private static final Color SELECTED_COLOR = new Color(220, 220, 120);

    private final Inventory container;
    private final int firstSlotIndex;
    private int selected;

    public HotbarWidget(Inventory container, int firstSlotIndex){
        // Размеры в "виртуальных" (UI) пикселях; реальный размер получится * UI.scale.
        // 9 слотов + 8 промежутков между ними.
        super(0, 0,
                9 * SLOT_SIZE + (9 - 1) * GAP,
                SLOT_SIZE);

        this.container = container;
        this.firstSlotIndex = firstSlotIndex;

        // Прижимаем хотбар к НИЗУ, по центру, с отступом 8px от нижнего края.
        // Всё позиционирование теперь делает Widget.updatePosition().
        setAnchor(Anchor.BOTTOM_CENTER);
        setOffset(0, 8);
    }

    public int getSelectedContainerIndex()  { return firstSlotIndex + selected; }
    public ItemStack getSelectedItem()      { return container.getStack(getSelectedContainerIndex()); }
    public int getSelected()                { return selected; }
    public void setSelected(int index)      { if(index >= 0 && index < SLOTS) selected = index; }

    public void selectByKey(int number){
        if(number >= 1 && number <= SLOTS) selected = number - 1;
    }

    public void scroll(int direction){
        selected = Math.floorMod(selected + direction, SLOTS);
    }

    /**
     * Рисует хотбар.
     * Вызывается из PlayingState.render как hotbar.render(g2d, screenW, screenH).
     *
     * Здесь НЕ считаем startX/y вручную: родительский Widget уже
     * применил anchor (BOTTOM_CENTER) через updatePosition(UI.virtualW, UI.virtualH)
     * и установил this.x / this.y в правильное место.
     */
    @Override
    public void render(Graphics2D g2d, int screenW, int screenH){
        // Родитель сам применяет масштаб (g2d.scale) и вычисляет x/y по anchor.
        // Поэтому внутри этого метода рисуем в виртуальных координатах,
        // а this.x / this.y уже указывают на место привязки.
        super.render(g2d, screenW, screenH);
    }

    /**
     * Собственно отрисовка слотов.
     * Родительский render(g2d, ...) вызывает этот метод в виртуальных
     * координатах и с уже настроенной шкалой (scale).
     */
    @Override
    public void render(Graphics2D g2d) {
        // this.x / this.y уже установлены в updatePosition (BOTTOM_CENTER).
        for(int i = 0; i < SLOTS; i++){
            int slotX = this.x + i * (SLOT_SIZE + GAP);
            int slotY = this.y;

            g2d.setColor(BG_COLOR);
            g2d.fillRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);
            g2d.setColor(i == selected ? SELECTED_COLOR : BORDER_COLOR);
            g2d.drawRect(slotX, slotY, SLOT_SIZE, SLOT_SIZE);

            ItemStack stack = container.getStack(firstSlotIndex + i);
            if(!stack.isEmpty()){
                BufferedImage tex = TextureLoader.getItemTexture(stack.getItem().getId());
                if(tex != null && tex != TextureLoader.getMissingTexture()){
                    g2d.drawImage(tex, slotX + 2, slotY + 2, SLOT_SIZE - 4, SLOT_SIZE - 4, null);
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.PLAIN, 11));
                    g2d.drawString(stack.getItem().getId(), slotX + 3, slotY + 13);
                }
                if(stack.getCount() > 1){
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 12));
                    g2d.drawString(String.valueOf(stack.getCount()), slotX + SLOT_SIZE - 14, slotY + SLOT_SIZE - 4);
                }
            }
        }
    }
}
