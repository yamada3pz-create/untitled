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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Экран выбора мира (стиль как в Minecraft):
 *  - миры — список по центру: 1 клик = выбрать, двойной клик = зайти;
 *  - внизу кнопки «Создать мир», «Переименовать», «Удалить», «Назад»;
 *  - Переименовать / Удалить действуют на выбранный кликом мир;
 *  - все координаты считаются от виртуального размера (UI.scale) каждый кадр.
 */
public class WorldSelectState extends GameState {

    private final ArrayList<String> worlds = new ArrayList<>();
    private int selected = -1;                  // выбранный мир (кликом/клавиатурой)

    private final List<ButtonWidget> rowButtons = new ArrayList<>();
    private final List<Widget> actionButtons = new ArrayList<>();
    private ButtonWidget createBtn;
    private ButtonWidget renameBtn;
    private ButtonWidget deleteBtn;
    private ButtonWidget backBtn;

    @Override
    public void enter(Game g){
        super.enter(g);
        refreshWorlds();
        ensureActionButtons();
        reposition();
    }

    private void refreshWorlds(){
        worlds.clear();
        rowButtons.clear();

        File savesDir = new File("saves");
        File[] dirs = savesDir.exists() ? savesDir.listFiles(File::isDirectory) : null;
        if(dirs != null){
            Arrays.sort(dirs, Comparator.comparingLong(File::lastModified).reversed());
            for(File d : dirs) worlds.add(d.getName());
        }

        int size = worlds.size();
        selected = (size == 0) ? -1 : Math.min(Math.max(selected, 0), size - 1);

        for(int i = 0; i < size; i++){
            rowButtons.add(new ButtonWidget(0, 0, 400, 36, worlds.get(i),
                    new Color(40, 40, 40), Color.WHITE));
        }
    }

    private void ensureActionButtons(){
        if(!actionButtons.isEmpty()) return;

        createBtn = new ButtonWidget(0, 0, 110, 44, "Создать мир", new Color(40, 40, 40), Color.WHITE);
        createBtn.setAction(() -> game.setState(new CreateWorldState()));
        actionButtons.add(createBtn);

        renameBtn = new ButtonWidget(0, 0, 110, 44, "Переименовать", new Color(40, 40, 40), Color.WHITE);
        renameBtn.setAction(() -> renameSelected());
        actionButtons.add(renameBtn);

        deleteBtn = new ButtonWidget(0, 0, 110, 44, "Удалить", new Color(40, 40, 40), Color.WHITE);
        deleteBtn.setAction(() -> deleteSelected());
        actionButtons.add(deleteBtn);

        backBtn = new ButtonWidget(0, 0, 110, 44, "Назад", new Color(40, 40, 40), Color.WHITE);
        backBtn.setAction(() -> game.setState(new MenuState()));
        actionButtons.add(backBtn);
    }

    // Пересчёт координат каждый кадр — от текущего виртуального размера (UI.scale)
    private void reposition(){
        int rowW = 400, rowH = 36;
        int rowX = (UI.virtualW - rowW) / 2;
        int rowY0 = Math.min(180, UI.virtualH - 150);

        for(int i = 0; i < rowButtons.size(); i++){
            rowButtons.get(i).setPosition(rowX, rowY0 + i * 45);
        }

        int btnW = 110, btnH = 44, gap = 6;
        int total = btnW * 4 + gap * 3;
        int x = (UI.virtualW - total) / 2;
        int y = UI.virtualH - 80;

        createBtn.setPosition(x, y);
        renameBtn.setPosition(x + (btnW + gap), y);
        deleteBtn.setPosition(x + 2 * (btnW + gap), y);
        backBtn.setPosition(x + 3 * (btnW + gap), y);
    }

    @Override
    public void update(float dt){
        reposition();
        // hover по кнопкам действий
        dispatchMove(actionButtons);

        // Ряды миров: подсветка = выбран или под курсором
        for(int i = 0; i < rowButtons.size(); i++){
            ButtonWidget r = rowButtons.get(i);
            r.setHovered(i == selected || r.isMouseOver(mouseX, mouseY));
        }
    }

    @Override
    public void mousePressed(MouseEvent e){
        super.mousePressed(e);
        if(e.getButton() != MouseEvent.BUTTON1) return;
        reposition();

        // Клик по ряду мира: 1 нажатие = выбрать, 2 нажатия = зайти
        int hit = rowAt(mouseX, mouseY);
        if(hit >= 0){
            selected = hit;
            if(e.getClickCount() >= 2){
                playWorld(hit);
                return;
            }
            return;
        }

        // Клик по кнопкам действий
        dispatchPress(actionButtons, MouseEvent.BUTTON1);
    }

    private int rowAt(int mx, int my){
        for(int i = 0; i < rowButtons.size(); i++){
            if(rowButtons.get(i).isMouseOver(mx, my)) return i;
        }
        return -1;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
            selected = Math.max(0, selected - 1);
        }
        if(e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
            selected = Math.min(worlds.size() - 1, selected + 1);
        }
        if(e.getKeyCode() == KeyEvent.VK_ENTER && selected >= 0){
            playWorld(selected);
        }
        if(e.getKeyCode() == KeyEvent.VK_N){
            game.setState(new CreateWorldState());
        }
        if(e.getKeyCode() == KeyEvent.VK_DELETE){
            deleteSelected();
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            game.setState(new MenuState());
        }
    }

    private void playWorld(int index){
        if(index < 0 || index >= worlds.size()) return;
        game.setState(new PlayingState(worlds.get(index), 0L));
    }

    private void renameSelected(){
        if(selected < 0 || selected >= worlds.size()) return;
        game.setState(new RenameWorldState(worlds.get(selected), this));
    }

    private void deleteSelected(){
        if(selected < 0 || selected >= worlds.size()) return;
        File dir = new File("saves", worlds.get(selected));
        if(dir.exists()) deleteRecursively(dir);
        refreshWorlds(); // список миров перестроится заново
    }

    private void deleteRecursively(File f){
        File[] files = f.listFiles();
        if(files != null){
            for(File c : files) deleteRecursively(c);
        }
        f.delete();
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
        String title = "Выбор мира";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (UI.virtualW - fm.stringWidth(title)) / 2, 130);

        if(worlds.isEmpty()){
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.PLAIN, 26));
            String msg = "Миров пока нет — создайте новый";
            g.drawString(msg, (UI.virtualW - g.getFontMetrics().stringWidth(msg)) / 2, 260);
        }

        for(ButtonWidget r : rowButtons) r.render(g);
        for(Widget w : actionButtons) w.render(g);

        g.dispose();
    }
}