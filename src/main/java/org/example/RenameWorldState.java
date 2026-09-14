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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Переименование мира: поле с текущим именем + кнопки «Готово» / «Назад».
 * После подтверждения переименовывает папку saves/oldName и возвращает
 * в экран выбора мира (тот пересканирует список).
 */
public class RenameWorldState extends GameState {

    private final String oldName;
    private final GameState returnTo;
    private TextFieldWidget nameField;
    private final List<Widget> widgets = new ArrayList<>();
    private ButtonWidget doneBtn;
    private ButtonWidget backBtn;

    public RenameWorldState(String oldName, GameState returnTo){
        this.oldName = oldName;
        this.returnTo = returnTo;
    }

    @Override
    public void enter(Game g){
        super.enter(g);
        if(widgets.isEmpty()){
            nameField = new TextFieldWidget(0, 0, 360, 40, oldName, 24);
            nameField.setFocused(true);
            nameField.setCharFilter(c -> c >= 32 && c <= 126 && "\\/:*?\"<>|".indexOf(c) < 0);

            doneBtn = new ButtonWidget(0, 0, 110, 44, "Готово", new Color(40, 40, 40), Color.WHITE);
            doneBtn.setAction(() -> rename());

            backBtn = new ButtonWidget(0, 0, 110, 44, "Назад", new Color(40, 40, 40), Color.WHITE);
            backBtn.setAction(() -> back());

            widgets.add(nameField);
            widgets.add(doneBtn);
            widgets.add(backBtn);
        }
        reposition();
    }

    private void reposition(){
        int fw = 360, fh = 40;
        int fx = (UI.virtualW - fw) / 2;
        int fieldY = Math.min(300, UI.virtualH - 230);
        nameField.setPosition(fx, fieldY);

        int bw = 110, bh = 44, gap = 8;
        int total = bw * 2 + gap;
        int x = (UI.virtualW - total) / 2;
        int btnY = Math.min(500, UI.virtualH - 120);
        backBtn.setPosition(x, btnY);
        doneBtn.setPosition(x + bw + gap, btnY);
    }

    @Override
    public void update(float dt){
        reposition();
        dispatchMove(widgets);
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
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){ back(); return; }
        if(e.getKeyCode() == KeyEvent.VK_ENTER){ rename(); return; }
        nameField.onKeyPressed(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        nameField.typeChar(e.getKeyChar());
    }

    private void rename(){
        String clean = sanitize(nameField.getText());
        if(clean.isEmpty()) clean = "world";

        if(!clean.equals(oldName)){
            File oldDir = new File("saves", oldName);
            File newDir = new File("saves", clean);
            if(oldDir.exists() && !newDir.exists()){
                oldDir.renameTo(newDir);
            }
        }
        back();
    }

    private void back(){
        if(returnTo != null) game.setState(returnTo);
        else game.setState(new MenuState());
    }

    private String sanitize(String s){
        s = s.trim();
        StringBuilder b = new StringBuilder();
        for(char c : s.toCharArray()){
            if("\\/:*?\"<>|".indexOf(c) < 0 && c >= 32 && c <= 126) b.append(c);
        }
        return b.toString();
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
        String title = "Переименовать мир";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (UI.virtualW - fm.stringWidth(title)) / 2, 200);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 26));
        g.drawString("Название:", nameField.getX(), nameField.getY() - 8);

        for(Widget w : widgets) w.render(g);

        g.dispose();
    }
}