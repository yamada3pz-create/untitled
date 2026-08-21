package org.example;

import org.example.block.*;
import org.example.block.Machine.MachineBlockEntity;
import org.example.core.Camera;
import org.example.core.GameState;
import org.example.core.ResourceManager;
import org.example.generation.WorldGenerator;
import org.example.ui.GuiWindow;
import org.example.ui.PlayerInventoryWindow;
import org.example.world.Chunk;
import org.example.world.World;
import org.example.inventory.SimpleContainer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PlayingState extends GameState {

    private final Camera camera;
    private World world;
    private WorldGenerator generator;

    private PlayerInventoryWindow playerInv;
    private SimpleContainer playerInventory;
    private ArrayList<GuiWindow> openWindows;
    private boolean uiOpen;

    private float playerX = 8.0f;
    private float playerY = 8.0f;
    private final int TILE_SIZE = 16;


    private boolean inGame;
    private final Set<Integer> pressedKeys = new HashSet<>();

    public PlayingState(){

        // Создаем мир и генератор
        world = new World("world_1");
        generator = new WorldGenerator(12345L);

        playerInventory = new SimpleContainer(36);
        playerInv = new PlayerInventoryWindow(playerInventory);
        openWindows = new ArrayList<GuiWindow>();

        // Генерируем отдельные чанки вокруг игрока
        for (int cy = -2; cy <= 2 ; cy++) {
            for (int cx = -2; cx <= 2 ; cx++) {
                Chunk chunk = world.getChunk(cx,cy);
                generator.generateChunk(chunk);
            }
        }

        camera = new Camera(1000,1000);
        camera.follow(-500,-500);
    }

    // --- Ввод ---

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());

        // ESC — назад в меню
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            game.setState(new MenuState());
        }
        // В keyPressed — E = инвентарь игрока:
        if (e.getKeyCode() == KeyEvent.VK_E) {
            if (playerInv.isOpen()) {
                playerInv.close();
            } else {
                playerInv.open();
            }
            uiOpen = playerInv.isOpen();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int rotation = e.getWheelRotation();
        if (rotation < 0) {
            camera.zoomIn(0.2f);
        } else {
            camera.zoomOut(0.2f);
        }
    }


    @Override
    public void update() {

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
        if (!uiOpen) {
            // движение игрока (текущий код)
            playerX += dx;
            playerY += dy;
        }

        camera.follow(playerX, playerY);
        world.tickBlockEntity();
    }

    @Override
    public void render(Graphics2D g2d) {

        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        float zoom = camera.getZoom();
        g2d.scale(zoom, zoom);
        g2d.translate(-camera.getX(), -camera.getY());

        // Вычисляем какие тайлы видны на экране
        int startTileX = (int) (camera.getX() / TILE_SIZE);
        int startTileY = (int) (camera.getY() / TILE_SIZE);
        int tilesOnScreenX = (int) (1000 / zoom / TILE_SIZE);
        int tilesOnScreenY = (int) (1000 / zoom / TILE_SIZE);

        // Рисуем видимые тайлы
        for (int ty = startTileY; ty < startTileY + tilesOnScreenY ; ty++) {
            for (int tx = startTileX; tx < startTileX + tilesOnScreenX ; tx++) {
                int worldPx = tx * TILE_SIZE;
                int worldPy = ty * TILE_SIZE;

                int blockId = world.getBlockIdAt(tx,ty);
                Block block = Blocks.get(blockId);

                if(block == Blocks.AIR) continue;

                // Имя текстур
                String pureName = block.getName();
                String texturePath = ResourceManager.getBlockTexturePath(pureName);
                BufferedImage texture = ResourceManager.loadTextures(texturePath);

                if (texture != null) {
                    g2d.drawImage(texture, worldPx, worldPy, TILE_SIZE, TILE_SIZE, null);
                } else {
                    g2d.setColor(block.getColor());
                    g2d.fillRect(worldPx, worldPy, TILE_SIZE, TILE_SIZE);
                    g2d.setColor(new Color(0, 0, 0, 40));
                    g2d.drawRect(worldPx, worldPy, TILE_SIZE, TILE_SIZE);
                }
                BlockEntity entity = world.getBlockEntityAt(tx, ty);
                if (entity instanceof ChestBlockEntity) {
                    // Рисуем сундук — коричневый квадрат с рамкой
                    g2d.setColor(new Color(160, 100, 40));
                    g2d.fillRect(worldPx + 2, worldPy + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                    g2d.setColor(new Color(100, 60, 20));
                    g2d.drawRect(worldPx + 2, worldPy + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                }
                if (entity instanceof PipeBlockEntity) {
                    PipeBlockEntity pipe = (PipeBlockEntity) entity;
                    if (!pipe.isEmpty()) {
                        // Рисуем уровень жидкости внутри трубы
                        int fillHeight = (int) (TILE_SIZE * pipe.getAmount());
                        g2d.setColor(new Color(30, 100, 200, 150));
                        g2d.fillRect(worldPx + 3, worldPy + TILE_SIZE - fillHeight - 3, TILE_SIZE - 6, fillHeight);
                    }
                }
                if (entity instanceof MachineBlockEntity) {
                    MachineBlockEntity machine = (MachineBlockEntity) entity;
                    // Рисуем корпус машины
                    g2d.setColor(new Color(180, 120, 40));
                    g2d.fillRect(worldPx + 1, worldPy + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                    // Если работает — рисуем полоску прогресса
                    if (machine.isRunning()) {
                        int progress = (int) ((float) machine.getCraftProgress() / machine.getCraftTime() * (TILE_SIZE - 4));
                        g2d.setColor(new Color(255, 200, 0));
                        g2d.fillRect(worldPx + 2, worldPy + TILE_SIZE - 4, progress, 2);
                    }
                }
            }

        }


        // Игрок
        g2d.setColor(Color.WHITE);
        g2d.fillRect((int) playerX, (int) playerY, 16, 16);

        // Возвращаем матрицу для UI
        g2d.setTransform(oldTransform);

        // UI текст
        g2d.setColor(Color.GREEN);
        g2d.drawString("World | Зум: " + zoom + " | Чанков: " + world.getLoadedCount(), 10, 20);
        g2d.drawString("Позиция X: " + (int) playerX + " Y: " + (int) playerY, 10, 40);

        for (int i = 0; i < openWindows.size(); i++) {
            openWindows.get(i).render(g2d);
        }
        if (playerInv.isOpen()) {
            playerInv.render(g2d);
        }


    }
}
