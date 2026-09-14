package org.example;

import org.example.core.GameState;
import org.example.core.Settings;
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

/**
 * Экран "Настройки" — список категорий (как в Minecraft: каждый пункт открывает
 * свой экран). Пока есть одна категория — "Настройки графики" (UI Scale).
 */
public class SettingsState extends GameState {

    private int selectedIndex = 0;              // 0 = Настройки графики, 1 = Назад
    private final List<Widget> widgets = new ArrayList<>();
    private ButtonWidget graphicsRow;
    private ButtonWidget backBtn;

    private final GameState returnTo;           // сюда вернуться кнопкой "Назад" (null = главное меню)

    public SettingsState(){
        this.returnTo = null;
    }

    public SettingsState(GameState returnTo){
        this.returnTo = returnTo;
    }

    @Override
    public void enter(Game g){
        super.enter(g);
        rebuildLayout(); // позиции зависят от виртуального размера (UI.scale)
    }

    private void rebuildLayout(){
        widgets.clear();

        int rowW = 400, rowH = 36;
        int rowX = (UI.virtualW - rowW) / 2;
        graphicsRow = new ButtonWidget(rowX, 260, rowW, rowH, "Настройки графики", new Color(40, 40, 40), Color.WHITE);
        graphicsRow.setAction(() -> game.setState(new GraphicsSettingsState(this)));
        widgets.add(graphicsRow);

        int actW = 220, actH = 44;
        int actX = (UI.virtualW - actW) / 2;
        backBtn = new ButtonWidget(actX, UI.virtualH - 180, actW, actH, "Назад", new Color(40, 40, 40), Color.WHITE);
        backBtn.setAction(() -> saveAndExit());
        widgets.add(backBtn);
    }

    @Override
    public void update(float dt){
        // hover из позиции мыши по цепочке виджетов
        dispatchMove(widgets);
        // Наведённый мышью пункт становится выбранным
        if(graphicsRow.isMouseOver(mouseX, mouseY)) selectedIndex = 0;
        if(backBtn.isMouseOver(mouseX, mouseY)) selectedIndex = 1;
    }

    @Override
    public void mousePressed(MouseEvent e){
        super.mousePressed(e);
        if(e.getButton() != MouseEvent.BUTTON1) return;
        dispatchPress(widgets, MouseEvent.BUTTON1);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                selectedIndex = Math.max(0, Math.min(widgets.size() - 1, selectedIndex - 1));
                syncSelectionToButtons();
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                selectedIndex = Math.max(0, Math.min(widgets.size() - 1, selectedIndex + 1));
                syncSelectionToButtons();
                break;

            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                if (selectedIndex == 0) graphicsRow.click();
                else if (selectedIndex == 1) backBtn.click();
                break;

            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_BACK_SPACE:
                saveAndExit();
                break;
        }
    }

    private void syncSelectionToButtons(){
        graphicsRow.setHovered(selectedIndex == 0);
        backBtn.setHovered(selectedIndex == 1);
    }

    private void saveAndExit() {
        Settings.save();
        if(returnTo != null) game.setState(returnTo);
        else game.setState(new MenuState());
    }

    @Override
    public void render(Graphics2D g2d) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.scale(UI.scale, UI.scale);

        BufferedImage bg = TextureLoader.getGuiTexture("gui/background");
        if(bg != null){
            g.drawImage(bg, 0, 0, 1000, 1000, null);
        }else{
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 1000, 1000);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        String header = "Настройки";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(header, (UI.virtualW - fm.stringWidth(header)) / 2, 150);

        for (Widget w : widgets) w.render(g);

        g.dispose();
    }
}