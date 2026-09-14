package org.example.core;

import org.example.Game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public abstract class GameState {

    protected Game game;

    // Текущие координаты курсора мыши (обновляются базовыми обработчиками)
    protected int mouseX = -1;
    protected int mouseY = -1;

    // Вызывается при переходе на этот экран
    public void enter(Game game){
        this.game = game;
    }

    //Вызывается при выходе с экрана
    public void exit() {}

    // Логика каждый кадр
    public abstract void update(float dt);

    // Отрисовка
    public abstract void render(Graphics2D g2d);

    // Фиксированная игровая логика - вызывается ровно 20 раз в секунду
    public void tick() {}

    // Вход
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    // Координаты мыши храним в ВИРТУАЛЬНЫХ (UI) пикселях: экран делится на UI.scale.
    // Все экраны рисуются в том же масштабе, поэтому hit()/dispatch* попадают в виджеты
    // верно при любом scale. Дальнейший перевод в реальные пиксели делает Graphics scale.
    public void mousePressed(MouseEvent e) { mouseX = UI.unscaled(e.getX()); mouseY = UI.unscaled(e.getY()); };
    public void mouseReleased(MouseEvent e) { mouseX = UI.unscaled(e.getX()); mouseY = UI.unscaled(e.getY()); };
    public void mouseDragged(MouseEvent e) { mouseX = UI.unscaled(e.getX()); mouseY = UI.unscaled(e.getY()); };
    public void mouseMoved(MouseEvent e) { mouseX = UI.unscaled(e.getX()); mouseY = UI.unscaled(e.getY()); };
    public void mouseWheelMoved(MouseWheelEvent e) {}

    // Попадание точки (курсора) в прямоугольник
    protected boolean hit(int x, int y, int w, int h){
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    // ── Цепочка обработки ввода по виджетам (как в Minecraft: Screen -> Widgets) ──

    /**
     * Распределяет наведение мыши по списку виджетов (обновляет hover).
     */
    protected void dispatchMove(java.util.List<org.example.ui.widgets.Widget> widgets){
        if(widgets == null) return;
        for(org.example.ui.widgets.Widget w : widgets){
            w.onMouseMoved(mouseX, mouseY);
        }
    }

    /**
     * Распределяет клик по виджетам. Возвращает true, если клик "поглощён"
     * одним из виджетов (и его не должен обрабатывать мир/окно дальше).
     */
    protected boolean dispatchPress(java.util.List<org.example.ui.widgets.Widget> widgets, int button){
        if(widgets == null) return false;
        for(org.example.ui.widgets.Widget w : widgets){
            if(w.onMousePressed(mouseX, mouseY, button)) return true;
        }
        return false;
    }

}
