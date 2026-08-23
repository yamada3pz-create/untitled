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
import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.item.Items;
import org.example.ui.widgets.HotbarWidget;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
    private HotbarWidget hotbar;
    private int mouseX, mouseY;
    private boolean uiOpen;

    private float playerX = 8.0f;
    private float playerY = 8.0f;
    private static final int TILE_SIZE = 16;

    // Хитбокс меньше видимого квадрата (16px), чтобы пролезать в щели меньше 16px
    private static final float PLAYER_SIZE = 12f;
    private static final float HITBOX_OFFSET = 2f;
    private static final float MOVE_SPEED = 120f; // Пикселей в секунду

    // Тонировка слоёв: пол темнее, руда подсвечена
    private static final Color SHADE_FLOOR = new Color(0, 0, 0, 45);
    private static final Color SHADE_ORE   = new Color(255, 255, 255, 40);


    private boolean inGame;
    private final Set<Integer> pressedKeys = new HashSet<>();

    public PlayingState(){

        // Создаем мир и генератор
        world = new World("world_1");
        generator = new WorldGenerator(12345L);




        playerInventory = new SimpleContainer(36);
        playerInv = new PlayerInventoryWindow(playerInventory);
        openWindows = new ArrayList<GuiWindow>();

        hotbar = new HotbarWidget(playerInventory, 27);// слоты 27 - 35 хотбар

        // Тестовые предметы для проверки, потом уберём
        playerInventory.setItem(27, new ItemStack(Items.GRASS, 5));
        playerInventory.setItem(28, new ItemStack(Items.STONE, 12));
        playerInventory.setItem(29, new ItemStack(Items.SAND, 3));
        playerInventory.setItem(33, new ItemStack(Items.WALL, 20));


        // Генерируем отдельные чанки вокруг игрока
        for (int cy = -2; cy <= 2 ; cy++) {
            for (int cx = -2; cx <= 2 ; cx++) {
                Chunk chunk = world.getChunk(cx,cy);
                generator.generateChunk(chunk);
            }
        }

        camera = new Camera(1000,1000);
        camera.follow(-500,-500);
        placePlayerSafely();
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
        // Цифры 1..9 — выбор слота хотбара
        if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
            hotbar.selectByKey(e.getKeyCode() - KeyEvent.VK_1 + 1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(e.getKeyCode());
    }

    @Override
    public void mouseMoved(MouseEvent e){
        mouseX = e.getX();
        mouseY = e.getY();
    }
    @Override
    public void mousePressed(MouseEvent e){
        mouseX = e.getX();
        mouseY = e.getY();
        if(playerInv.isOpen()) return; // Открыт инвентарь мир не кликаем

        int tx = getMouseTileX();
        int ty = getMouseTileY();

        if(e.getButton() == MouseEvent.BUTTON1){
            breakBlock(tx, ty);
        }else{
            placeBlock(tx, ty);
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.isControlDown()) {
            // Ctrl+колесо — зум
            if (e.getWheelRotation() < 0) camera.zoomIn(0.2f);
            else camera.zoomOut(0.2f);
        } else {
            // Колесо — листаем хотбар
            hotbar.scroll(e.getWheelRotation());
        }
    }
    @Override
    public void exit(){
        world.saveAll();
    }


    @Override
    public void update(float dt) {
        // Накапливаем суммарный сдвиг за кадр — так move() видит полный вектор и определяет 8 направлений
        float dirX = 0, dirY = 0;
        if(!uiOpen) {
            if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) dirY -= 1;
            if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) dirY += 1;
            if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) dirX -= 1;
            if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) dirX += 1;
        }
        // Нормальные диагонали: без нее W+D дает скорость =1.41
        if (dirX != 0 && dirY != 0) {
            float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= len;
            dirY /= len;
        }
        float newX = playerX + dirX * MOVE_SPEED * dt;
        if(!collides(newX, playerY)) playerX = newX;

        float newY = playerY + dirY * MOVE_SPEED * dt;
        if(!collides(playerX, newY)) playerY = newY;

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
        int tilesOnScreenX = (int) (game.getWidth() / zoom / TILE_SIZE);
        int tilesOnScreenY = (int) (game.getHeight() / zoom / TILE_SIZE);



        // Рисуем видимые тайлы
        for (int ty = startTileY; ty < startTileY + tilesOnScreenY ; ty++) {
            for (int tx = startTileX; tx < startTileX + tilesOnScreenX ; tx++) {
                int worldPx = tx * TILE_SIZE;
                int worldPy = ty * TILE_SIZE;

                int airId = Blocks.AIR.getGlobalId();

// Пол — самый тёмный слой
                int floorId = world.getFloorIdAt(tx, ty);
                if(floorId != airId) drawTile(g2d, Blocks.get(floorId), worldPx, worldPy, SHADE_FLOOR);

// Руда поверх пола — подсвечена
                int oreId = world.getOreIdAt(tx, ty);
                if(oreId != airId) drawTile(g2d, Blocks.get(oreId), worldPx, worldPy, SHADE_ORE);

// Объекты поверх всего — без тонировки
                int objId = world.getObjectIdAt(tx, ty);
                if(objId != airId) drawTile(g2d, Blocks.get(objId), worldPx, worldPy, null);

// Энтити поверх
                BlockEntity entity = world.getBlockEntityAt(tx, ty);
                if (entity instanceof ChestBlockEntity) {          // ← эти три блока
                    g2d.setColor(new Color(160, 100, 40));         //   оставляешь как были
                    g2d.fillRect(worldPx + 2, worldPy + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                    g2d.setColor(new Color(100, 60, 20));
                    g2d.drawRect(worldPx + 2, worldPy + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                }
                if (entity instanceof PipeBlockEntity) {
                    PipeBlockEntity pipe = (PipeBlockEntity) entity;
                    if (!pipe.isEmpty()) {
                        int fillHeight = (int) (TILE_SIZE * pipe.getAmount());
                        g2d.setColor(new Color(30, 100, 200, 150));
                        g2d.fillRect(worldPx + 3, worldPy + TILE_SIZE - fillHeight - 3, TILE_SIZE - 6, fillHeight);
                    }
                }
                if (entity instanceof MachineBlockEntity) {
                    MachineBlockEntity machine = (MachineBlockEntity) entity;
                    g2d.setColor(new Color(180, 120, 40));
                    g2d.fillRect(worldPx + 1, worldPy + 1, TILE_SIZE - 2, TILE_SIZE - 2);
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

        // хотбар
        hotbar.render(g2d, game.getWidth(), game.getHeight());
    }
    private boolean collides(float px, float py){
        float hx = px + HITBOX_OFFSET;
        float hy = py + HITBOX_OFFSET;

        // Диапазон тайлов аод хитбокс(максимум 2х2 тайла)
        int minTx = Math.floorDiv((int) hx, TILE_SIZE);
        int maxTx = Math.floorDiv((int) (hx + PLAYER_SIZE - 1), TILE_SIZE);
        int minTy = Math.floorDiv((int) hy, TILE_SIZE);
        int maxTy = Math.floorDiv((int) (hy + PLAYER_SIZE - 1), TILE_SIZE);
        for(int ty = minTy; ty <= maxTy; ty++){
            for(int tx = minTx; tx <=maxTx; tx++){
                if(Blocks.get(world.getObjectIdAt(tx,ty)).isSolid()) return true;
            }
        }
        return false;
    }
    // Текстура или цвет + маска тонировки сверху
    private void drawTile(Graphics2D g2d, Block block, int px, int py, Color shade){
        BufferedImage texture = ResourceManager.getBlockTexture(block.getGlobalId(), block.getName());
        if(texture != null){
            g2d.drawImage(texture, px, py, TILE_SIZE, TILE_SIZE, null);
        } else {
            g2d.setColor(block.getColor());
            g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        }
        if(shade != null){
            g2d.setColor(shade);
            g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        }
    }

    // Ставим игрока на ближайшую свободную клетку, чтобы не появился внутри земли
    private void placePlayerSafely() {
        for(int ty = (int) playerY - 16; ty <= (int) playerY + 16; ty++){
            for (int tx = (int)playerX - 16; tx <= (int) playerX + 16; tx++) {
                if(!Blocks.get(world.getObjectIdAt(tx,ty)).isSolid()){
                    playerX = tx;
                    playerY = ty;
                    return;
                }
            }
        }
    }

    // Тайл под курсором
    private int getMouseTileX(){
        return Math.floorDiv((int) camera.screenToWorldX(mouseX), TILE_SIZE);
    }

    private int getMouseTileY(){
        return Math.floorDiv((int) camera.screenToWorldY(mouseY), TILE_SIZE);
    }

    // Ломание: сначала объект, потом руда; пол неразрушаем
    private void breakBlock(int tx, int ty){
        int airId = Blocks.AIR.getGlobalId();

        int objId = world.getObjectIdAt(tx, ty);
        if(objId != airId){
            world.setObjectIdAt(tx, ty, airId);
            world.removeBlockEntityAt(tx, ty);
            dropItem(Blocks.get(objId));
            return;
        }

        int oreId = world.getOreIdAt(tx, ty);
        if(oreId != airId){
            world.setOreIdAt(tx, ty, airId);
            dropItem(Blocks.get(oreId));
        }
    }

    private void dropItem(Block broken){
        Item drop = Items.get(broken.getName());
        if(drop != null) addToInventory(drop, 1);
    }
    // Установка блока из выбранного слота
    private void placeBlock(int tx, int ty){

        int airId = Blocks.AIR.getGlobalId();
        ItemStack held = hotbar.getSelectedItem();
        if(held.isEmpty()) return;

        Block toPlace = itemToBlock(held);
        if(toPlace == null) return;
        if(world.getObjectIdAt(tx, ty) != airId) return;
        if(playerOverlapsTile(tx, ty)) return;

        world.setObjectIdAt(tx, ty, toPlace.getGlobalId());

        if(toPlace.hasBlockEntity()){
            world.setBlockEntityAt(tx, ty, BlockEntityType.create(toPlace.getName(),tx,ty));
        }

        held.shrink(1);
        if(held.getCount() <= 0){
            playerInventory.clearSlot(hotbar.getSelectedContainerIndex());
        }
    }
    // Предмет -> какой блок ставить
    private Block itemToBlock(ItemStack stack) {
        switch (stack.getItem().getId()) {
            case "grass":   return Blocks.GRASS;
            case "stone":   return Blocks.STONE;
            case "sand":    return Blocks.SAND;
            case "pipe":    return Blocks.PIPE;
            case "chest":   return Blocks.CHEST;
            case "machine": return Blocks.MACHINE;
            case "wall":    return  Blocks.WALL;
            default:        return null;
        }
    }
    // Пересекается ли хитбокс игрока с тайлом (tx, ty)
    private boolean playerOverlapsTile(int tx, int ty) {
        float hx = playerX + HITBOX_OFFSET;
        float hy = playerY + HITBOX_OFFSET;
        return hx < tx + TILE_SIZE && hx + PLAYER_SIZE > tx
                && hy < ty + TILE_SIZE && hy + PLAYER_SIZE > ty;
    }

    // Положить предметы: сначала в существующий стек, иначе в пустой слот
    private void addToInventory(Item item, int count) {
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack s = playerInventory.getItem(i);
            if (!s.isEmpty() && s.getItem().getId().equals(item.getId()) && s.isStackable()) {
                s.grow(count);
                return;
            }
        }
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            if (playerInventory.getItem(i).isEmpty()) {
                playerInventory.setItem(i, new ItemStack(item, count));
                return;
            }
        }
    }
}
