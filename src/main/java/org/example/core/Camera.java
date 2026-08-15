package org.example.core;

import org.example.Game;

public class Camera {

    private float x,y; // Позиция камеры
    private float zoom = 2.0f; // Масштаб (зум)
    private int screenWidth, screenHeight;// Размер экрана

    public Camera(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.x = 0;
        this.y = 0;
    }

    // Центрирование камеры на заданных координатах объекта
    public void follow (float targetX, float targetY){
        this.x = targetX - (screenWidth / 2f / zoom);
        this.y = targetY - (screenHeight / 2f / zoom);
    }

    // Ручное смещение камеры (клавишами или мышкой)
    public void move(float dx, float dy){
        this.x += dx;
        this.y += dy;
    }

    // Методы для изменения зума
    public void zoomIn(float amount){
        zoom += amount;
        if(zoom < 2.0f) zoom = 2.0f;
        if(zoom > 5.0f) zoom = 5.0f;
    }

    public void zoomOut(float amount){
        zoom -= amount;
        if(zoom < 2.0f) zoom = 2.0f;
        if(zoom > 5.0f) zoom = 5.0f;
    }
    // Геттеры для трансформации при отрисовке
    public float getX()     { return this.x; }
    public float getY()     { return this.y; }
    public float getZoom()  { return this.zoom; }

    // установить новые размеры окна
    public void setScreenSize(int width, int height){
        this.screenWidth = width;
        this.screenHeight = height;
    }
}
