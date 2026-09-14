package org.example;

import org.example.core.GameState;
import org.example.core.TextureLoader;
import org.example.core.UI;
import org.example.ui.widgets.ButtonWidget;
import org.example.ui.widgets.TextFieldWidget;
import org.example.ui.widgets.Widget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

// Создание мира: два поля ввода — имя и сид (можно оставить пустым)
public class CreateWorldState extends GameState {

    private TextFieldWidget nameField;   // false-состояние: редактируем имя
    private TextFieldWidget seedField;   // true-состояние: редактируем сид

    private final List<Widget> widgets = new ArrayList<>();

    @Override
    public void enter(Game g){
        super.enter(g);
        widgets.clear(); // позиции зависят от виртуального размера (UI.scale) — строим заново

        // Поля ввода — по центру, при маленьком виртуальном экране поджимаем снизу
        int fieldW = 360, fieldH = 40;
        int fx = (UI.virtualW - fieldW) / 2;
        int fieldY = Math.min(300, UI.virtualH - 230);
        nameField = new TextFieldWidget(fx, fieldY, fieldW, fieldH, "Новый мир", 24);
        seedField = new TextFieldWidget(fx, fieldY + 85, fieldW, fieldH, "", 20);
        seedField.setCharFilter(c -> Character.isDigit(c));
        nameField.setFocused(true);

        int btnW = 260, btnH = 44;
        int bx = (UI.virtualW - btnW) / 2;
        int btnY = Math.min(500, UI.virtualH - 120);
        ButtonWidget createBtn = new ButtonWidget(bx, btnY, btnW, btnH, "Создать мир", new Color(40, 40, 40), Color.WHITE);
        createBtn.setAction(() -> finishCreation());

        ButtonWidget backBtn = new ButtonWidget(bx, btnY + 60, btnW, btnH, "Назад", new Color(40, 40, 40), Color.WHITE);
        backBtn.setAction(() -> game.setState(new WorldSelectState()));

        widgets.add(nameField);
        widgets.add(seedField);
        widgets.add(createBtn);
        widgets.add(backBtn);
    }

    @Override
    public void update(float dt) {
        // hover/фокус из позиции мыши
        dispatchMove(widgets);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){ game.setState(new WorldSelectState()); return; }

        if(e.getKeyCode() == KeyEvent.VK_TAB
                || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN){
            boolean next = !nameField.isFocused();
            nameField.setFocused(!next);
            seedField.setFocused(next);
            return;
        }

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            if(nameField.isFocused()){
                nameField.setFocused(false);
                seedField.setFocused(true);
                return;
            }
            finishCreation();
            return;
        }

        // Backspace и прочее — отдаём активному полю
        (nameField.isFocused() ? nameField : seedField).onKeyPressed(e.getKeyCode());
    }

    // Сюда падают обычные символы
    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if(c < 32 || c > 126) return; // служебные пропускаем
        if(nameField.isFocused()) nameField.typeChar(c);
        else seedField.typeChar(c);
    }

    @Override
    public void mousePressed(MouseEvent e){
        super.mousePressed(e);
        if(e.getButton() != MouseEvent.BUTTON1) return;
        dispatchPress(widgets, MouseEvent.BUTTON1);
    }

    private void finishCreation(){
        String clean = sanitize(nameField.getText());
        String seedText = seedField.getText();
        long seed = seedText.isEmpty() ? System.nanoTime() : Long.parseLong(seedText);
        // Если папка уже существует — просто продолжаем этот мир, ничего не удаляем
        game.setState(new PlayingState(clean, seed));
    }

    // Запрещённые в именах файлов символы
    private String sanitize(String s){
        s = s.trim();
        StringBuilder b = new StringBuilder();
        for(char c : s.toCharArray()){
            if("\\/:*?\"<>|".indexOf(c) < 0) b.append(c);
        }
        return b.length() > 0 ? b.toString() : "world";
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
        String title = "Создание мира";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (UI.virtualW - fm.stringWidth(title)) / 2, 200);

        // Подписи полей
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 26));
        g.drawString("Название:", nameField.getX(), nameField.getY() - 8);
        g.drawString("Сид:", seedField.getX(), seedField.getY() - 8);

        // Поля, кнопки рисуются сами
        for(Widget w : widgets) w.render(g);

        g.dispose();
    }
}