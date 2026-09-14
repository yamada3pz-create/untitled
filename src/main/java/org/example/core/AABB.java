package org.example.core;

/**
 * Выровненный по осям ограничивающий прямоугольник (Axis-Aligned Bounding Box).
 * 2D-аналог AABB из Minecraft. Границы в мировых пикселях.
 *
 * Границы полуоткрытые: касание краев НЕ считается столкновением
 * (как в MC: maxX >= other.minX без "=").
 */
public class AABB {

    public float minX, minY, maxX, maxY;

    public AABB(float minX, float minY, float maxX, float maxY){
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
    }

    /** Пересечение двух коробок. */
    public boolean intersects(AABB other){
        return maxX > other.minX && minX < other.maxX
            && maxY > other.minY && minY < other.maxY;
    }

    /** Копия, сдвинутая на вектор (dx, dy). */
    public AABB moved(float dx, float dy){
        return new AABB(minX + dx, minY + dy, maxX + dx, maxY + dy);
    }

    /** Ширина и высота коробки. */
    public float getWidth()  { return maxX - minX; }
    public float getHeight() { return maxY - minY; }

    @Override
    public String toString(){
        return "AABB[" + minX + "," + minY + " -> " + maxX + "," + maxY + "]";
    }
}