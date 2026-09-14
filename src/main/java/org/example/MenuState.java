package org.example;

import org.example.core.GameState;
import org.example.core.TextureLoader;
import org.example.core.UI;
import org.example.ui.widgets.ButtonWidget;
import org.example.ui.widgets.Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// Главное меню. Кнопки — виджеты ButtonWidget; ввод идёт по цепочке
// Screen -> Widgets (dispatchMove/dispatchPress), как в Minecraft.
public class MenuState extends GameState {

    private int selectedIndex = 0;
    private final List<Widget> widgets = new ArrayList<>();

    @Override
    public void enter(org.example.Game g){
        super.enter(g);
        widgets.clear(); // позиции зависят от виртуального размера (UI.scale) — строим заново
        int w = 180, h = 24; // высота кнопки = высота текстуры (16px)
        int x = (UI.virtualW - w) / 2;
        int baseY = Math.min(300, UI.virtualH - 160);

        addButton("Одиночная игра", x, baseY, w, h, () -> game.setState(new WorldSelectState()));
        addButton("Сетевая игра (скоро)", x, baseY + (h + 14), w, h, () -> { });
        addButton("Настройки", x, baseY + 2 * (h + 14), w, h, () -> game.setState(new SettingsState()));
        addButton("Выход", x, baseY + 3 * (h + 14), w, h, () -> game.shutdown());
    }

    private void addButton(String text, int x, int y, int w, int h, Runnable action){
        ButtonWidget b = new ButtonWidget(x, y, w, h, text, new Color(40, 40, 40), Color.WHITE);
        b.setAction(() -> action.run());
        widgets.add(b);
    }

    @Override
    public void update(float dt) {
        // hover из позиции мыши
        dispatchMove(widgets);
    }

    @Override
    public void mousePressed(MouseEvent e){
        super.mousePressed(e);
        if(e.getButton() != MouseEvent.BUTTON1) return;
        dispatchPress(widgets, MouseEvent.BUTTON1);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            selectedIndex = (selectedIndex - 1 + widgets.size()) % widgets.size();
            syncSelectionToButtons();
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            selectedIndex = (selectedIndex + 1) % widgets.size();
            syncSelectionToButtons();
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
            Widget w = widgets.get(selectedIndex);
            if(w instanceof ButtonWidget) ((ButtonWidget) w).click();
        }
    }

    private void syncSelectionToButtons(){
        for(int i = 0; i < widgets.size(); i++){
            if(widgets.get(i) instanceof ButtonWidget b){
                b.setHovered(i == selectedIndex);
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        // Весь экран рисуем в виртуальных координатах и увеличиваем на UI.scale
        Graphics2D g = (Graphics2D) g2d.create();
        g.scale(UI.scale, UI.scale);

        drawBackground(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "My Game";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (UI.virtualW - fm.stringWidth(title)) / 2, 200);

        for(Widget w : widgets) w.render(g);

        g.dispose();
    }

    private void drawBackground(Graphics2D g){
        BufferedImage bg = TextureLoader.getGuiTexture("gui/background");
        if(bg != null){
            g.drawImage(bg, 0, 0, 1000, 1000, null);
        }else{
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 1000, 1000);
        }
    }
}
