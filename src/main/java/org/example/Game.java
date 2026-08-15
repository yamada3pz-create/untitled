package org.example;

import org.example.core.Camera;
import org.example.core.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.HashSet;
import java.util.Set;

public class Game extends JPanel implements Runnable {

    private final Camera camera;
    private float playerX = 0.0f;
    private float playerY = 0.0f;

    private boolean inGame;
    private final Set<Integer> pressedKeys = new HashSet<>();

    public Game(){

        setPreferredSize(new Dimension(1000,1000));
        setBackground(Color.BLACK);
        setFocusable(true);

        camera = new Camera(1000,1000);
        camera.follow(-500,-500);

        // Создание файловой системы
        ResourceManager.initFolderStructure();

        // Подключаем слушатель клавиатуры
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                pressedKeys.add(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                pressedKeys.remove(e.getKeyCode());
            }
        });
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                super.mouseWheelMoved(e);
                int rotation = e.getWheelRotation();
                if(rotation < 0){
                    camera.zoomIn(0.2f);
                }else{
                    camera.zoomOut(0.2f);
                }
            }
        });

        Thread thread = new Thread(this);
        thread.start();
        inGame = true;
    }


    @Override
    public void run() {
        while (inGame){

            update();
            repaint();

            try{
                Thread.sleep(1000/60);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void update(){
        float speed = 5.0f; // Скорость движения по изометрической сетке

        // Управление игроком в изометрии (диагональные оси).
        // Накапливаем суммарный сдвиг за кадр — так move() видит полный вектор и определяет 8 направлений
        float dx = 0, dy = 0;
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) {
            dy -= speed;
        }
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) {
            dy += speed;
        }
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) {
            dx -= speed;
        }
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            dx += speed;
        }
        playerX += dx;
        playerY += dy;
        camera.follow(playerX, playerY);


    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 1. Сохраняем стандартную матрицу трансформации (чтобы потом вернуть всё назад)
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        // 2. ПОЛУЧАЕМ ДАННЫЕ ИЗ КАМЕРЫ
        float zoom = camera.getZoom();
        float camX = camera.getX();
        float camY = camera.getY();

        // 3. ПРИМЕНЯЕМ ТРАНСФОРМАЦИЮ КАМЕРЫ (Порядок команд ОЧЕНЬ важен!)
        // Сначала масштабируем сцену
        g2d.scale(zoom, zoom);
        // Затем сдвигаем весь мир на координаты камеры (в минус, так как мир движется в обратную сторону)
        g2d.translate(-camX, -camY);

        // 4. ОТРИСОВКА (Теперь все координаты указываются строго в мировом пространстве!)
        int objectX = 10;
        int objectY = 10;

        // Рисуем обычный квадрат. Матрица Java сама применит к нему сдвиг камеры и зум!
        g2d.drawRect(objectX, objectY, 200, 200);

        // Если у вас есть игрок, рисуем его тоже в его мировых координатах:
        g2d.fillRect((int)(playerX - 16), (int)(playerY - 16), 32, 32);

        // 5. Восстанавливаем матрицу (нужно, если вы рисуете интерфейс/UI поверх игры, который не должен двигаться)
        g2d.setTransform(oldTransform);

        // Тут можно рисовать UI (текст, полоску здоровья), они будут намертво привязаны к экрану
        g2d.drawString("Зум: " + zoom, 10, 20);
    }
}
