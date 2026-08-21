package org.example;

import org.example.core.GameState;

import java.awt.*;
import java.awt.event.KeyEvent;

public class MenuState extends GameState {

    private int selectedIndex = 0;

    private final String[] buttons = {
            "Новая игра",
            "Выход"
    };

    @Override
    public void update() {
        // В меню ничего не обновляется каждый кадр
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
            selectedIndex--;
            if (selectedIndex < 0) {
                selectedIndex = buttons.length - 1;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
            selectedIndex++;
            if (selectedIndex >= buttons.length) {
                selectedIndex = 0;
            }
        }
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (selectedIndex == 0) {
                // Новая игра
                game.setState(new PlayingState());
            }
            if (selectedIndex == 1) {
                // Выход
                System.exit(0);
            }
        }
    }

    @Override
    public void render(Graphics2D g2d) {
        // Фон
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, 1000, 1000);

        // Заголовок
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.drawString("My Game", 400, 200);

        // Кнопки
        g2d.setFont(new Font("Arial", Font.PLAIN, 32));
        for (int i = 0; i < buttons.length; i++) {
            if (i == selectedIndex) {
                g2d.setColor(Color.YELLOW);
            } else {
                g2d.setColor(Color.WHITE);
            }
            g2d.drawString(buttons[i], 420, 350 + i * 50);
        }

        // Подсказка
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("W/S - выбор | Enter - начать", 350, 500);
    }
}
