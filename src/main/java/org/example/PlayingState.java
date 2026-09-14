package org.example;

import org.example.block.*;
import org.example.block.Machine.MachineBlockEntity;
import org.example.core.AABB;
import org.example.core.Camera;
import org.example.core.GameState;
import org.example.core.ResourceManager;
import org.example.core.UI;
import org.example.generation.WorldGenerator;
import org.example.inventory.ChestContainer;
import org.example.inventory.Inventory;
import org.example.ui.ContainerWindow;
import org.example.ui.GuiWindow;
import org.example.ui.PlayerInventoryWindow;
import org.example.ui.widgets.ChestWindow;
import org.example.ui.widgets.ButtonWidget;
import org.example.ui.widgets.HotbarWidget;
import org.example.ui.widgets.Widget;
import org.example.world.Chunk;
import org.example.world.World;
import org.example.item.Item;
import org.example.item.ItemStack;
import org.example.item.Items;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlayingState extends GameState {

    private final Camera camera;
    private World world;
    private WorldGenerator generator;

    private PlayerInventoryWindow playerInv;
    private Inventory playerInventory;
    private ArrayList<GuiWindow> openWindows;
    private ChestWindow chestWindow;
    private ItemStack cursorStack = ItemStack.EMPTY; // предмет зажатый курсором
    private HotbarWidget hotbar;
    private int mouseX, mouseY;
    private boolean uiOpen;

    // Для автозакрытия сундука по дистанции
    private int chestTileX, chestTileY;
    private boolean chestClosedByDistance = false;      // сундук был закрыт из-за отхода (не вручную)
    private boolean playerInvOpenedFirst = false;       // инвентарь игрока открыт ещё до сундука
    private static final float CHEST_CLOSE_RADIUS = 5f; // в тайлах

    private float playerX = 8.0f;
    private float playerY = 8.0f;
    private float prevX = playerX; // позиция на прошлом тике (для интерполяции)
    private float prevY = playerY;

    private static final int TILE_SIZE = 16;

    // Хитбокс 12px меньше видимого квадрата 16px: текстура заходит на блок на 2px
    // (как в MC — коллизия по хитбоксу, а модель/текстура могут выступать за него).
    private static final float PLAYER_SIZE = 12f;
    private static final float HITBOX_OFFSET = 2f;
    private static final float MOVE_SPEED = 120f; // Пикселей в секунду

    // Тонировка слоёв: пол темнее, руда подсвечена
    private static final Color SHADE_FLOOR = new Color(0, 0, 0, 0);
    private static final Color SHADE_ORE   = new Color(255, 255, 255, 0);

    private static final int LOAD_RADIUS = 4;
    private float chunkTimer; // Копит время перед выгрузками


    private boolean inGame;
    private final Set<Integer> pressedKeys = new HashSet<>();

    // Меню паузы (ESC): мир заморожен, поверх рисуем кнопки.
    // Управляется МЫШЬЮ; клавиатура на паузе не работает (ESC — переключатель).
    private boolean paused;
    private final List<Widget> pauseWidgets = new ArrayList<>();

    public PlayingState(String worldName, long seed){

        // Создаем мир и генератор
        world = new World(worldName);
        long realSeed = world.loadSeed(seed);   // Существующий сид важнее переданного
        generator = new WorldGenerator(realSeed);
        world.saveSeed(realSeed);               // Фиксируем при первом создании




        playerInventory = new Inventory(36);
        hotbar = new HotbarWidget(playerInventory, 27);// слоты 27 - 35 хотбар

        boolean loaded = loadPlayer();
        if(!loaded){
            playerInv = new PlayerInventoryWindow(playerInventory);
            openWindows = new ArrayList<GuiWindow>();

            // Тестовые предметы для проверки, потом уберём
            playerInventory.setStack(27, new ItemStack(Items.get("grass"), 5));
            playerInventory.setStack(28, new ItemStack(Items.get("stone"), 12));
            playerInventory.setStack(29, new ItemStack(Items.get("sand"), 3));
            playerInventory.setStack(33, new ItemStack(Items.get("wall"), 20));
            playerInventory.setStack(34, new ItemStack(Items.get("chest"), 2));
        }else {
            playerInv = new PlayerInventoryWindow(playerInventory);
            openWindows = new ArrayList<GuiWindow>();
        }

            // Генерируем отдельные чанки вокруг игрока
            for (int cy = -2; cy <= 2 ; cy++) {
                for (int cx = -2; cx <= 2 ; cx++) {
                    Chunk chunk = world.getChunk(cx,cy);
                    generator.generateChunk(chunk);
                }
            }

        camera = new Camera(1000, 1000);
        camera.follow(playerX, playerY);
        if(!loaded){
            placePlayerSafely();
        }

        initPauseMenu();
    }

    // --- Ввод ---

    @Override
    public void keyPressed(KeyEvent e) {
        pressedKeys.add(e.getKeyCode());

        // ESC: сначала закрываем окна, потом встаём на паузу
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if(anyWindowOpen()){
                closeAllUi();
                chestWindow = null;
            }else if(paused){
                paused = false;
            }else{
                paused = true;
                initPauseMenu(); // scale мог поменяться в настройках — пересоберём кнопки
                pressedKeys.clear(); // чтобы после паузы игрок не «бежал» сам
            }
            return;
        }
        // На паузе клавиатура не управляет меню — только мышь.
        // Снимаем паузу (ESC) — и клавиши снова работают в самой игре.
        if(paused) return;

        if (e.getKeyCode() == KeyEvent.VK_E) {
            if(anyWindowOpen()){
                closeAllUi();
                chestWindow = null;
            }else{
                playerInv.open();
                uiOpen = true;
            }
        }
        // Цифры 1..9 — выбор слота хотбара
        if (e.getKeyCode() >= KeyEvent.VK_1 && e.getKeyCode() <= KeyEvent.VK_9) {
            hotbar.selectByKey(e.getKeyCode() - KeyEvent.VK_1 + 1);
        }
    }

    private void returnCursorToInventory(){
        if(!cursorStack.isEmpty()){
            playerInventory.addItem(cursorStack.getItem(), cursorStack.getCount());
            cursorStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {pressedKeys.remove(e.getKeyCode());
    }

    // --- Меню паузы ---

    private void initPauseMenu(){
        // Пересобираем заново: позиции зависят от виртуального размера (UI.scale)
        pauseWidgets.clear();
        int w = 360, h = 16; // высота кнопки = высота текстуры (16px)
        int x = (UI.virtualW - w) / 2;
        int baseY = Math.min(340, UI.virtualH - 130);

        addPauseButton("Продолжить", x, baseY, w, h, () -> paused = false);
        addPauseButton("Настройки", x, baseY + (h + 14), w, h,
                () -> game.setState(new SettingsState(this)));
        addPauseButton("Сохранить и выйти", x, baseY + 2 * (h + 14), w, h,
                () -> game.setState(new MenuState()));
    }

    private void addPauseButton(String text, int x, int y, int w, int h, Runnable action){
        ButtonWidget b = new ButtonWidget(x, y, w, h, text, new Color(40, 40, 40), Color.WHITE);
        b.setAction(() -> action.run());
        pauseWidgets.add(b);
    }

    @Override
    public void mouseMoved(MouseEvent e){
        super.mouseMoved(e);   // обновляем координаты базового класса (нужны для dispatchMove)
        mouseX = e.getX();
        mouseY = e.getY();
        if(paused){
            dispatchMove(pauseWidgets);
            return;
        }
        sendMouseMoveToUi(uiMouseX(), uiMouseY());
    }

    @Override
    public void mouseDragged(MouseEvent e){
        super.mouseDragged(e);
        if(paused) return;
        mouseX = e.getX();
        mouseY = e.getY();
        sendMouseDragToUi(uiMouseX(), uiMouseY());
    }

    @Override
    public void mouseReleased(MouseEvent e){
        super.mouseReleased(e);
        if(paused) return;
        sendMouseReleaseToUi(uiMouseX(), uiMouseY());
    }

    @Override
    public void mousePressed(MouseEvent e){
        super.mousePressed(e); // обновляем координаты базового класса (нужны для dispatchPress)
        mouseX = e.getX();
        mouseY = e.getY();

        // На паузе клики идут только в меню паузы
        if(paused){
            dispatchPress(pauseWidgets, e.getButton());
            return;
        }

        int ux = uiMouseX();
        int uy = uiMouseY();

        // Если открыт сундук — клик уходит в его окно
        if(chestWindow != null && chestWindow.isOpen()){
            cursorStack = sendMousePressToUi(ux, uy, e.getButton(), e.isShiftDown());
            if(!anyWindowOpen()){
                uiOpen = false;
                chestWindow = null;
            }
            return;
        }
        // Открыт только инвентарь игрока (по E) — клики по миру разрешены,
        // но только если клик НЕ попал в окно инвентаря
        if(playerInv.isOpen()){
            cursorStack = sendMousePressToUi(ux, uy, e.getButton(), e.isShiftDown());
            // Клик внутри окна инвентаря — поглощаем, в мир не передаём
            if(playerInv.contains(ux, uy)){
                if(!anyWindowOpen()){
                    uiOpen = false;
                    chestWindow = null;
                }
                return;
            }
        }

        int tx = getMouseTileX();
        int ty = getMouseTileY();

        if(e.getButton() == MouseEvent.BUTTON1){
            breakBlock(tx, ty);
        }else{
            if(tryOpenChest(tx, ty)) return;
            placeBlock(tx, ty);
        }
    }

    private boolean anyWindowOpen(){
        if(playerInv.isOpen()) return true;
        for(GuiWindow w : openWindows) if(w.isOpen()) return true;
        return false;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(paused) return;
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
        savePlayer();
        world.saveAll();
    }

    // --- Сохранение игрока: позиция + инвентарь + активный слот хотбара ---
    private void savePlayer(){
        try{
            DataOutputStream out = new DataOutputStream(new FileOutputStream(
                    new File(world.getWorldDir(), "player.dat")));
            out.writeFloat(playerX);
            out.writeFloat(playerY);
            playerInventory.save(out);
            out.writeInt(hotbar.getSelected());
            out.close();
        }catch(Exception e){ System.out.println("[Player] Ошибка сохранения: " + e.getMessage()); }
    }

    private boolean loadPlayer(){
        File f = new File(world.getWorldDir(), "player.dat");
        if(!f.exists()) return false;
        try{
            DataInputStream in = new DataInputStream(new FileInputStream(f));
            playerX = in.readFloat();
            playerY = in.readFloat();
            prevX = playerX; prevY = playerY;   // без рывка интерполяции
            playerInventory.load(in);
            hotbar.setSelected(in.readInt());
            in.close();
            return true;
        }catch(Exception e){
            System.out.println("[Player] Ошибка загрузки: " + e.getMessage());
            return false;
        }
    }


    @Override
    public void tick(){
        if(paused) return; // мир заморожен, пока открыто меню паузы

        prevX = playerX;
        prevY = playerY;

        float dirX = 0, dirY = 0;
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) dirY -= 1;
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) dirY += 1;
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) dirX -= 1;
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) dirX += 1;
        if (dirX != 0 && dirY != 0) {
            float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= len;
            dirY /= len;
        }


        // MC-подход: двигаем хитбокс по осям ПО-ОТДЕЛЬНОСТИ.
        // Скорость усекается до реально свободного расстояния до грани блока
        // (Vector Clipping), поэтому игрок всегда встаёт вплотную, без щелей.
        float moveX = dirX * MOVE_SPEED * Game.SECONDS_PER_TICK;
        float moveY = dirY * MOVE_SPEED * Game.SECONDS_PER_TICK;

        AABB box = playerBox();
        box = box.moved(collideAxisX(box, moveX), 0);
        box = box.moved(0, collideAxisY(box, moveY));

        playerX = box.minX - HITBOX_OFFSET;
        playerY = box.minY - HITBOX_OFFSET;

        world.tickSystems();
        loadChunksAroundPlayer();
        unloadFarChunks(Game.SECONDS_PER_TICK);
        checkChestDistance();
    }

    // Покадровое: только камера по интерполированной позиции
    @Override
    public void update(float dt) {
        camera.setScreenSize(game.getWidth(), game.getHeight());
        camera.follow(getRenderX(), getRenderY());
    }

    // Плавная позиция игрока между тиками
    private float getRenderX(){
        return prevX + (playerX - prevX) * game.getTickAlpha();
    }
    private float getRenderY(){
        return prevY + (playerY - prevY) * game.getTickAlpha();
    }

    @Override
    public void render(Graphics2D g2d) {

        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();

        float zoom = camera.getZoom();
        g2d.scale(zoom, zoom);
        g2d.translate(-camera.getX(), -camera.getY());

        // Вычисляем какие тайлы видны на экране
        int startTileX = (int) (camera.getX() / TILE_SIZE) - 1;
        int startTileY = (int) (camera.getY() / TILE_SIZE) - 1;
        int tilesOnScreenX = (int) (game.getWidth() / zoom / TILE_SIZE) + 2;
        int tilesOnScreenY = (int) (game.getHeight() / zoom / TILE_SIZE) + 2;



        // Рисуем видимые тайлы
        for (int ty = startTileY; ty < startTileY + tilesOnScreenY ; ty++) {
            for (int tx = startTileX; tx < startTileX + tilesOnScreenX ; tx++) {
                int worldPx = tx * TILE_SIZE;
                int worldPy = ty * TILE_SIZE;

                int airId = Blocks.AIR.getGlobalId();

                // Пол — самый тёмный слой
                int floorId = world.getFloorIdAt(tx, ty);
                if(floorId != airId) drawTile(g2d, Blocks.get(floorId), worldPx, worldPy, SHADE_FLOOR, "floor", tx, ty);

                // Руда поверх пола — подсвечена
                int oreId = world.getOreIdAt(tx, ty);
                if(oreId != airId) drawTile(g2d, Blocks.get(oreId), worldPx, worldPy, SHADE_ORE, "ore", tx, ty);

                // Объекты поверх всего — без тонировки
                int objId = world.getObjectIdAt(tx, ty);
                if(objId != airId) drawTile(g2d, Blocks.get(objId), worldPx, worldPy, null, "object", tx, ty);

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
        g2d.fillRect((int) getRenderX(), (int) getRenderY(), 16, 16);

        // Возвращаем матрицу для UI
        g2d.setTransform(oldTransform);

        // UI текст
        g2d.setColor(Color.GREEN);
        g2d.drawString("World | Зум: " + zoom + " | Чанков: " + world.getLoadedCount(), 10, 20);
        g2d.drawString("Позиция X: " + (int) playerX + " Y: " + (int) playerY, 10, 40);

        // Весь интерфейс (окна + зажатый предмет) рисуем в "виртуальных" координатах
        // и увеличиваем на UI.scale — так же, как хотбар.
        int s = uiScale();
        Graphics2D uiG2d = (Graphics2D) g2d.create();
        uiG2d.scale(s, s);

        for (int i = 0; i < openWindows.size(); i++) {
            openWindows.get(i).render(uiG2d);
        }
        if (playerInv.isOpen()) {
            playerInv.render(uiG2d);
        }

        // хотбар сам применяет масштаб и рисуется в реальных пикселях
        hotbar.render(g2d, game.getWidth(), game.getHeight());

        // Зажатый предмет под курсором — в той же виртуальной системе, что и окна
        ContainerWindow.drawStackAt(uiG2d, cursorStack, uiMouseX() - 16, uiMouseY() - 16);

        uiG2d.dispose();

        // Меню паузы — поверх всего, мир затемнён. Рисуем в том же виртуальном масштабе, что и окна.
        if(paused){
            Graphics2D pg = (Graphics2D) g2d.create();
            pg.scale(s, s);

            pg.setColor(new Color(0, 0, 0, 160));
            pg.fillRect(0, 0, UI.virtualW, UI.virtualH);

            pg.setColor(Color.WHITE);
            pg.setFont(new Font("Arial", Font.BOLD, 44));
            String title = "Игра приостановлена";
            FontMetrics fm = pg.getFontMetrics();
            pg.drawString(title, (UI.virtualW - fm.stringWidth(title)) / 2, 210);

            for(Widget w : pauseWidgets) w.render(pg);

            pg.dispose();
        }
    }

    // ПКМ по сундуку — открываем его окно
    private boolean tryOpenChest(int tx, int ty){
        if(!(world.getBlockEntityAt(tx, ty) instanceof ChestBlockEntity chest)) return false;

        // Инвентарь игрока уже открыт (по E) до клика по сундуку?
        boolean invAlreadyOpen = playerInv.isOpen();
        this.playerInvOpenedFirst = invAlreadyOpen;

        // Сундук всегда слева-справа? По ТЗ: слева инвентарь игрока, справа сундук.
        positionUiDefault();

        // Открываем окно сундука справа
        if(chestWindow != null && chestWindow.isOpen()) chestWindow.close();
        chestWindow = new ChestWindow(chest.getInventory(), 620, 150);
        chestWindow.open();
        openWindows.add(chestWindow);

        // Инвентарь игрока открываем слева, только если его ещё не открыл сам игрок
        if(!invAlreadyOpen){
            playerInv.setPosition(200, 150);
            playerInv.open();
        }

        uiOpen = true;
        openWindows.removeIf(w -> !w.isOpen());

        // Запоминаем позицию сундука для автозакрытия по дистанции
        chestTileX = tx;
        chestTileY = ty;
        return true;
    }

    // Закрыть все окна UI и вернуть предмет с курсора
    private void closeAllUi(){
        returnCursorToInventory();
        playerInv.close();
        for(GuiWindow w : openWindows) w.close();
        uiOpen = false;
    }

    // Стандартная раскладка: слева инвентарь игрока, справа сундук (не заслоняет мир)
    private void positionUiDefault(){
        playerInv.setPosition(200, 150);
    }

    // Закрыть ТОЛЬКО сундук (инвентарь игрока при этом остаётся)
    private void closeChestOnly(){
        returnCursorToInventory();
        if(chestWindow != null){
            chestWindow.close();
            openWindows.removeIf(w -> !w.isOpen());
        }
        if(!playerInv.isOpen()) uiOpen = false;
        else uiOpen = true;
    }

    // Автозакрытие сундука при отходе на заданный радиус
    private void checkChestDistance(){
        if(chestWindow == null || !chestWindow.isOpen()) return;
        float dtx = playerX - chestTileX;
        float dty = playerY - chestTileY;
        float dist = (float) Math.sqrt(dtx * dtx + dty * dty);
        if(dist > CHEST_CLOSE_RADIUS){
            boolean removeInv = !playerInvOpenedFirst;
            closeChestOnly();
            // Если сундук открывал сам себя (инвентарь не открывался по E) — закрываем и инвентарь
            if(removeInv){
                closeAllUi();
            }
            chestWindow = null;
        }
    }

    // Хитбокс игрока (в мировых пикселях). Меньше видимого квадрата 16px — пролезает в щели.
    private AABB playerBox(){
        float hx = playerX + HITBOX_OFFSET;
        float hy = playerY + HITBOX_OFFSET;
        return new AABB(hx, hy, hx + PLAYER_SIZE, hy + PLAYER_SIZE);
    }

    // Правильное округление ВНИЗ координаты до тайла (MC: floor, а не (int) trunc).
    // Важно для отрицательных координат: (int)(-0.5/16) = 0, а floor = -1.
    private int tileFloor(float worldPx){
        return (int) Math.floor(worldPx / TILE_SIZE);
    }

    // MC-клинпинг по X (аналог calculateXOffset): усекаем dx до свободного
    // расстояния до грани блока, чтобы игрок вставал вплотную, грань к грани.
    private float collideAxisX(AABB box, float dx){
        if(dx == 0) return 0f;

        // Все тайлы, которые хитбокс заденет при сдвиге на dx (свип)
        int minTx = tileFloor(Math.min(box.minX, box.minX + dx));
        int maxTx = tileFloor(Math.max(box.maxX, box.maxX + dx) - 0.001f);
        int minTy = tileFloor(box.minY);
        int maxTy = tileFloor(box.maxY - 0.001f);

        for(int ty = minTy; ty <= maxTy; ty++){
            for(int tx = minTx; tx <= maxTx; tx++){
                if(!Blocks.get(world.getObjectIdAt(tx, ty)).isSolid()) continue;

                float tileMinX = tx * TILE_SIZE;
                float tileMaxX = tileMinX + TILE_SIZE;
                float tileMinY = ty * TILE_SIZE;
                float tileMaxY = tileMinY + TILE_SIZE;

                // Блок на другой высоте — столкновения по X нет
                if(box.maxY <= tileMinY || box.minY >= tileMaxY) continue;

                if(dx > 0 && box.maxX <= tileMinX){
                    float free = tileMinX - box.maxX;
                    if(free < dx) dx = free;
                }
                if(dx < 0 && box.minX >= tileMaxX){
                    float free = tileMaxX - box.minX;
                    if(free > dx) dx = free;
                }
            }
        }
        return dx;
    }

    // MC-клинпинг по Y (аналог calculateZOffset) — зеркально к X.
    private float collideAxisY(AABB box, float dy){
        if(dy == 0) return 0f;

        int minTx = tileFloor(box.minX);
        int maxTx = tileFloor(box.maxX - 0.001f);
        int minTy = tileFloor(Math.min(box.minY, box.minY + dy));
        int maxTy = tileFloor(Math.max(box.maxY, box.maxY + dy) - 0.001f);

        for(int ty = minTy; ty <= maxTy; ty++){
            for(int tx = minTx; tx <= maxTx; tx++){
                if(!Blocks.get(world.getObjectIdAt(tx, ty)).isSolid()) continue;

                float tileMinX = tx * TILE_SIZE;
                float tileMaxX = tileMinX + TILE_SIZE;
                float tileMinY = ty * TILE_SIZE;
                float tileMaxY = tileMinY + TILE_SIZE;

                // Блок не на этой высоте — столкновения по Y нет
                if(box.maxX <= tileMinX || box.minX >= tileMaxX) continue;

                if(dy > 0 && box.maxY <= tileMinY){
                    float free = tileMinY - box.maxY;
                    if(free < dy) dy = free;
                }
                if(dy < 0 && box.minY >= tileMaxY){
                    float free = tileMaxY - box.minY;
                    if(free > dy) dy = free;
                }
            }
        }
        return dy;
    }
    // Текстура или цвет + маска тонировки сверху
    private void drawTile(Graphics2D g2d, Block block, int px, int py, Color shade, String layer, int worldX, int worldY){
        long tick = System.currentTimeMillis() / 50;
        BufferedImage texture = ResourceManager.getBlockTexture(block.getGlobalId(), block.getName(), layer, worldX, worldY, tick);
        if(texture != null){
            g2d.drawImage(texture, px, py, TILE_SIZE, TILE_SIZE, null);
        } else {
            for(int dy = 0; dy < TILE_SIZE; dy += 4){
                for(int dx = 0; dx < TILE_SIZE; dx += 4){
                    g2d.setColor(((dx / 4 + dy / 4) % 2 == 0) ? new Color(255, 0, 255) : Color.BLACK);
                    g2d.fillRect(px + dx, py + dy, 4, 4);
                }
            }
        }
        if(shade != null){
            g2d.setColor(shade);
            g2d.fillRect(px, py, TILE_SIZE, TILE_SIZE);
        }
    }

    // Ставим игрока на ближайшую свободную клетку, чтобы не появился внутри земли
    private void placePlayerSafely() {
        int startTx = tileFloor(playerX);
        int startTy = tileFloor(playerY);
        for(int ty = startTy - 16; ty <= startTy + 16; ty++){
            for (int tx = startTx - 16; tx <= startTx + 16; tx++) {
                if(!Blocks.get(world.getObjectIdAt(tx,ty)).isSolid()){
                    playerX = tx * TILE_SIZE;
                    playerY = ty * TILE_SIZE;
                    prevX = playerX; prevY = playerY;
                    return;
                }
            }
        }
    }

    // Тайл под курсором
    private int getMouseTileX(){
        return tileFloor(camera.screenToWorldX(mouseX));
    }

    private int getMouseTileY(){
        return tileFloor(camera.screenToWorldY(mouseY));
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
        String dropId = broken.getDrops();
        if(dropId == null || dropId.isEmpty()) return;
        Item drop = Items.get(dropId);
        if(drop != null) playerInventory.addItem(drop, 1);
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
            world.setBlockEntityAt(tx, ty, BlockEntityType.create(toPlace.getId(),tx,ty));
        }

        held.shrink(1);
        if(held.getCount() <= 0){
            playerInventory.clearSlot(hotbar.getSelectedContainerIndex());
        }
    }
    // Предмет -> какой блок ставить
    private Block itemToBlock(ItemStack stack) {
        String blockId = stack.getItem().getPlaceBlock();
        if(blockId == null) return null;
        return Blocks.getByName(blockId);
    }
    // Пересекается ли хитбокс игрока с тайлом (tx, ty)
    private boolean playerOverlapsTile(int tx, int ty) {
        return playerBox().intersects(new AABB(tx * TILE_SIZE, ty * TILE_SIZE,
                tx * TILE_SIZE + TILE_SIZE, ty * TILE_SIZE + TILE_SIZE));
    }

    // Догружает квадрат чанков вокруг игрока: память -> файл -> генерация
    private void loadChunksAroundPlayer(){
        int pcx = getPlayerChunkX();
        int pcy = getPlayerChunkY();

        for (int cy = pcy - LOAD_RADIUS; cy <= pcy + LOAD_RADIUS; cy++) {
            for (int cx = pcx - LOAD_RADIUS; cx <= pcx + LOAD_RADIUS; cx++) {
                Chunk c = world.getChunk(cx, cy);
                if (!c.isGenerated()) {
                    generator.generateChunk(c);
                }
            }
        }
    }

    // Раз в секунду выгружает дальние чанки (изменённые сначала сохраняются)
    private void unloadFarChunks(float dt){
        chunkTimer += dt;
        if(chunkTimer < 1f) return;
        chunkTimer = 0f;
        world.unloadDistant(getPlayerChunkX(), getPlayerChunkY(), LOAD_RADIUS + 1);
    }

    private int getPlayerChunkX(){
        return Math.floorDiv(tileFloor(playerX), Chunk.SIZE);
    }
    private int getPlayerChunkY(){
        return Math.floorDiv(tileFloor(playerY), Chunk.SIZE);
    }
    // --- Масштаб интерфейса: все окна живут в "виртуальных" (UI) координатах ---
    // Как хотбар: позиции и размеры в UI-пикселях, реальный размер умножается на UI.scale.
    private static int uiScale(){
        return UI.scale < 1 ? 1 : UI.scale;
    }
    private int uiMouseX(){ return mouseX / uiScale(); }
    private int uiMouseY(){ return mouseY / uiScale(); }

    // --- Проброс ввода в открытые окна ---
    // Инвентарь игрока рисуется поверх openWindows, поэтому клик проверяем в нём первым

    private ItemStack sendMousePressToUi(int mx, int my, int button, boolean shift){
        if(playerInv.isOpen()){
            return playerInv.mousePressed(mx, my, button, shift, cursorStack);
        }
        for(int i = openWindows.size() - 1; i >= 0; i--){
            GuiWindow w = openWindows.get(i);
            if(w.isOpen()){
                return w.mousePressed(mx, my, button, shift, cursorStack);
            }
        }
        return cursorStack;
    }

    private void sendMouseMoveToUi(int mx, int my){
        if(playerInv.isOpen()) playerInv.mouseMoved(mx, my);
        for(int i = 0; i < openWindows.size(); i++) openWindows.get(i).mouseMoved(mx, my);
    }

    private void sendMouseDragToUi(int mx, int my){
        if(playerInv.isOpen()) playerInv.mouseDragged(mx, my);
        for(int i = 0; i < openWindows.size(); i++) openWindows.get(i).mouseDragged(mx, my);
    }

    private void sendMouseReleaseToUi(int mx, int my){
        if(playerInv.isOpen()) playerInv.mouseReleased(mx, my);
        for(int i = 0; i < openWindows.size(); i++) openWindows.get(i).mouseReleased(mx, my);
    }
}
