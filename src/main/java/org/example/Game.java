package org.example;

import org.example.block.BlockEntity;
import org.example.block.BlockEntityType;
import org.example.core.GameState;
import org.example.core.Settings;
import org.example.core.TextureLoader;
import org.example.core.UI;
import org.example.data.DataLoader;

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

            @Override
            public void keyTyped(KeyEvent e) { if (currentState != null) currentState.keyTyped(e); }
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

        // Загружаем настройки (UI scale, разрешение) из settings.txt
        Settings.load();
        // Применяем сохранённый масштаб интерфейса к глобальному UI
        UI.scale = Settings.uiScale;
        if (UI.scale > UI.MAX_SCALE) { // старые сейвы могли содержать 3x — подрезаем
            UI.scale = UI.MAX_SCALE;
            Settings.uiScale = UI.MAX_SCALE;
        }
        UI.update(1000, 1000); // виртуальные размеры известны уже при создании состояний

        DataLoader.loadAll();
        BlockEntityType.init();
        TextureLoader.preloadAll();

        // Запускаемся с главного меню
        setState(new MenuState());

        Thread thread = new Thread(this);
        thread.start();
        inGame = true;
    }

    public void setState(GameState newState) {
        if (currentState != null) currentState.exit();
        // enter() до присвоения currentState: поток игрового цикла не должен видеть новое
        // состояние с ещё не построенными виджетами (гонка давала NPE в update/render).
        if (newState != null) newState.enter(this);
        currentState = newState;
    }

    // Корректное завершение: сохранить и погасить цикл
    public void shutdown(){
        if(currentState != null) currentState.exit();
        inGame = false;
        System.exit(0);
    }

    // --- Тик-система: логика 20 раз/сек, рендер каждый кадр ---
    public static final float SECONDS_PER_TICK = 1f / 20f;

    private float tickAccumulator = 0f;
    private float tickAlpha = 0f; // доля текущего тика (0..1) для интерполяции

    public float getTickAlpha(){ return tickAlpha; }

    // Игровой цикл
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        while (inGame) {
            long nowTime = System.nanoTime();

            float frameTime = (nowTime - lastTime) / 1_000_000_000f;
            lastTime = nowTime;

            // После лагов/паузы не догоняем больше 5 тиков за кадр
            if (frameTime > 5 * SECONDS_PER_TICK) frameTime = 5 * SECONDS_PER_TICK;

            // Фиксированные тики игровой логики
            tickAccumulator += frameTime;
            while (tickAccumulator >= SECONDS_PER_TICK) {
                if (currentState != null) currentState.tick();
                tickAccumulator -= SECONDS_PER_TICK;
            }
            tickAlpha = tickAccumulator / SECONDS_PER_TICK;

            // Покадровое обновление (камера, UI)
            if (currentState != null) currentState.update(frameTime);
            repaint();

            long elapsed = (System.nanoTime() - nowTime) / 1_000_000;
            try {
                Thread.sleep(Math.max(0, 16 - elapsed));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentState != null) {
            UI.update(getWidth(), getHeight());
            currentState.render((Graphics2D) g);
        }
    }
}
