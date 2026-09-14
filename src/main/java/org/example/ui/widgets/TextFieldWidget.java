package org.example.ui.widgets;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.function.IntPredicate;

/**
 * Переиспользуемое поле ввода текста (аналог MC EditBox).
 *
 * - typeChar(char) — ввод символа с фильтром и лимитом длины;
 * - deleteChar() / onKeyPressed(keyCode) — удаление по Backspace;
 * - onMousePressed — клик забирает фокус у других полей;
 * - render — рамка + текст + мигающая каретка.
 */
public class TextFieldWidget extends Widget {

    private String text;
    private final int maxLength;
    private boolean focused;
    private IntPredicate charFilter; // null = принимать любые символы

    public TextFieldWidget(int x, int y, int width, int height, String text, int maxLength){
        super(x, y, width, height);
        this.text = (text == null) ? "" : text;
        this.maxLength = maxLength;
        this.focused = false;
        this.charFilter = null;
    }

    public String getText() { return text; }

    public void setText(String text) { this.text = (text == null) ? "" : text; }

    public boolean isFocused() { return focused; }

    public void setFocused(boolean focused) { this.focused = focused; }

    /** Ограничение допустимых символов (например, только цифры для сида). */
    public void setCharFilter(IntPredicate filter) { this.charFilter = filter; }

    /** Ввести символ (если проходит фильтр и не превышен лимит длины). */
    public void typeChar(char c){
        if(text.length() >= maxLength) return;
        if(charFilter != null && !charFilter.test(c)) return;
        text += c;
    }

    /** Удалить последний символ. */
    public void deleteChar(){
        if(!text.isEmpty()) text = text.substring(0, text.length() - 1);
    }

    /** Обработка клавиш (вызывается из GameState.keyPressed у сфокусированного поля). */
    public void onKeyPressed(int keyCode){
        if(keyCode == KeyEvent.VK_BACK_SPACE) deleteChar();
    }

    @Override
    public boolean onMousePressed(int mx, int my, int button){
        if(!isMouseOver(mx, my)) return false;
        if(button == MouseEvent.BUTTON1){
            focused = true;
            return true; // клик поглощён полем
        }
        return false;
    }

    @Override
    public void render(Graphics2D g2d){
        if(!visible) return;

        // Подложка
        g2d.setColor(focused ? new Color(45, 45, 45) : new Color(28, 28, 28));
        g2d.fillRect(x, y, width, height);

        // Рамка (жёлтая при фокусе)
        g2d.setColor(focused ? Color.YELLOW : Color.LIGHT_GRAY);
        g2d.drawRect(x, y, width, height);

        // Текст + мигающая каретка
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 26));
        boolean cursorOn = focused && (System.currentTimeMillis() % 1000) < 500;
        g2d.drawString(text + (cursorOn ? "_" : ""), x + 10, y + height - 12);
    }
}