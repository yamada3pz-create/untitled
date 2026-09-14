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
 * Экран "Настройки графики": здесь лежит UI Scale (масштаб интерфейса).
 *
 * Стрелки < > меняют ТОЛЬКО предпросмотр значения (pendingScale). Реальный масштаб
 * применяется кнопкой «Применить» (или «Готово»). Так как координаты всех кнопок
 * пересчитываются КАЖДЫЙ кадр от текущего виртуального размера, сразу после
 * применения интерфейс выстраивается заново по центру — как будто мы снова вошли
 * в настройки графики. «Назад» возвращает без применения изменений.
 */
public class GraphicsSettingsState extends GameState {

    private int selectedIndex = 0;              // 0 = UI Scale, 1 = Назад, 2 = Применить, 3 = Готово
    private final String[] options = { "1x", "2x" };
    private static final int MAX_UI_SCALE = 2;  // окно 1000x1000: больше 2x меню не влезает
    private static final int BTN_COUNT = 4;     // строк: UI Scale + три кнопки

    private final List<Widget> widgets = new ArrayList<>();
    private ButtonWidget minusBtn;
    private ButtonWidget plusBtn;
    private ButtonWidget backBtn;
    private ButtonWidget applyBtn;
    private ButtonWidget doneBtn;

    private int pendingScale = 1;               // предпросмотр масштаба (ещё не применён)
    private final GameState returnTo;           // сюда вернуться (null = главное меню)

    public GraphicsSettingsState(){ this(null); }

    public GraphicsSettingsState(GameState returnTo){ this.returnTo = returnTo; }

    @Override
    public void enter(Game g){
        super.enter(g);
        pendingScale = Settings.uiScale; // заходим с текущим применённым масштабом

        // Кнопки строим один раз; позиции обновляются каждый кадр (reposition)
        if(widgets.isEmpty()){
            buildWidgets();
        }
        reposition();
    }

    private void buildWidgets(){
        minusBtn = new ButtonWidget(0, 0, 40, 16, "<", new Color(40, 40, 40), Color.WHITE);
        minusBtn.setAction(() -> setPendingScale(pendingScale - 1));
        widgets.add(minusBtn);

        plusBtn = new ButtonWidget(0, 0, 40, 16, ">", new Color(40, 40, 40), Color.WHITE);
        plusBtn.setAction(() -> setPendingScale(pendingScale + 1));
        widgets.add(plusBtn);

        backBtn = new ButtonWidget(0, 0, 96, 16, "Назад", new Color(40, 40, 40), Color.WHITE);
        backBtn.setAction(() -> backExit());
        widgets.add(backBtn);

        applyBtn = new ButtonWidget(0, 0, 96, 16, "Применить", new Color(40, 40, 40), Color.WHITE);
        applyBtn.setAction(() -> applyScale());
        widgets.add(applyBtn);

        doneBtn = new ButtonWidget(0, 0, 96, 16, "Готово", new Color(40, 40, 40), Color.WHITE);
        doneBtn.setAction(() -> applyAndExit());
        widgets.add(doneBtn);
    }

    // Пересчёт координат из ВИРТУАЛЬНОГО размера (то есть из применённого UI.scale).
    // Вызывается каждый кадр, поэтому при смене масштаба всё сразу перестраивается по центру.
    private void reposition(){
        int cx = UI.virtualW / 2;

        // Строка "UI Scale": [label][value][<][>] — одной группой по центру
        int labelW = 90, valW = 40, gap = 20, arrowW = 40;
        int total = labelW + gap + valW + gap + arrowW * 2;
        int startX = cx - total / 2;

        // Подписи (label/value) рисуются в render по тем же осям
        minusBtn.setPosition(startX + labelW + gap + valW + gap, 296);
        plusBtn.setPosition(startX + labelW + gap + valW + gap + arrowW, 296);

        // Кнопки Назад / Применить / Готово
        int btnW = 96, btnHeight = 16, btnGap = 8;
        int btnY = Math.min(360, UI.virtualH - 80);
        int btnStartX = (UI.virtualW - (btnW * 3 + btnGap * 2)) / 2;

        backBtn.setPosition(btnStartX, btnY);
        applyBtn.setPosition(btnStartX + (btnW + btnGap), btnY);
        doneBtn.setPosition(btnStartX + 2 * (btnW + btnGap), btnY);
    }

    @Override
    public void update(float dt){
        reposition(); // масштаб могли применить — кнопки уже выстроены под новые координаты
        // hover из позиции мыши по цепочке виджетов
        dispatchMove(widgets);
        // Наведённая кнопка становится выбранной
        if(backBtn.isMouseOver(mouseX, mouseY)) selectedIndex = 1;
        if(applyBtn.isMouseOver(mouseX, mouseY)) selectedIndex = 2;
        if(doneBtn.isMouseOver(mouseX, mouseY)) selectedIndex = 3;
    }

    @Override
    public void mousePressed(MouseEvent e){
        reposition();
        super.mousePressed(e);
        if(e.getButton() != MouseEvent.BUTTON1) return;
        dispatchPress(widgets, MouseEvent.BUTTON1);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                selectedIndex = Math.max(0, Math.min(BTN_COUNT - 1, selectedIndex - 1));
                syncSelectionToButtons();
                break;

            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                selectedIndex = Math.max(0, Math.min(BTN_COUNT - 1, selectedIndex + 1));
                syncSelectionToButtons();
                break;

            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                setPendingScale(pendingScale - 1);
                break;

            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                setPendingScale(pendingScale + 1);
                break;

            case KeyEvent.VK_ENTER:
            case KeyEvent.VK_SPACE:
                if (selectedIndex == 1) backExit();
                else if (selectedIndex == 2) applyScale();
                else if (selectedIndex == 3) applyAndExit();
                break;

            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_BACK_SPACE:
                backExit();
                break;
        }
    }

    private void syncSelectionToButtons(){
        backBtn.setHovered(selectedIndex == 1);
        applyBtn.setHovered(selectedIndex == 2);
        doneBtn.setHovered(selectedIndex == 3);
    }

    // Стрелки/клавиши меняют ТОЛЬКО предпросмотр, реальный масштаб не трогаем
    private void setPendingScale(int value) {
        pendingScale = Math.max(1, Math.min(MAX_UI_SCALE, value));
    }

    // «Применить» — применяем масштаб сразу; следующий же кадр перестроит кнопки по центру
    private void applyScale() {
        if (pendingScale != Settings.uiScale) {
            Settings.uiScale = pendingScale;
            UI.scale = pendingScale;
        }
    }

    // «Готово» — применяем, сохраняем и выходим
    private void applyAndExit() {
        applyScale();
        Settings.save();
        backExit();
    }

    // «Назад» — просто уходим, ничего не применяя (предпросмотр отбрасывается)
    private void backExit() {
        if(returnTo != null) game.setState(returnTo);
        else game.setState(new MenuState());
    }

    @Override
    public void render(Graphics2D g2d) {
        reposition();
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
        String header = "Настройки графики";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(header, (UI.virtualW - fm.stringWidth(header)) / 2, 150);

        // Строка UI Scale: label и value по тем же осям, что и стрелки в reposition()
        int cx = UI.virtualW / 2;
        int startX = cx - (90 + 20 + 40 + 20 + 80) / 2;

        g.setFont(new Font("Arial", Font.PLAIN, 28));
        g.setColor(selectedIndex == 0 ? Color.YELLOW : Color.WHITE);
        g.drawString("UI Scale", startX, 300);

        String current = (pendingScale >= 1 && pendingScale <= options.length)
                ? options[pendingScale - 1] : pendingScale + "x";
        g.setColor(selectedIndex == 0 ? Color.YELLOW : Color.CYAN);
        g.drawString(current, startX + 110, 305);

        // Стрелки и кнопки рисуются сами (виджеты)
        for (Widget w : widgets) w.render(g);

        g.dispose();
    }
}