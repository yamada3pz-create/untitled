package org.example.core;

import org.example.Game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public abstract class GameState {

    protected Game game;

    // Вызывается при переходе на этот экран
    public void enter(Game game){
        this.game = game;
    }

    //Вызывается при выходе с экрана
    public void exit() {}

    // Логика каждый кадр
    public abstract void update();

    // Отрисовка
    public abstract void render(Graphics2D g2d);

    // Вход
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    public void mousePressed(MouseEvent e) {};
    public void mouseReleased(MouseEvent e) {};
    public void mouseDragged(MouseEvent e) {};
    public void mouseMoved(MouseEvent e) {};
    public void mouseWheelMoved(MouseWheelEvent e) {}

}
