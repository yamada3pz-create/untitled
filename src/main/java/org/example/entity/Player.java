package org.example.entity;

import org.example.Game;
import org.example.block.Block;
import org.example.block.Blocks;
import org.example.block.Direction;
import org.example.core.AABB;
import org.example.world.World;

import java.awt.event.KeyEvent;
import java.util.Set;

/**
 * Игрок: позиция, хитбокс, ввод и AABB-скольжение по коллизионным блокам.
 * Движение MC-подходом: каждая ось клиппится отдельно (Vector Clipping),
 * поэтому игрок встаёт вплотную к грани блока и скользит вдоль стен.
 */
public class Player {

    public static final float TILE_SIZE = 16f;

    // Хитбокс 12px меньше видимого квадрата 16px: текстура заходит на блок на 2px
    // (как в MC — коллизия по хитбоксу, а модель/текстура могут выступать за него).
    public static final float PLAYER_SIZE = 12f;
    public static final float HITBOX_OFFSET = 2f;
    public static final float MOVE_SPEED = 120f; // Пикселей в секунду

    // Радиус взаимодействия с миром (в тайлах) — от центра игрока
    public static final float INTERACT_RADIUS = 6f;

    private final World world;

    private float x;
    private float y;
    private float prevX;
    private float prevY;

    private Direction facing = Direction.SOUTH;

    public Player(World world, float x, float y){
        this.world = world;
        setPosition(x, y);
    }

    public float getX(){ return x; }
    public float getY(){ return y; }
    public Direction getFacing(){ return facing; }

    public void setPosition(float x, float y){
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
    }

    // Позиция на прошлом тике — для интерполяции рендера
    public void saveTickPos(){
        prevX = x;
        prevY = y;
    }

    // Плавная позиция между тиками
    public float getRenderX(float tickAlpha){
        return prevX + (x - prevX) * tickAlpha;
    }
    public float getRenderY(float tickAlpha){
        return prevY + (y - prevY) * tickAlpha;
    }

    // Хитбокс (в мировых пикселях)
    public AABB getBox(){
        float hx = x + HITBOX_OFFSET;
        float hy = y + HITBOX_OFFSET;
        return new AABB(hx, hy, hx + PLAYER_SIZE, hy + PLAYER_SIZE);
    }

    public float getCenterX(){ return x + TILE_SIZE / 2f; }
    public float getCenterY(){ return y + TILE_SIZE / 2f; }

    // Можно ли взаимодействовать с тайлом (tx, ty): постройка, добыча, сундук
    public boolean canInteract(int tileX, int tileY){
        float dtx = getCenterX() - (tileX * TILE_SIZE + TILE_SIZE / 2f);
        float dty = getCenterY() - (tileY * TILE_SIZE + TILE_SIZE / 2f);
        return dtx * dtx + dty * dty <= INTERACT_RADIUS * INTERACT_RADIUS * TILE_SIZE * TILE_SIZE;
    }

    // Ввод (WASD / стрелки) + движение с коллизиями
    public void handleInput(Set<Integer> pressedKeys){
        float dirX = 0, dirY = 0;
        if (pressedKeys.contains(KeyEvent.VK_W) || pressedKeys.contains(KeyEvent.VK_UP)) dirY -= 1;
        if (pressedKeys.contains(KeyEvent.VK_S) || pressedKeys.contains(KeyEvent.VK_DOWN)) dirY += 1;
        if (pressedKeys.contains(KeyEvent.VK_A) || pressedKeys.contains(KeyEvent.VK_LEFT)) dirX -= 1;
        if (pressedKeys.contains(KeyEvent.VK_D) || pressedKeys.contains(KeyEvent.VK_RIGHT)) dirX += 1;

        if (dirX == 0 && dirY == 0) return;

        facing = facingFromVector(dirX, dirY);
        if (dirX != 0 && dirY != 0){
            float len = (float) Math.sqrt(dirX * dirX + dirY * dirY);
            dirX /= len;
            dirY /= len;
        }

        float moveX = dirX * MOVE_SPEED * Game.SECONDS_PER_TICK;
        float moveY = dirY * MOVE_SPEED * Game.SECONDS_PER_TICK;

        AABB box = getBox();
        box = box.moved(collideAxisX(box, moveX), 0);
        box = box.moved(0, collideAxisY(box, moveY));

        x = box.minX - HITBOX_OFFSET;
        y = box.minY - HITBOX_OFFSET;
    }

    // Дискретное направление взгляда (4 стороны) по вектору движения
    private Direction facingFromVector(float dirX, float dirY){
        if(Math.abs(dirX) >= Math.abs(dirY)){
            return dirX > 0 ? Direction.EAST : Direction.WEST;
        }
        return dirY > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    // Правильное округление ВНИЗ координаты до тайла
    // (MC: floor, а не (int) trunc) — важно для отрицательных координат.
    private int tileFloor(float worldPx){
        return (int) Math.floor(worldPx / TILE_SIZE);
    }

    // Блок коллизионный? Проверяем объекты Layer 1 (по свойству "collidable" из JSON).
    private boolean isCollidable(int tx, int ty){
        return Blocks.get(world.getObjectIdAt(tx, ty)).isCollidable();
    }

    // MC-клиппинг по X (аналог calculateXOffset): усекаем dx до свободного
    // расстояния до грани блока, чтобы игрок вставал вплотную, грань к грани.
    private float collideAxisX(AABB box, float dx){
        if(dx == 0) return 0f;

        int minTx = tileFloor(Math.min(box.minX, box.minX + dx));
        int maxTx = tileFloor(Math.max(box.maxX, box.maxX + dx) - 0.001f);
        int minTy = tileFloor(box.minY);
        int maxTy = tileFloor(box.maxY - 0.001f);

        for(int ty = minTy; ty <= maxTy; ty++){
            for(int tx = minTx; tx <= maxTx; tx++){
                if(!isCollidable(tx, ty)) continue;

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

    // MC-клиппинг по Y (аналог calculateZOffset) — зеркально к X.
    private float collideAxisY(AABB box, float dy){
        if(dy == 0) return 0f;

        int minTx = tileFloor(box.minX);
        int maxTx = tileFloor(box.maxX - 0.001f);
        int minTy = tileFloor(Math.min(box.minY, box.minY + dy));
        int maxTy = tileFloor(Math.max(box.maxY, box.maxY + dy) - 0.001f);

        for(int ty = minTy; ty <= maxTy; ty++){
            for(int tx = minTx; tx <= maxTx; tx++){
                if(!isCollidable(tx, ty)) continue;

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
}