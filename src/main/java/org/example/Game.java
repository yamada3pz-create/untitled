package org.example;

import org.example.core.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JPanel implements Runnable {

    private GameState currentState;
    private boolean inGame;

    public Game() {
        setPreferredSize(new Dimension(1000, 1000));
        setBackground(Color.BLACK);
        setFocusable(true);

        // Слушатели клавиатуры — делегируют текущему состоянию
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (currentState != null) currentState.keyPressed(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (currentState != null) currentState.keyReleased(e);
            }
        });

        // Слушатели мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(currentState != null) currentState.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if(currentState != null) currentState.mouseReleased(e);
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
                if(currentState != null) currentState.mouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if(currentState != null) currentState.mouseDragged(e);
            }
        });

        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (currentState != null) currentState.mouseWheelMoved(e);
            }
        });

        // Запускаемся с главного меню
        setState(new MenuState());

        Thread thread = new Thread(this);
        thread.start();
        inGame = true;
    }

    public void setState(GameState newState) {
        if (currentState != null) currentState.exit();
        currentState = newState;
        if (currentState != null) currentState.enter(this);
    }

    @Override
    public void run() {
        while (inGame) {
            if (currentState != null) currentState.update();
            repaint();
            try {
                Thread.sleep(1000 / 60);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentState != null) {
            currentState.render((Graphics2D) g);
        }
    }
}
